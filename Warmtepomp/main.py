import paho.mqtt.client as mqtt

localhost = mqtt.Client("heatpump")
sendlab = mqtt.Client()

def on_connect_localhost(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("$SYS/#")

# The callback for when a PUBLISH message is received from the server.
def on_message_localhost(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))


localhost.on_connect = on_connect_localhost
localhost.on_message = on_message_localhost

localhost.connect("localhost", 1183, 60, "10.0.0.0")