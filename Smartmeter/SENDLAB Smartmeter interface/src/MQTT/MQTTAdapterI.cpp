class MQTTAdapterI {
    public:
        virtual void connect();
        virtual void disconnect();
        virtual bool connected();
        virtual void Subscribe();
        virtual void publish(char topic[], char message[]);
};