#!/usr/bin/python
#
# Copyright (c) 2020 packom.net
#
# Sample python app using pyMeterBus to control M-Bus Master Hat
#
# pyMeterBus can be found here: https://github.com/ganehag/pyMeterBus
#
# More about the M-Bus Master hat: https://www.packom.net/m-bus-master-hat/
#

import os, time, serial, meterbus
import RPi.GPIO as GPIO
import paho.mqtt.client as mqtt
import json

sensorId = "SENDLAB_Warmtemeter"

#MQTT Functions and data
def on_connect_sendlab(client, userdata, flags, rc):
    print("Connected To Sendlab")

    # Subscribing in on_connect() means that if we lose the connection and
    # reconnect then subscriptions will be renewed.
    client.subscribe("node/" + sensorId + "/message", qos = 0)

def on_message_sendlab(client, userdata, msg):
    print(msg.topic+" "+str(msg.payload))

sensorInit = {
    "mode": 0,
    "type": "sensor",
    "id": sensorId,
    "name": "SENDLAB Warmtemeter",
    "measurements": [
        {
            "name": "heat_energy",
            "description": "Heat energy of heatmeter",
            "unit": "W/Hour"
        },{
            "name": "cool_energy",
            "description": "Cooling energy of heatmeter",
            "unit": "W/Hour"
        },{
            "name": "energy_E8",
            "description": "Marked as energy E8 - (m3*T1)",
            "unit": "M3"
        },{
            "name": "vol_d1_t0",
            "description": "Volume of device 1 with tariff 0",
            "unit": "M3"
        },{
            "name": "vol_d2_t0",
            "description": "Volume of device 2 with tariff 0",
            "unit": "M3"
        },{
            "name": "vol_d0_t1",
            "description": "Volume of device 0 with tariff 1",
            "unit": "M3"
        },{
            "name": "vol_d0_t2",
            "description": "Volume of device 0 with tariff 2",
            "unit": "M3"
        },{
            "name": "vol_d0_t3",
            "description": "Volume of device 0 with tariff 3",
            "unit": "M3"
        },{
            "name": "op_on_time",
            "description": "On time in operation",
            "unit": "Seconds"
        },{
            "name": "err_on_time",
            "description": "On time in error state",
            "unit": "Seconds"
        },{
            "name": "flow_temp_in",
            "description": "Flow temp inlet as T1",
            "unit": "degree of celsius"
        },{
            "name": "flow_temp_out",
            "description": "Flow temp outlet as T2",
            "unit": "degree of celsius"
        },{
            "name": "act_pow",
            "description": "Actual power",
            "unit": "W"
        },{
            "name": "max_pow",
            "description": "Max power this month",
            "unit": "W"
        },{
            "name": "act_vol_flow",
            "description": "Actual volume flow",
            "unit": "M3_H"
        },{
            "name": "max_vol_flow",
            "description": "Maximum volume flow this month",
            "unit": "M3_H"
        },{
            "name": "heat_energy_targ",
            "description": "Heat energy target of heatmeter",
            "unit": "W/Hour"
        },{
            "name": "cool_energy_targ",
            "description": "Cooling energy target of heatmeter",
            "unit": "W/Hour"
        },{
            "name": "vol_d1_t0_targ",
            "description": "Volume target of device 1 with tariff 0",
            "unit": "M3"
        },{
            "name": "vol_d2_t0_targ",
            "description": "Volume target of device 2 with tariff 0",
            "unit": "M3"
        },{
            "name": "vol_d0_t1_targ",
            "description": "Volume target of device 0 with tariff 1",
            "unit": "M3"
        },{
            "name": "vol_d0_t2_targ",
            "description": "Volume target of device 0 with tariff 2",
            "unit": "M3"
        },{
            "name": "vol_d0_t3_targ",
            "description": "Volume target of device 0 with tariff 3",
            "unit": "M3"
        },{
            "name": "max_pow_targ",
            "description": "Max power this year target",
            "unit": "W"
        },{
            "name": "max_vol_flow_targ",
            "description": "Maximum volume flow this year target",
            "unit": "M3_H"
        }
    ],
    "actuators": []
}

# Address of slave to read from - change this
slave_address = 1

# M-Bus Master Hat constants
mbus_master_product = 'M-Bus Master'
mbus_gpio_bcm = 26  # BCM pin number, not wiringPi or physical pin
serial_dev = '/dev/ttyAMA0'
baud_rate = 2400

# Check we have an M-Bus Master Hat installed
got_hat = False
if os.path.isfile('/proc/device-tree/hat/product'):
  namef = open('/proc/device-tree/hat/product')
  if namef.read().replace('\x00', '') == mbus_master_product:
    print('Found M-Bus Master Hat version ' + open('/proc/device-tree/hat/product_ver').read())
    got_hat = True

if got_hat == False:
  print('Warning: No M-Bus Master hat found')
  exit(1)

# Initialize GPIO handling
GPIO.setmode(GPIO.BCM)
GPIO.setup(mbus_gpio_bcm, GPIO.OUT)  # Strictly this is unnecessary, but belt and braces
GPIO.output(mbus_gpio_bcm, GPIO.LOW)

# Turn M-Bus on
GPIO.output(mbus_gpio_bcm, GPIO.HIGH)
time.sleep(.1)  # Pause briefly to allow the bus time to power up

# Connect To SENDLAB MQTT
sendlab = mqtt.Client()

sendlab.on_connect = on_connect_sendlab
sendlab.on_message = on_message_sendlab

#sendlab.username_pw_set("server", password="servernode")
sendlab.username_pw_set("node", password="smartmeternode")
sendlab.connect("sendlab.nl", 11884, 60)

print("Sending Init message")
sendlab.publish("node/init", json.dumps(sensorInit))

# Read data from slave
ser = serial.Serial(serial_dev, baud_rate, 8, 'E', 1, 0.5)
try:
  meterbus.send_ping_frame(ser, slave_address)
  frame = meterbus.load(meterbus.recv_frame(ser, 1))
  assert isinstance(frame, meterbus.TelegramACK)
  meterbus.send_request_frame(ser, slave_address)
  frame = meterbus.load(meterbus.recv_frame(ser, meterbus.FRAME_DATA_LENGTH))
  assert isinstance(frame, meterbus.TelegramLong)

  obj = json.loads(frame.to_JSON())
  records = obj["body"]["records"]
  print(records)
except:
  print('Failed to read data from slave at address %d' % slave_address)

# Turn off M-Bus
GPIO.output(mbus_gpio_bcm, GPIO.HIGH)
GPIO.cleanup()