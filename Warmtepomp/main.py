from datetime import datetime
import paho.mqtt.client as mqtt
import json
import math

sensorId = "SENDLAB_WARMTEPOMP"

spaceOpMode = ""
spaceRoomTempAuto = 0
spaceRoomTempCooling = 0
spaceRoomTempHeating = 0
spaceSensIndoorTemp = 0
spaceSensOutdoorTemp = 0
spaceheatingConsumption = ""
sensorTankTemp = 0
opTankTargetTemp = 0
opModeWaterTank = ""
waterTankConsumption = ""

localhost = mqtt.Client("test")
sendlab = mqtt.Client()

def on_connect_sendlab(client, userdata, flags, rc):
    print("Connected To Sendlab")

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("node/" + sensorId + "/message", qos = 0)

def on_connect_localhost(client, userdata, flags, rc):
    print("Connected To Localhost")

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
    
def handleConsumptionSpaceheating(payload):
    obj = json.loads(payload)["Electrical"]
    heating = obj["Heating"]
    cooling = obj["Cooling"]
    data = {
            "id": sensorId,
            "timestamp": datetime.utcnow().isoformat(),
            "measurements": [{
                "timestamp": datetime.utcnow().isoformat(),
                "cons_cool_sh": getLastConsumption(cooling["D"]),
                "cons_heat_sh": getLastConsumption(heating["D"])
            }]
        }

    sendlab.publish("node/data", json.dumps(data))
    print(json.dumps(data))

def handleConsumptionWatertank(payload):
    obj = json.loads(payload)["Electrical"]
    heating = obj["Heating"]
    data = {
            "id": sensorId,
            "timestamp": datetime.utcnow().isoformat(),
            "measurements": [{
                "timestamp": datetime.utcnow().isoformat(),
                "consumption_tank": getLastConsumption(heating["D"])
            }]
        }

    sendlab.publish("node/data", json.dumps(data))
    print(json.dumps(data))
    

def getLastConsumption(array):
    last = array[0]
    for e in array:
        if e == None:
            return last
        last = e

def on_message_localhost(client, userdata, msg):
    #print(msg.topic+" "+str(msg.payload))
    topic = msg.topic[25:]
    category = topic[:12]
    value = topic[15:]

    if category == "spaceheating":
        if value == "operation-targettemperature":
            global spaceTargetTemp
            spaceTargetTemp = float(msg.payload)
        if value == "operation-operationmode":
            global spaceOpMode
            spaceOpMode = msg.payload
        if value == "operation-roomtemperatureauto":
            global spaceRoomTempAuto
            spaceRoomTempAuto = float(msg.payload)
        if value == "operation-roomtemperaturecooling":
            global spaceRoomTempCooling
            spaceRoomTempCooling = float(msg.payload)
        if value == "operation-roomtemperatureheating":
            global spaceRoomTempHeating
            spaceRoomTempHeating = float(msg.payload)
        if value == "sensor-indoortemperature":
            global spaceSensIndoorTemp
            spaceSensIndoorTemp = float(msg.payload)
        if value == "sensor-outdoortemperature":
            global spaceSensOutdoorTemp
            spaceSensOutdoorTemp = float(msg.payload)
        if value == "consumption":
            handleConsumptionSpaceheating(msg.payload)
         
    if category == "domestichotwatertank":
        if value == "sensor-tanktemperature":
            global sensorTankTemp
            sensorTankTemp = float(msg.payload)
        if value == "operation-targettemperature":
            global opTankTargetTemp
            opTankTargetTemp = float(msg.payload)
        if value == "operation-operationmode":
            global opModeWaterTank
            opModeWaterTank = msg.payload
        if value == "consumption":
            handleConsumptionWatertank(msg.payload)

sensorInit = {
    "mode": 0,
    "type": "sensor",
    "id": sensorId,
    "name": "SENDLAB Warmtepomp",
    "measurements": [
        {
            "name": "targTemp_sh",
            "description": "Target temperature for SENDLAB heating/cooling",
            "unit": "degree of Celsius"
        }, {
            "name": "opMode_sh",
            "description": "Spaceheating operation mode",
            "unit": "enum"
        }, {
            "name": "roomTempAuto_sh",
            "description": "Spaceheating set temperature for auto mode",
            "unit": "degree of Celsius"
        }, {
            "name": "roomTempCool_sh",
            "description": "Spaceheating set temperature for cooling mode",
            "unit": "degree of Celsius"
        }, {
            "name": "roomTempHeat_sh",
            "description": "Spaceheating set temperature for heating mode",
            "unit": "degree of Celsius"
        }, {
            "name": "indoorTemp_sh",
            "description": "Spaceheating indoor temperature sensor readings",
            "unit": "degree of Celsius"
        }, {
            "name": "outdoorTemp_sh",
            "description": "Spaceheating outdoor temperature sensor readings",
            "unit": "degree of Celsius"
        }, {
            "name": "cons_cool_sh",
            "description": "Spaceheating Cooling electricity consumption from last 2h",
            "unit": "kW"
        }, {
            "name": "cons_heat_sh",
            "description": "Spaceheating Heating electricity consumption from last 2h",
            "unit": "kW"
        }, {
            "name": "temp_tank",
            "description": "Watertank temperature",
            "unit": "degree of Celsius"
        }, {
            "name": "targTemp_tank",
            "description": "Watertank target temperature",
            "unit": "degree of Celsius"
        }, {
            "name": "opMode_tank",
            "description": "Watertank operation mode",
            "unit": "enum"
        }, {
            "name": "consumption_tank",
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
sendlab.publish("node/init", json.dumps(sensorInit))

timestamp = datetime.now()
while( 1 ):
    localhost.loop()
    sendlab.loop()

    timediff = datetime.now() - timestamp
    if ( timediff.seconds > 60 ):

        data = {
            "id": sensorId,
            "timestamp": datetime.utcnow().isoformat(),
            "measurements": [{
                "timestamp": datetime.utcnow().isoformat(),
                "targTemp_sh": float(spaceTargetTemp),
                "opMode_sh": spaceOpMode,
                "roomTempAuto_sh": float(spaceRoomTempAuto),
                "roomTempCool_sh": float(spaceRoomTempCooling),
                "roomTempHeat_sh": float(spaceRoomTempHeating),
                "indoorTemp_sh": float(spaceSensIndoorTemp),
                "outdoorTemp_sh": float(spaceSensOutdoorTemp),
                "temp_tank": float(sensorTankTemp),
                "targTemp_tank": float(opTankTargetTemp),
                "opMode_tank": opModeWaterTank
            }]
        }

        sendlab.publish("node/data", json.dumps(data))
        print(json.dumps(data))
        timestamp = datetime.now()