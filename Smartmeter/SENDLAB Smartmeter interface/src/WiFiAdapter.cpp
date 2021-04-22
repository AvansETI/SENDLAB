#include "WiFiAdapter.h"

void WifiAdapter::connect()
{
    WiFi.begin(WIFI_SSID, WIFI_PASS);
    WiFi.setAutoReconnect(true);
    WiFi.persistent(true);
};

void WifiAdapter::disconnect()
{
    WiFi.disconnect();
}

bool WifiAdapter::connected()
{
    return WiFi.isConnected();
}

IPAddress WifiAdapter::getIp()
{
    return WiFi.localIP();
}

wl_status_t WifiAdapter::status()
{
    return WiFi.status();
}