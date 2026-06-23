#include <WiFi.h>
#include <WiFiClientSecure.h>
#include <PubSubClient.h>
#include <ArduinoJson.h>
#include "esp_camera.h"
#include <base64.h>

// WLAN
const char* WIFI_SSID = "LAB01";
const char* WIFI_PASS = "IchBinGastImLab1312";

// HiveMQ - Aaron's credentials (fill in)
const char* MQTT_HOST = "YOUR_HIVEMQ_HOST";
const int   MQTT_PORT = 8883;
const char* MQTT_USER = "YOUR_MQTT_USER";
const char* MQTT_PASSWORD = "YOUR_MQTT_PASSWORD";

// Topics
const char* TOPIC_IMAGE    = "scfs/machine/scfs-machine-001/image";    // ESP32 → AI backend
const char* TOPIC_SENSORS  = "scfs/machine/scfs-machine-001/sensors";  // ESP32 → Node-RED
const char* TOPIC_COMMAND  = "scfs/machine/scfs-machine-001/command";  // Node-RED → ESP32

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

// Servo (food dispenser)
#define SERVO_PIN 13

WiFiClientSecure espClient;
PubSubClient mqtt(espClient);

unsigned long lastCapture = 0;
const unsigned long captureInterval = 3000; // every 3 seconds

void initCamera() {
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer   = LEDC_TIMER_0;
  config.pin_d0       = Y2_GPIO_NUM;
  config.pin_d1       = Y3_GPIO_NUM;
  config.pin_d2       = Y4_GPIO_NUM;
  config.pin_d3       = Y5_GPIO_NUM;
  config.pin_d4       = Y6_GPIO_NUM;
  config.pin_d5       = Y7_GPIO_NUM;
  config.pin_d6       = Y8_GPIO_NUM;
  config.pin_d7       = Y9_GPIO_NUM;
  config.pin_xclk     = XCLK_GPIO_NUM;
  config.pin_pclk     = PCLK_GPIO_NUM;
  config.pin_vsync    = VSYNC_GPIO_NUM;
  config.pin_href     = HREF_GPIO_NUM;
  config.pin_sscb_sda = SIOD_GPIO_NUM;
  config.pin_sscb_scl = SIOC_GPIO_NUM;
  config.pin_pwdn     = PWDN_GPIO_NUM;
  config.pin_reset    = RESET_GPIO_NUM;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size   = FRAMESIZE_QVGA; // 320x240 - small enough for MQTT
  config.jpeg_quality = 20;
  config.fb_count     = 1;

  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("Camera init failed: 0x%x\n", err);
    return;
  }
  Serial.println("Camera ready");
}

void connectWiFi() {
  WiFi.begin(WIFI_SSID, WIFI_PASS);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("WiFi connected, IP: ");
  Serial.println(WiFi.localIP());
}

void onMessage(char* topic, byte* payload, unsigned int length) {
  String msg = "";
  for (int i = 0; i < length; i++) msg += (char)payload[i];

  Serial.print("Message on ");
  Serial.print(topic);
  Serial.print(": ");
  Serial.println(msg);

  // Handle command from Node-RED
  if (String(topic) == TOPIC_COMMAND) {
    StaticJsonDocument<256> doc;
    deserializeJson(doc, msg);
    String action = doc["action"];

    if (action == "DISPENSE") {
      Serial.println("Dispensing food...");
      // Activate servo
      analogWrite(SERVO_PIN, 128);
      delay(1000);
      analogWrite(SERVO_PIN, 0);
    }
  }
}

void connectMQTT() {
  while (!mqtt.connected()) {
    Serial.print("Connecting MQTT... ");
    String clientId = "esp32-cam-" + String((uint32_t)ESP.getEfuseMac(), HEX);
    if (mqtt.connect(clientId.c_str(), MQTT_USER, MQTT_PASSWORD)) {
      Serial.println("connected");
      mqtt.subscribe(TOPIC_COMMAND);
    } else {
      Serial.print("failed: ");
      Serial.println(mqtt.state());
      delay(2000);
    }
  }
}

void captureAndPublish() {
  camera_fb_t* fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("Camera capture failed");
    return;
  }

  // Encode image to base64
  String encoded = base64::encode(fb->buf, fb->len);
  esp_camera_fb_return(fb);

  // MQTT max payload is ~128KB - QVGA JPEG should be ~5-15KB encoded
  if (encoded.length() > 100000) {
    Serial.println("Image too large, skipping");
    return;
  }

  // Publish image
  StaticJsonDocument<512> doc;
  doc["image"] = encoded;
  doc["timestamp"] = millis();

  String payload;
  serializeJson(doc, payload);

  mqtt.setBufferSize(120000);
  bool ok = mqtt.publish(TOPIC_IMAGE, payload.c_str());
  Serial.printf("Image published (%d bytes): %s\n", payload.length(), ok ? "OK" : "FAIL");
}

void setup() {
  Serial.begin(115200);
  pinMode(SERVO_PIN, OUTPUT);

  initCamera();
  connectWiFi();

  espClient.setInsecure();
  mqtt.setServer(MQTT_HOST, MQTT_PORT);
  mqtt.setCallback(onMessage);
  mqtt.setBufferSize(120000);

  connectMQTT();
}

void loop() {
  if (WiFi.status() != WL_CONNECTED) connectWiFi();
  if (!mqtt.connected()) connectMQTT();
  mqtt.loop();

  unsigned long now = millis();
  if (now - lastCapture >= captureInterval) {
    lastCapture = now;
    captureAndPublish();
  }
}
