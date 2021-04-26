#include <P1Adapter.h>
#include <WiFiAdapter.h>
#include <MQTTAdapter.h>
#include <ArduinoJson.h>

WifiAdapter wifi;
MQTTAdapter mqtt;
P1Adapter p1;

void clearLed()
{
  digitalWrite(PIN_RED, HIGH);
  digitalWrite(PIN_GREEN, HIGH);
  digitalWrite(PIN_BLUE, HIGH);
}

void setup()
{
  // put your setup code here, to run once:

  pinMode(PIN_RED, OUTPUT);   // green
  pinMode(PIN_GREEN, OUTPUT); // blue
  pinMode(PIN_BLUE, OUTPUT);  // red

  clearLed();

  Serial.begin(9600);

  wifi.connect();
  Serial.print("Setup Wi-Fi:");
  while(wifi.status() != WL_CONNECTED){
    clearLed();
    delay(50);
    digitalWrite(PIN_RED, LOW);
    Serial.print('.');
    delay(5);
  }
  clearLed();
  Serial.println();
  Serial.print("WiFi connected: ");
  Serial.println(wifi.getIp());

  mqtt.init(wifi.wifi);
  mqtt.connect();
  Serial.println("MQTT Connected");

  DynamicJsonDocument doc(2048);
  DynamicJsonDocument arrayDoc(2048);
  JsonArray array = arrayDoc.to<JsonArray>();

  doc["version"] = 1;
  doc["type"] = "sensor";
  doc["id"] = SENSORID;
  doc["name"] = "Smartmeter";
  doc["pecc"] = "";
  doc["pdhk"] = "";
  doc["nonce"] = int(random(LONG_MAX)*100000);

  JsonObject n1 = array.createNestedObject();
  n1["name"] = "Timestamp";
  n1["description"] = "Date-time stamp of the P1 message. Format: YYMMDDhhmmssX. OBIS-ref: 0-0:1.0.0.255";
  n1["unit"] = "timestamp";

  JsonObject n2 = array.createNestedObject();
  n2["name"] = "Electricity delivered to client (Tariff 1)";
  n2["description"] = "Meter Reading electricity delivered to client (Tariff 1) in 0,001 kWh. OBIS-ref: 1-0:1.8.1.255";
  n2["unit"] = "0,001 kWh";

  JsonObject n3 = array.createNestedObject();
  n3["name"] = "Electricity delivered to client (Tariff 2)";
  n3["description"] = "Meter Reading electricity delivered to client (Tariff 2) in 0,001 kWh. OBIS-ref: 1-0:1.8.2.255";
  n3["unit"] = "0,001 kWh";

  JsonObject n4 = array.createNestedObject();
  n4["name"] = "Electricity delivered by client (Tariff 1)";
  n4["description"] = "Meter Reading electricity delivered by client (Tariff 1) in 0,001 kWh. OBIS-ref: 1-0:2.8.1.255";
  n4["unit"] = "0,001 kWh";

  JsonObject n5 = array.createNestedObject();
  n5["name"] = "Electricity delivered by client (Tariff 2)";
  n5["description"] = "Meter Reading electricity delivered by client (Tariff 2) in 0,001 kWh. OBIS-ref: 1-0:2.8.2.255";
  n5["unit"] = "0,001 kWh";

  JsonObject n6 = array.createNestedObject();
  n6["name"] = "Tariff indicator electricity";
  n6["description"] = "Tariff indicator elec-tricity. The tariffin-dicator can also be used to switch tariffdependent loads e.g boilers. OBIS-ref: 0-0:96.14.0.255";
  n6["unit"] = "indicator";

  JsonObject n7 = array.createNestedObject();
  n7["name"] = "";
  n7["description"] = "";
  n7["unit"] = "";
  JsonObject n = array.createNestedObject();
  n["name"] = "";
  n["description"] = "";
  n["unit"] = "";
  JsonObject n = array.createNestedObject();
  n["name"] = "";
  n["description"] = "";
  n["unit"] = "";
  JsonObject n = array.createNestedObject();
  n["name"] = "";
  n["description"] = "";
  n["unit"] = "";

  doc["measurements"] = array;


"{
    "version": 1, # The version is important when it comes to security, version 1 is no security, 2 is symmetric, 3 is diffie helman, etc.
    "type":  "simulation",
    "id":    sensor_id,
    "name":  "test 1",
    "pecc":  "public ecc key",
    "pdhk":  "public diffie hellman key",
    "nonce": int( random.random()*100000 ),
    "measurements": [{
        "name": "temperature",
        "description": "Temperature sensor -40 to 100 with 0.1 accuracy.",
        "unit": "degree of Celsius",
    },{
        "name": "humidity",
        "description": "Humidity sensor 0 to 100 with 0.5 accuracy.",
        "unit": "%",
    }],
    "actuators": [{
        "name": "cv",
        "description": "Central heating control from 10 tp 40 degree of Celsius.",
        "unit": "degree of Celsius",
    }],
    "hash": "60A335487A7277CA00619D02038BAC48413832337E0CACA0A9673BCDC0D4B45A",
    "sign": "MEUCIHiBfZmjK1R92CifJ9rki3t66oh+hnZQik3oEtngSJoFAiEAv8jD44JMqlGQ+UGPQqJMymXOTkXacsP+KClyBnX0voQ="
};"


  p1.init();

  digitalWrite(PIN_GREEN, LOW);
  delay(2000);
  clearLed();
}

void loop()
{
  if(!mqtt.connected()){
    mqtt.connect();
  }
  
  if(p1.capture_p1() == true){
    mqtt.publish("SENDLABSMARTMETERTEST", (char*) p1.p1_buffer);

    digitalWrite(PIN_BLUE, LOW);
    delay(50);
    clearLed();
  }
}