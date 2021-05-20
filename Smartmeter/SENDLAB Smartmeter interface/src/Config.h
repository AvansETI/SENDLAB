#pragma once
constexpr char WIFI_SSID[] = "SENDLAB";
constexpr char WIFI_PASS[] = "SEnDLab@LA121";

constexpr char SENSORID[] = "SENDLAB_SMARTMETER";

constexpr char MQTT_SERVER_HOST[] = "sendlab.nl";
constexpr int MQTT_SERVER_PORT = 11884;
constexpr char MQTT_ID[] = "SENDLAB_SMARTMETER";
constexpr char MQTT_USERNAME[] = "node";
constexpr char MQTT_PASS[] = "smartmeternode";

// constexpr char MQTT_SERVER_HOST[] = "test.mosquitto.org";
// constexpr int MQTT_SERVER_PORT = 1883;
// constexpr char MQTT_ID[] = "SENDLAB_SMARTMETER";
// constexpr char MQTT_USERNAME[] = "";
// constexpr char MQTT_PASS[] = "";

constexpr int PIN_RED = D6;
constexpr int PIN_GREEN = D1;
constexpr int PIN_BLUE = D5;

constexpr int INTERVAL = 10;