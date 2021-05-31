#include <P1Adapter.h>
#include <WiFiAdapter.h>
#include <MQTTAdapter.h>
#include <ArduinoJson.h>
#include <WiFiUdp.h>
#include <NTPClient.h>
#include <list>
#include <map>
#include <string>

WifiAdapter wifi;
MQTTAdapter mqtt;
P1Adapter p1;
WiFiUDP udp;
NTPClient ntp(udp, "nl.pool.ntp.org", 3600);

long lastTime = 0;
bool receivedReaction = false;

void clearLed()
{
  digitalWrite(PIN_RED, HIGH);
  digitalWrite(PIN_GREEN, HIGH);
  digitalWrite(PIN_BLUE, HIGH);
}

std::string createInit(){
  DynamicJsonDocument doc(2048);
  DynamicJsonDocument arrayDoc(2048);
  DynamicJsonDocument arrayDoc2(1);
  JsonArray array = arrayDoc.to<JsonArray>();
  JsonArray array2 = arrayDoc2.to<JsonArray>();

  doc["mode"] = 0;
  doc["type"] = "sensor";
  doc["id"] = SENSORID;
  doc["name"] = "SENDLAB Smartmeter";
  
  JsonObject n = array.createNestedObject();
  n["name"] = "timestamp";
  n["description"] = "Date-time stamp of the P1 message. Format: YYMMDDhhmmssX. OBIS-ref: 0-0:1.0.0.255";
  n["unit"] = "timestamp";

  JsonObject n1 = array.createNestedObject();
  n1["name"] = "Electricity delivered to client (Tariff 1)";
  n1["description"] = "Meter Reading electricity delivered to client (Tariff 1) in 0,001 kWh. OBIS-ref: 1-0:1.8.1.255";
  n1["unit"] = "0,001 kWh";

  JsonObject n2 = array.createNestedObject();
  n2["name"] = "Electricity delivered to client (Tariff 2)";
  n2["description"] = "Meter Reading electricity delivered to client (Tariff 2) in 0,001 kWh. OBIS-ref: 1-0:1.8.2.255";
  n2["unit"] = "0,001 kWh";

  JsonObject n3 = array.createNestedObject();
  n3["name"] = "Electricity delivered by client (Tariff 1)";
  n3["description"] = "Meter Reading electricity delivered by client (Tariff 1) in 0,001 kWh. OBIS-ref: 1-0:2.8.1.255";
  n3["unit"] = "0,001 kWh";

  JsonObject n4 = array.createNestedObject();
  n4["name"] = "Electricity delivered by client (Tariff 2)";
  n4["description"] = "Meter Reading electricity delivered by client (Tariff 2) in 0,001 kWh. OBIS-ref: 1-0:2.8.2.255";
  n4["unit"] = "0,001 kWh";

  JsonObject n5 = array.createNestedObject();
  n5["name"] = "Tariff indicator electricity";
  n5["description"] = "Tariff indicator elec-tricity. The tariffin-dicator can also be used to switch tariffdependent loads e.g boilers. OBIS-ref: 0-0:96.14.0.255";
  n5["unit"] = "indicator";

  JsonObject n6 = array.createNestedObject();
  n6["name"] = "Actual electricity power delivered";
  n6["description"] = "Actual electricity power delivered (+P) in 1 Watt resolution. OBIS-ref: 1-0:1.7.0.255";
  n6["unit"] = "W";

  JsonObject n7 = array.createNestedObject();
  n7["name"] = "Actual elecrticity power received";
  n7["description"] = "Actual electricity power received (-P) in 1 Watt resolution. OBIS-ref: 1-0:2.7.0.255";
  n7["unit"] = "W";

  JsonObject n8 = array.createNestedObject();
  n8["name"] = "Instantaneous voltage L1";
  n8["description"] = "Instantaneous voltage L1 in V resolution. OBIS-ref: 1-0:32.7.0.255";
  n8["unit"] = "V";

  JsonObject n9 = array.createNestedObject();
  n9["name"] = "Number of power failures";
  n9["description"] = "Number of power failures in any phase. OBIS-ref: 1-0:96.7.21.255";
  n9["unit"] = "";

  JsonObject n10 = array.createNestedObject();
  n10["name"] = "Number of long power failures";
  n10["description"] = "Number of long power failures in any phase. OBIS-ref: 1-0:96.7.9.255";
  n10["unit"] = "";

  JsonObject n11 = array.createNestedObject();
  n11["name"] = "Number of voltage sags L1";
  n11["description"] = "Number of voltage sags in phase L1. OBIS-ref: 1-0:32.32.0.255";
  n11["unit"] = "";

  JsonObject n12 = array.createNestedObject();
  n12["name"] = "Number of voltage sags L2";
  n12["description"] = "Number of voltage sags in phase L2. OBIS-ref: 1-0:52.32.0.255";
  n12["unit"] = "";

  JsonObject n13 = array.createNestedObject();
  n13["name"] = "Number of voltage sags L3";
  n13["description"] = "Number of voltage sags in phase L3. OBIS-ref: 1-0:72.32.0.255";
  n13["unit"] = "";

  JsonObject n14 = array.createNestedObject();
  n14["name"] = "Number of voltage swells L1";
  n14["description"] = "Number of voltage swells in phase L1. OBIS-ref: 1-0:32.32.0.255";
  n14["unit"] = "";

  JsonObject n15 = array.createNestedObject();
  n15["name"] = "Number of voltage swells L2";
  n15["description"] = "Number of voltage swells in phase L2. OBIS-ref: 1-0:52.32.0.255";
  n15["unit"] = "";

  JsonObject n16 = array.createNestedObject();
  n16["name"] = "Number of voltage swells L3";
  n16["description"] = "Number of voltage swells in phase L3. OBIS-ref: 1-0:72.32.0.255";
  n16["unit"] = "";

  JsonObject n77 = array.createNestedObject();
  n77["name"] = "Instantaneous voltage L1";
  n77["description"] = "Instantaneous voltage L1 in V resolution. OBIS-ref: 1-0:32.7.0.255";
  n77["unit"] = "V";

  JsonObject n17 = array.createNestedObject();
  n17["name"] = "Instantaneous voltage L2";
  n17["description"] = "Instantaneous voltage L2 in V resolution. OBIS-ref: 1-0:52.7.0.255";
  n17["unit"] = "V";

  JsonObject n18 = array.createNestedObject();
  n18["name"] = "Instantaneous voltage L3";
  n18["description"] = "Instantaneous voltage L3 in V resolution. OBIS-ref: 1-0:72.7.0.255";
  n18["unit"] = "V";

  JsonObject n19 = array.createNestedObject();
  n19["name"] = "Instantaneous current L1";
  n19["description"] = "Instantaneous current L1 in A resolution. OBIS-ref: 1-0:31.7.0.255";
  n19["unit"] = "A";

  JsonObject n20 = array.createNestedObject();
  n20["name"] = "Instantaneous current L2";
  n20["description"] = "Instantaneous current L2 in A resolution. OBIS-ref: 1-0:51.7.0.255";
  n20["unit"] = "A";

  JsonObject n21 = array.createNestedObject();
  n21["name"] = "Instantaneous current L3";
  n21["description"] = "Instantaneous current L3 in A resolution. OBIS-ref: 1-0:71.7.0.255";
  n21["unit"] = "A";

  JsonObject n22 = array.createNestedObject();
  n22["name"] = "Instantaneous active power L1 (+P)";
  n22["description"] = "Instantaneous active power L1 (+P) in W resolution. OBIS-ref: 1-0:21.7.0.255";
  n22["unit"] = "KW";

  JsonObject n23 = array.createNestedObject();
  n23["name"] = "Instantaneous active power L2 (+P)";
  n23["description"] = "Instantaneous active power L2 (+P) in W resolution. OBIS-ref: 1-0:41.7.0.255";
  n23["unit"] = "KW";

  JsonObject n24 = array.createNestedObject();
  n24["name"] = "Instantaneous active power L3 (+P)";
  n24["description"] = "Instantaneous active power L3 (+P) in W resolution. OBIS-ref: 1-0:61.7.0.255";
  n24["unit"] = "KW";

  JsonObject n25 = array.createNestedObject();
  n25["name"] = "Instantaneous active power L1 (-P)";
  n25["description"] = "Instantaneous active power L1 (-P) in W resolution. OBIS-ref: 1-0:22.7.0.255";
  n25["unit"] = "KW";

  JsonObject n26 = array.createNestedObject();
  n26["name"] = "Instantaneous active power L2 (-P)";
  n26["description"] = "Instantaneous active power L2 (-P) in W resolution. OBIS-ref: 1-0:42.7.0.255";
  n26["unit"] = "KW";

  JsonObject n27 = array.createNestedObject();
  n27["name"] = "Instantaneous active power L3 (-P)";
  n27["description"] = "Instantaneous active power L3 (-P) in W resolution. OBIS-ref: 1-0:62.7.0.255";
  n27["unit"] = "KW";

  doc["measurements"] = array;
  doc["actuators"] = array2;
  
  String init;
  serializeJson(doc, init);

  return std::string(init.c_str());
}

