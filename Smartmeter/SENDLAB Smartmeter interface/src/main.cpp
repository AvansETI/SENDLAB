#include <P1Adapter.h>
#include <WiFiAdapter.h>
#include <MQTTAdapter.h>

WifiAdapter wifi;
MQTTAdapter mqtt = MQTTAdapter(wifi.wifi);
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

  mqtt.connect();
}

void loop()
{
  // put your main code here, to run repeatedly:
}