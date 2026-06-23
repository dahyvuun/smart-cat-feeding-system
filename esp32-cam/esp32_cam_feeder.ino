#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include <HTTPClient.h>
#include "esp_camera.h"

// WiFi
const char* WIFI_SSID = "LAB01";
const char* WIFI_PASS = "IchBinGastImLab1312";

// HiveMQ
const char* MQTT_HOST     = "YOUR_HIVEMQ_HOST";
const int   MQTT_PORT     = 8883;
const char* MQTT_USER     = "YOUR_MQTT_USER";
const char* MQTT_PASSWORD = "YOUR_MQTT_PASSWORD";

// FastAPI backend
const char* BACKEND_URL  = "http://YOUR_BACKEND_IP:8000/upload/scfs-machine-001";

// Topics
const char* TOPIC_CAMERA  = "scfs/machine/scfs-machine-001/camera";   // subscribe: trigger
const char* TOPIC_COMMAND = "scfs/machine/scfs-machine-001/command";  // subscribe: dispense

// Camera pins (ESP32-CAM AI-Thinker)
#define PWDN_GPIO_NUM  32
#define RESET_GPIO_NUM -1
#define XCLK_GPIO_NUM   0
#define SIOD_GPIO_NUM  26
#define SIOC_GPIO_NUM  27
#define Y9_GPIO_NUM    35
#define Y8_GPIO_NUM    34
#define Y7_GPIO_NUM    39
#define Y6_GPIO_NUM    36
#define Y5_GPIO_NUM    21
#define Y4_GPIO_NUM    19
#define Y3_GPIO_NUM    18
#define Y2_GPIO_NUM     5
#define VSYNC_GPIO_NUM 25
#define HREF_GPIO_NUM  23
#define PCLK_GPIO_NUM  22

#define SERVO_PIN 13

WiFiClientSecure espClient;
PubSubClient mqtt(espClient);

void initCamera() {
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer   = LEDC_TIMER_0;
  config.pin_d0 = Y2_GPIO_NUM; config.pin_d1 = Y3_GPIO_NUM;
  config.pin_d2 = Y4_GPIO_NUM; config.pin_d3 = Y5_GPIO_NUM;
  config.pin_d4 = Y6_GPIO_NUM; config.pin_d5 = Y7_GPIO_NUM;
  config.pin_d6 = Y8_GPIO_NUM; config.pin_d7 = Y9_GPIO_NUM;
  config.pin_xclk = XCLK_GPIO_NUM; config.pin_pclk = PCLK_GPIO_NUM;
  config.pin_vsync = VSYNC_GPIO_NUM; config.pin_href = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM; config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn = PWDN_GPIO_NUM; config.pin_reset = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size   = FRAMESIZE_QVGA;
  config.jpeg_quality = 15;
  config.fb_count     = 1;

  if (esp_camera_init(&config) != ESP_OK) {
    Serial.println("Camera init failed");
    return;
  }
  Serial.println("Camera ready");
}

void captureAndUpload() {
  camera_fb_t* fb = esp_camera_fb_get();
  if (!fb) { Serial.println("Capture failed"); return; }

  Serial.printf("Captured %d bytes, uploading...\n", fb->len);

  HTTPClient http;
  http.begin(BACKEND_URL);
  http.addHeader("Content-Type", "image/jpeg");

  int code = http.POST(fb->buf, fb->len);
  Serial.printf("Upload response: %d\n", code);
  if (code > 0) Serial.println(http.getString());

  http.end();
  esp_camera_fb_return(fb);
}

void onMessage(char* topic, byte* payload, unsigned int length) {
  String msg = "";
  for (int i = 0; i < length; i++) msg += (char)payload[i];
  Serial.printf("Topic: %s | %s\n", topic, msg.c_str());

  StaticJsonDocument<256> doc;
  deserializeJson(doc, msg);

  // Trigger from backend: capture photo
  if (String(topic) == TOPIC_CAMERA) {
    String action = doc["action"];
    if (action == "CAPTURE") captureAndUpload();
  }

  // Dispense command
  if (String(topic) == TOPIC_COMMAND) {
    String action = doc["action"];
    if (action == "DISPENSE") {
      Serial.println("Dispensing food...");
      analogWrite(SERVO_PIN, 128);
      delay(1000);
      analogWrite(SERVO_PIN, 0);
    }
  }
}

void connectWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) { delay(500); Serial.print("."); }
  Serial.println("\nWiFi connected: " + WiFi.localIP().toString());
}

void connectMQTT() {
  while (!mqtt.connected()) {
    String id = "esp32-cam-" + String((uint32_t)ESP.getEfuseMac(), HEX);
    if (mqtt.connect(id.c_str(), MQTT_USER, MQTT_PASSWORD)) {
      Serial.println("MQTT connected");
      mqtt.subscribe(TOPIC_CAMERA);
      mqtt.subscribe(TOPIC_COMMAND);
    } else {
      Serial.printf("MQTT failed: %d\n", mqtt.state());
      delay(2000);
    }
  }
}

void setup() {
  Serial.begin(115200);
  pinMode(SERVO_PIN, OUTPUT);
  initCamera();
  connectWiFi();
  espClient.setInsecure();
  mqtt.setServer(MQTT_HOST, MQTT_PORT);
  mqtt.setCallback(onMessage);
  connectMQTT();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) connectWiFi();
  if (!mqtt.connected()) connectMQTT();
  mqtt.loop();
}