void on_message(char* topic, uint8_t* payload, unsigned int length){
  receivedReaction = true;
  for(int i = 0; i < length; i++){
    Serial.print((char)payload[i]);
  }
  digitalWrite(PIN_BLUE, LOW);
  delay(200);
  clearLed();
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

  // ntp.begin();
  // ntp.forceUpdate();
  // delay(30000);
  // Serial.println("Time is: ");
  // Serial.println(ntp.getFormattedTime());


  mqtt.init(wifi.wifi);
  mqtt.setCallback(on_message);
  mqtt.connect();
  Serial.println("MQTT Connected");

  String topic = "node/" + String(SENSORID) + "/message";
  mqtt.subscribe(topic.c_str());
  
  Serial.println("Sending Init...");
  mqtt.publish("node/init", createInit().c_str());

  p1.init();

  digitalWrite(PIN_GREEN, LOW);
  delay(2000);
  clearLed();
}

std::list<std::string> split(std::string string, std::string deliminator){
  std::list<std::string> words = {};
  int begin = 0;
  int end = string.find(deliminator);
  while(end != std::string::npos){
    words.insert(words.end(), string.substr(begin, end - begin));
    begin = end + deliminator.length();
    end = string.find(deliminator, begin);
  }

  return words;
}

std::map<std::string, std::string> parseToMap(char buffer[P1_MAX_DATAGRAM_SIZE]){
  std::list<std::string> words = split(buffer, "\n");
  std::list<std::string> temp;
  std::map<std::string, std::string> map;

	for (auto const& word : words) {
		if (word.find(':') != std::string::npos) {
			temp.insert(temp.end(), word);
		}
	}

	for (auto const& word : temp) {
		std::string key = split(word, "(").front();
		std::string data = word.substr(key.length());

		if (data.find("(") == data.find_last_of("(")) {
			data = data.substr(data.find("(") + 1, data.find(")") - 1);
		}

    
    if (data.find('*') != std::string::npos){
      data = data.substr(0, data.find('*'));
    }

    map.insert({key, data});
	}

  return map;
}

