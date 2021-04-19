#include <PubSubClient.h>
#include "MQTTAdapterI.cpp"

class MQTTAdapter : public MQTTAdapterI{
    
    public:
        MQTTAdapter(Client &client){
            PubSubClient mqtt;
            mqtt.setClient(client);
            mqtt.setBufferSize(2048);
            
        }

        void connect(){

        }

};