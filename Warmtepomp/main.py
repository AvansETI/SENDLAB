import paho.mqtt.client as mqtt

localhost = mqtt.Client(transport="websockets")
sendlab = mqtt.Client()

def on_connect(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("#")

# The callback for when a PUBLISH message is received from the server.
def on_message(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))


localhost.on_connect = on_connect
localhost.on_message = on_message

localhost.connect("localhost", 1183, 60)
# sendlab.username_pw_set("server", password="servernode")
# sendlab.connect("sendlab.nl", 11884, 60)

while( 1 ):
    localhost.loop()