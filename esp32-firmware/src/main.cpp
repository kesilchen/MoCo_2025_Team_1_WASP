#include <Arduino.h>
#include <WiFi.h>
#include <WebServer.h>
#include <Preferences.h>
#include <ArduinoJson.h>

uint8_t device_id[16];
const char* device_type = "uwbeesp32_basic_led";

const char* ssid = "testnet";
const char* password = "esp32testnet";

const int ledPin = 33;

Preferences prefs;
WebServer server(80);

String uuidToString(const uint8_t* uuidBytes) {
  const char* hex = "0123456789abcdef";
  String uuid;

  for (int i = 0; i < 16; i++) {
    if (i == 4 || i == 6 || i == 8 || i == 10) {
      uuid += '-';
    }
    uuid += hex[(uuidBytes[i] >> 4) & 0x0F];
    uuid += hex[uuidBytes[i] & 0x0F];
  }

  return uuid;
}

void handleInfoGet() {
  JsonDocument doc;
  doc["deviceId"] = uuidToString(device_id);
  doc["deviceType"] = device_type;

  String json;
  serializeJson(doc, json);
  server.send(200, "application/json", json);
}

void handleLedPost() {
  if (server.hasArg("plain")) {
    String body = server.arg("plain");
    body.trim();
    
    if (body == "on") {
      digitalWrite(ledPin, HIGH);
      server.send(200, "text/plain", "LED turned ON");
    } else if (body == "off") {
      digitalWrite(ledPin, LOW);
      server.send(200, "text/plain", "LED turned OFF");
    } else {
      server.send(400, "text/plain", "Invalid value, send 'on' or 'off'");
    }
  } else {
    server.send(400, "text/plain", "No POST body received");
  }
}

void getDeviceId(uint8_t* device_uuid_bytes) {
  prefs.begin("device", false);

  size_t len = prefs.getBytes("device_id", device_uuid_bytes, 16);

  if (len != 16) {
    Serial.println("Generating new device ID");
    for (int i = 0; i < 16; i++) {
      device_uuid_bytes[i] = random(0, 256);
    }
    prefs.putBytes("device_id", device_uuid_bytes, 16);
  }

  prefs.end();
}

void setup() {
  Serial.begin(115200);

  pinMode(ledPin, OUTPUT);

  getDeviceId(device_id);

  Serial.print("Device ID: ");
  Serial.println(uuidToString(device_id));

  Serial.print("Connecting to Wi-Fi");
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(500);
    Serial.print(".");
  }
  Serial.println();
  Serial.print("Connected! IP address: ");
  Serial.println(WiFi.localIP());

  server.on("/led", HTTP_POST, handleLedPost);
  server.on("/info", HTTP_GET, handleInfoGet);

  server.begin();
  Serial.println("HTTP server started");
}

void loop() {
  server.handleClient();
}