std::string getMapData(std::map<std::string, std::string> map, std::string key){
  auto i = map.find(key);
  if(i != map.end()){
    return i->second;
  }
  return "null";
}

std::string parseTime(std::string time){
  //YYMMDDhhmmss 201225084523W
  //2021-05-20T06:35:31  2020-12-25T09:02:27
  std::string retval = "";
  retval += "20" + time.substr(0, 2) + "-";   //year
  retval += time.substr(2,2) + "-";           //month
  retval += time.substr(4,2) + "T";           //day
  retval += time.substr(6,2) + ":";           //hour
  retval += time.substr(8,2) + ":";           //minute
  retval += time.substr(10,2);                //seconds
  return retval;
}

char* parseToJson(char buffer[P1_MAX_DATAGRAM_SIZE]){
  DynamicJsonDocument doc(2048);
  DynamicJsonDocument arrayDoc(2048);
  JsonArray array = arrayDoc.to<JsonArray>();

  std::map<std::string, std::string> data = parseToMap(buffer);

  JsonObject n = array.createNestedObject();

  n["timestamp"] =                                    parseTime(getMapData(data, "0-0:1.0.0").c_str()).c_str();
  //n["timestamp"] =                                    ntp.getEpochTime();
  n["Electricity delivered to client (Tariff 1)"] =   atof(getMapData(data,"1-0:1.8.1").c_str());
  n["Electricity delivered to client (Tariff 2)"] =   atof(getMapData(data,"1-0:1.8.2").c_str());
  n["Electricity delivered by client (Tariff 1)"] =   atof(getMapData(data,"1-0:2.8.1").c_str());
  n["Electricity delivered by client (Tariff 2)"] =   atof(getMapData(data,"1-0:2.8.2").c_str());
  n["Tariff indicator electricity"] =                 atof(getMapData(data,"0-0:96.14.0").c_str());
  n["Actual electricity power delivered"] =           atof(getMapData(data,"1-0:1.7.0").c_str());
  n["Actual elecrticity power received"] =            atof(getMapData(data,"1-0:2.7.0").c_str());
  n["Number of power failures"] =                     atof(getMapData(data,"1-0:96.7.21").c_str());
  n["Number of long power failures"] =                atof(getMapData(data,"1-0:96.7.9").c_str());
  n["Number of voltage sags L1"] =                    atof(getMapData(data,"1-0:32.32.0").c_str());
  n["Number of voltage sags L2"] =                    atof(getMapData(data,"1-0:52.32.0").c_str());
  n["Number of voltage sags L3"] =                    atof(getMapData(data,"1-0:72.32.0").c_str());
  n["Number of voltage swells L1"] =                  atof(getMapData(data,"1-0:32.32.0").c_str());
  n["Number of voltage swells L2"] =                  atof(getMapData(data,"1-0:52.32.0").c_str());
  n["Number of voltage swells L3"] =                  atof(getMapData(data,"1-0:72.32.0").c_str());
  n["Instantaneous voltage L1"] =                     atof(getMapData(data,"1-0:32.7.0").c_str());
  n["Instantaneous voltage L2"] =                     atof(getMapData(data,"1-0:52.7.0").c_str());
  n["Instantaneous voltage L3"] =                     atof(getMapData(data,"1-0:72.7.0").c_str());
  n["Instantaneous current L1"] =                     atof(getMapData(data,"1-0:31.7.0").c_str());
  n["Instantaneous current L2"] =                     atof(getMapData(data,"1-0:51.7.0").c_str());
  n["Instantaneous current L3"] =                     atof(getMapData(data,"1-0:71.7.0").c_str());
  n["Instantaneous active power L1 (+P)"] =           atof(getMapData(data,"1-0:21.7.0").c_str());
  n["Instantaneous active power L2 (+P)"] =           atof(getMapData(data,"1-0:41.7.0").c_str());
  n["Instantaneous active power L3 (+P)"] =           atof(getMapData(data,"1-0:61.7.0").c_str());
  n["Instantaneous active power L1 (-P)"] =           atof(getMapData(data,"1-0:22.7.0").c_str());
  n["Instantaneous active power L2 (-P)"] =           atof(getMapData(data,"1-0:42.7.0").c_str());
  n["Instantaneous active power L3 (-P)"] =           atof(getMapData(data,"1-0:62.7.0").c_str());

  doc["id"] = SENSORID;
  doc["measurements"] = array;
  
  
  char retval[2048];

  serializeJson(doc, retval);

  return retval;

}

void loop()
{
    
  if(!mqtt.connected()){
    mqtt.connect();
  }

  if(p1.capture_p1() == true){

    if(ntp.getEpochTime() - lastTime > INTERVAL){
      lastTime = ntp.getEpochTime();
      digitalWrite(PIN_GREEN, LOW);
      delay(50);
      clearLed();

      mqtt.publish("node/data", parseToJson(p1.p1_buffer));
    }
  }
}