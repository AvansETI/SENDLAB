#include <MQTTAdapter.h>

MQTTAdapter::MQTTAdapter()
{
}

void MQTTAdapter::init(Client &client)
{
    mqtt.setClient(client);
    mqtt.setBufferSize(2048);
    
    mqtt.setServer(MQTT_SERVER_HOST, MQTT_SERVER_PORT);
}

void MQTTAdapter::connect()
{
    while (!mqtt.connected())
    {
        mqtt.connect(MQTT_ID);
        if (mqtt.connected())
        {
            // connected
            //publish("SENDLABSMARTMETERTEST","Connected!");
        }
    }
}

void MQTTAdapter::disconnect()
{
    mqtt.disconnect();
}

bool MQTTAdapter::connected()
{
    return mqtt.connected();
}

void MQTTAdapter::subscribe(char topic[])
{
    mqtt.subscribe(topic);
}

void MQTTAdapter::setCallback(std::function<void(char *, uint8_t *, unsigned int)> callback)
{
    mqtt.setCallback(callback);
}

void MQTTAdapter::publish(const char* topic, const char* message)
{
    mqtt.publish(topic, message);
}