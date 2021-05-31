from datetime import datetime
import paho.mqtt.client as mqtt
import json
import time

sensorId = "SENDLAB_WARMTEPOMP"

localhost = mqtt.Client("test")
sendlab = mqtt.Client()

def on_connect_sendlab(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("node/" + sensorId + "/message", qos = 0)

def on_connect_localhost(client, userdata, flags, rc):
    print("Connected with result code "+str(rc))
    print("Pi Connected at: " + datetime.now().isoformat())

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
    client.subscribe("homie/daikin-heatingunit/domestichotwatertank/2-operation-operationmode")
    client.subscribe("homie/daikin-heatingunit/domestichotwatertank/2-consumption")


# The callback for when a PUBLISH message is received from the server.
def on_message_sendlab(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))
    

def on_message_localhost(client, userdata, msg):
    #print(msg.topic+" "+str(msg.payload))
    topic = msg.topic[25:]
    category = topic[:12]
    value = topic[15:]
    
    if category == "spaceheating":
        if value == "operation-targettemperature":
            opTargetTemp = msg.payload
        if value == "operation-operationmode":
            opModeSpaceHeating = msg.payload
        if value == "operation-roomtemperatureauto":
            opRoomTempAuto = msg.payload
        if value == "operation-roomtemperaturecooling":
            opRoomTempCooling = msg.payload
        if value == "operation-roomtemperatureheating":
            opRoomTempHeating = msg.payload
        if value == "sensor-indoortemperature":
            sensIndoorTemp = msg.payload
        if value == "sensor-outdoortemperature":
            sensOutdoorTemp = msg.payload
        if value == "consumption":
            spaceheatingConsumption = msg.payload
         
    if category == "domestichotwatertank":
        if value == "sensor-tanktemperature":
            sensorTankTemp = msg.payload
        if value == "operation-targettemperature":
            opTankTargetTemp = msg.payload
        if value == "operation-operationmode":
            opModeWaterTank = msg.payload
        if value == "consumption":
            waterTankConsumption = msg.payload

sensorInit = {
    "mode": 0,
    "type": "sensor",
    "id": sensorId,
    "name": "SENDLAB Warmtepomp",
    "measurements": [
        {
            "name": "Spaceheating target temperature",
            "description": "Target temperature for SENDLAB heating/cooling",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating operation mode",
            "description": "Spaceheating operation mode",
            "unit": "enum"
        }, {
            "name": "Spaceheating room temperature auto",
            "description": "Spaceheating set temperature for auto mode",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating room temperature cooling",
            "description": "Spaceheating set temperature for cooling mode",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating room temperature heating",
            "description": "Spaceheating set temperature for heating mode",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating indoor temperature",
            "description": "Spaceheating indoor temperature sensor readings",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating outdoor temperature",
            "description": "Spaceheating outdoor temperature sensor readings",
            "unit": "degree of Celsius"
        }, {
            "name": "Spaceheating electricity consumption",
            "description": "Spaceheating eclectricity consumption from last 2h",
            "unit": "kW"
        }, {
            "name": "Watertank temperature",
            "description": "Watertank temperature",
            "unit": "degree of Celsius"
        }, {
            "name": "Watertank target temperature",
            "description": "Watertank target temperature",
            "unit": "degree of Celsius"
        }, {
            "name": "Watertank operation mode",
            "description": "Watertank operation mode",
            "unit": "enum"
        }, {
            "name": "Watertank electricity consumption",
            "description": "Watertank eclectricity consumption from last 2h",
            "unit": "kW"
        }
    ],
    "actuators": []
}

localhost.on_connect = on_connect_localhost
localhost.on_message = on_message_localhost

sendlab.on_connect = on_connect_sendlab
sendlab.on_message = on_message_sendlab

localhost.connect("localhost", 1883, 60)

#sendlab.username_pw_set("server", password="servernode")
sendlab.username_pw_set("node", password="smartmeternode")
sendlab.connect("sendlab.nl", 11884, 60)

print("Sending Init message")
#sendlab.publish("node/init", json.dumps(sensorInit))

timestamp = datetime.now()
while( 1 ):
    localhost.loop()
    sendlab.loop()

    timediff = datetime.now() - timestamp
    if ( timediff > 60 ):

        data = {
            "id": sensorId,
            "measurements": {
                "timestamp": datetime.now().isoformat()
            }
        }

        print(datetime.now().isoformat())
        timestamp = datetime.now()