#include <ESP8266WiFi.h>
#include <Config.h>

class WifiAdapter
{

    WiFiClient wifi;

    void connect();
    void disconnect();
    bool connected();
};