import base64
import json
import os
import threading
import time
import uuid
from io import BytesIO

import paho.mqtt.client as mqtt
import numpy as np
from PIL import Image
from fastapi import FastAPI, UploadFile, File, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import httpx
import torch
from transformers import CLIPProcessor, CLIPModel

# ─── Config (fill in after getting from Aaron) ───────────────────────────────
MQTT_HOST     = "YOUR_HIVEMQ_HOST"
MQTT_PORT     = 8883
MQTT_USER     = "YOUR_MQTT_USER"
MQTT_PASSWORD = "YOUR_MQTT_PASSWORD"

SUPABASE_URL     = "YOUR_SUPABASE_URL"
SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"

# MQTT Topics
TOPIC_SENSORS = "scfs/machine/scfs-machine-001/sensors"   # ESP32 → backend
TOPIC_CAMERA  = "scfs/machine/scfs-machine-001/camera"    # backend → ESP32-CAM trigger
TOPIC_COMMAND = "scfs/machine/scfs-machine-001/command"   # backend → ESP32 feeder

# ─── CLIP Model ──────────────────────────────────────────────────────────────
print("Loading CLIP model...")
clip_model     = CLIPModel.from_pretrained("openai/clip-vit-base-patch32")
clip_processor = CLIPProcessor.from_pretrained("openai/clip-vit-base-patch32")
print("CLIP model ready")

app = FastAPI()
app.add_middleware(CORSMiddleware, allow_origins=["*"], allow_methods=["*"], allow_headers=["*"])

mqtt_client = None

# ─── Supabase helpers ─────────────────────────────────────────────────────────
async def supabase_get(table: str, params: dict = {}):
    headers = {
        "apikey": SUPABASE_ANON_KEY,
        "Authorization": f"Bearer {SUPABASE_ANON_KEY}",
    }
    query = "&".join([f"{k}=eq.{v}" for k, v in params.items()])
    url = f"{SUPABASE_URL}/rest/v1/{table}?{query}"
    async with httpx.AsyncClient() as client:
        r = await client.get(url, headers=headers)
        return r.json()

async def supabase_insert(table: str, data: dict):
    headers = {
        "apikey": SUPABASE_ANON_KEY,
        "Authorization": f"Bearer {SUPABASE_ANON_KEY}",
        "Content-Type": "application/json",
        "Prefer": "return=representation",
    }
    async with httpx.AsyncClient() as client:
        r = await client.post(f"{SUPABASE_URL}/rest/v1/{table}", headers=headers, json=data)
        return r.json()

async def supabase_upload_image(machine_id: str, image_bytes: bytes) -> str:
    """Upload image to Supabase Storage, return public URL."""
    filename = f"{machine_id}/{uuid.uuid4()}.jpg"
    headers = {
        "apikey": SUPABASE_ANON_KEY,
        "Authorization": f"Bearer {SUPABASE_ANON_KEY}",
        "Content-Type": "image/jpeg",
    }
    async with httpx.AsyncClient() as client:
        r = await client.post(
            f"{SUPABASE_URL}/storage/v1/object/cat-images/{filename}",
            headers=headers,
            content=image_bytes,
        )
    return f"{SUPABASE_URL}/storage/v1/object/public/cat-images/{filename}"

