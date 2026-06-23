import base64
import json
import os
import threading
import time
from io import BytesIO

import paho.mqtt.client as mqtt
import numpy as np
from PIL import Image
from ultralytics import YOLO
from fastapi import FastAPI
import uvicorn

# HiveMQ credentials
MQTT_HOST = "YOUR_HIVEMQ_HOST"
MQTT_PORT = 8883
MQTT_USER = "YOUR_MQTT_USER"
MQTT_PASSWORD = "YOUR_MQTT_PASSWORD"

TOPIC_IMAGE   = "scfs/machine/scfs-machine-001/image"    # subscribe
TOPIC_SENSORS = "scfs/machine/scfs-machine-001/sensors"  # publish results to Node-RED

# Load YOLOv8 model (downloads automatically on first run)
print("Loading YOLOv8 model...")
model = YOLO("yolov8n.pt")  # nano = fastest
print("Model ready")

app = FastAPI()
mqtt_client = None


def detect_cat(image_bytes: bytes) -> dict:
    """Run YOLOv8 on image bytes, return cat detection result."""
    image = Image.open(BytesIO(image_bytes)).convert("RGB")
    results = model(image, verbose=False)

    cat_detected = False
    confidence = 0.0

    for result in results:
        for box in result.boxes:
            cls_id = int(box.cls[0])
            cls_name = model.names[cls_id]
            conf = float(box.conf[0])

            if cls_name == "cat" and conf > 0.5:
                cat_detected = True
                confidence = max(confidence, conf)

    return {
        "cat_present": cat_detected,
        "confidence": round(confidence, 3)
    }


def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print("MQTT connected")
        client.subscribe(TOPIC_IMAGE)
        print(f"Subscribed to {TOPIC_IMAGE}")
    else:
        print(f"MQTT connection failed: {rc}")


def on_message(client, userdata, msg):
    try:
        payload = json.loads(msg.payload.decode())
        image_b64 = payload.get("image")
        if not image_b64:
            return

        # Decode base64 image
        image_bytes = base64.b64decode(image_b64)

        # Run detection
        result = detect_cat(image_bytes)
        print(f"Detection result: {result}")

        # Publish to catfeeder/sensors for Node-RED
        sensor_payload = {
            "cat_present": result["cat_present"],
            "cat_id": "A",          # single cat for now
            "food_weight": 0,       # filled by Load Cell (Aaron)
            "confidence": result["confidence"],
            "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S")
        }
        client.publish(TOPIC_SENSORS, json.dumps(sensor_payload))
        print(f"Published to {TOPIC_SENSORS}: {sensor_payload}")

    except Exception as e:
        print(f"Error processing image: {e}")


def start_mqtt():
    global mqtt_client
    mqtt_client = mqtt.Client()
    mqtt_client.username_pw_set(MQTT_USER, MQTT_PASSWORD)
    mqtt_client.tls_set()  # HiveMQ requires TLS
    mqtt_client.on_connect = on_connect
    mqtt_client.on_message = on_message
    mqtt_client.connect(MQTT_HOST, MQTT_PORT, 60)
    mqtt_client.loop_forever()


@app.get("/health")
def health():
    return {"status": "ok", "mqtt_connected": mqtt_client.is_connected() if mqtt_client else False}


if __name__ == "__main__":
    # Start MQTT in background thread
    mqtt_thread = threading.Thread(target=start_mqtt, daemon=True)
    mqtt_thread.start()

    # Start FastAPI
    uvicorn.run(app, host="0.0.0.0", port=8000)
