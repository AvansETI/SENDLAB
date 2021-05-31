import paho.mqtt.client as mqtt
import datetime
import time

localhost = mqtt.Client("test")
sendlab = mqtt.Client()

def on_connect_sendlab(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("#")

def on_connect_localhost(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    print("Pi Connected at: " + time.asctime(time.localtime()))

    client.subscribe("homie/daikin-heatingunit/spaceheating/1-operation-targettemperature")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-sensor-indoortemperature")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-sensor-outdoortemperature")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-operation-roomtemperatureauto")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-operation-roomtemperaturecooling")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-operation-roomtemperatureheating")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-consumption")
    client.subscribe("homie/daikin-heatingunit/spaceheating/1-operation-operationmode")

    client.subscribe("homie/daikin-heatingunit/domestichotwatertank/2-sensor-tanktemperature")
    client.subscribe("homie/daikin-heatingunit/domestichotwatertank/2-operation-targettemperature")
    client.subscribe("homie/daikin-heatingunit/domestichotwatertank/2-consumption")


# The callback for when a PUBLISH message is received from the server.
def on_message_sendlab(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    

def on_message_localhost(client, userdata, msg):
    #print(msg.topic+" "+str(msg.payload))
    topic = msg.topic[24]
    print(topic)
    

localhost.on_connect = on_connect_localhost
localhost.on_message = on_message_localhost

sendlab.on_connect = on_connect_sendlab
sendlab.on_message = on_message_sendlab

localhost.connect("localhost", 1883, 60)

#sendlab.username_pw_set("server", password="servernode")
sendlab.username_pw_set("node", password="smartmeternode")
sendlab.connect("sendlab.nl", 11884, 60)


timestamp = time.time()
while( 1 ):
    localhost.loop()
    sendlab.loop()

    timediff = time.time() - timestamp
    if ( timediff > 60 ):
        print(time.time())
        timestamp = time.time()