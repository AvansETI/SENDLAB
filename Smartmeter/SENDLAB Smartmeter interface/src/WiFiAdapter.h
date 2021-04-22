#pragma once
#include <ESP8266WiFi.h>
#include <Config.h>

class WifiAdapter
{

    public:
        WiFiClient wifi;
        void connect();
        void disconnect();
        bool connected();
        wl_status_t status();
        IPAddress getIp();
};