#pragma once
#include <PubSubClient.h>
#include <Config.h>

class MQTTAdapter {
    public:
        PubSubClient mqtt;
        MQTTAdapter(Client &client);
        void connect();
        void disconnect();
        bool connected();
        void subscribe(char topic[]);
        void setCallback(std::function<void(char *, uint8_t *, unsigned int)> callback);
        void publish(char topic[], char message[]);
};