# ─── CLIP matching ────────────────────────────────────────────────────────────
async def match_cat(image_bytes: bytes, machine_id: str) -> dict:
    """Compare captured image against registered cat reference photos using CLIP."""
    # Load cats linked to this machine
    machine_cats = await supabase_get("machine_cats", {"machine_id": machine_id})
    if not machine_cats:
        return {"cat_detected": False, "cat_id": None, "cat_name": None, "confidence": 0.0}

    captured = Image.open(BytesIO(image_bytes)).convert("RGB")
    best_match = None
    best_score = -1.0

    for mc in machine_cats:
        cat_id = mc["cat_id"]
        # Load training photos for this cat
        photos = await supabase_get("cat_training_photos", {"cat_id": cat_id, "machine_id": machine_id})
        if not photos:
            continue

        scores = []
        for photo in photos:
            # Download reference image
            async with httpx.AsyncClient() as client:
                r = await client.get(photo["image_path"])
            ref_image = Image.open(BytesIO(r.content)).convert("RGB")

            # CLIP embedding comparison
            inputs = clip_processor(images=[captured, ref_image], return_tensors="pt", padding=True)
            with torch.no_grad():
                features = clip_model.get_image_features(**inputs)
            features = features / features.norm(dim=-1, keepdim=True)
            score = float(torch.cosine_similarity(features[0].unsqueeze(0), features[1].unsqueeze(0)))
            scores.append(score)

        avg_score = sum(scores) / len(scores)
        if avg_score > best_score:
            best_score = avg_score
            best_match = cat_id

    if best_score > 0.75:  # threshold
        # Get cat name
        cats = await supabase_get("cats", {"id": best_match})
        cat_name = cats[0]["name"] if cats else "Unknown"
        return {"cat_detected": True, "cat_id": best_match, "cat_name": cat_name, "confidence": round(best_score, 3)}

    return {"cat_detected": False, "cat_id": None, "cat_name": None, "confidence": round(best_score, 3)}

# ─── Image upload endpoint (called by ESP32-CAM via HTTP) ─────────────────────
@app.post("/upload/{machine_id}")
async def upload_image(machine_id: str, file: UploadFile = File(...)):
    image_bytes = await file.read()

    # 1. Store image in Supabase Storage
    image_url = await supabase_upload_image(machine_id, image_bytes)

    # 2. Run CLIP matching
    result = await match_cat(image_bytes, machine_id)

    # 3. Save camera event to DB
    await supabase_insert("camera_events", {
        "machine_id": machine_id,
        "image_path": image_url,
        "motion_detected": True,
        "cat_detected": result["cat_detected"],
        "detected_cat_id": result["cat_id"],
        "detected_cat_name": result["cat_name"],
        "confidence": result["confidence"],
    })

    # 4. If cat detected, publish dispense command via MQTT
    if result["cat_detected"] and mqtt_client:
        command = {
            "action": "DISPENSE",
            "cat_id": result["cat_id"],
            "cat_name": result["cat_name"],
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
        }
        mqtt_client.publish(TOPIC_COMMAND, json.dumps(command))

    return {"status": "ok", "result": result, "image_url": image_url}

# ─── MQTT (sensor data from ESP32 feeder) ────────────────────────────────────
def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("MQTT connected")
        client.subscribe(TOPIC_SENSORS)
    else:
        print(f"MQTT failed: {rc}")

def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
        print(f"Sensor data: {payload}")

        # If motion detected, trigger ESP32-CAM
        if payload.get("motion"):
            trigger = {"action": "CAPTURE", "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")}
            client.publish(TOPIC_CAMERA, json.dumps(trigger))
            print("Camera trigger sent")

    except Exception as e:
        print(f"MQTT message error: {e}")

def start_mqtt():
    global mqtt_client
    mqtt_client = mqtt.Client()
    mqtt_client.username_pw_set(MQTT_USER, MQTT_PASSWORD)
    mqtt_client.tls_set()
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message
    mqtt_client.connect(MQTT_HOST, MQTT_PORT, 60)
    mqtt_client.loop_forever()

# ─── Health check ─────────────────────────────────────────────────────────────
@app.get("/health")
def health():
    return {"status": "ok", "mqtt": mqtt_client.is_connected() if mqtt_client else False}

# ─── Start ────────────────────────────────────────────────────────────────────
if __name__ == "__main__":
    mqtt_thread = threading.Thread(target=start_mqtt, daemon=True)
    mqtt_thread.start()
    uvicorn.run(app, host="0.0.0.0", port=8000)
