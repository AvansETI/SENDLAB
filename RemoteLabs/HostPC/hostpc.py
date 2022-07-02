#!/usr/bin/python3

# SENDLAB - RemoteLabs HostPC for Solar and Battery experimental setup
#
#

import os
import socketio #  pip3 install python-socketio==4.6.1, pip3 install requests
import base64

from remotelabs.remotelabs_hostpc import RemoteLabsHostPC

# SEND events
#     - ActuatorDefinitionMessage as "actuator_definition" |
#     - BulkSignalsMessage as "bulk_signals" |
#     - ConfigDefinitionMessage as "config_definition" |
#     - ConnectParamsMessage as "remote_labs_connect_params" |
#     - ErrorMessage as "remote_labs_error" |
#     - ExperimentResultsMessage as "experiment_results" |
#     - FreeMessage as "free_message" |
#     - IsBrokenMessage as "is_broken" |
#     - SendStatusMessage as "send_status" |
#     - SignalDefinitionMessage as "signal_definition" |
#     - SignalValuesMessage as "signal_values"

# Credentials
# 7:X9fg2WYtJlRFe4CdGZAr
# MauriceTest:X9fg2WYtJlRFe4CdGZAr

# namespace name: hostpc
# meta data velden: als headers bij connection attempt of los event te sturen. => Dat is de authorizatie stap
# Testen: STAP 1
# - Bij connectie krijg ik een STATUS berich en verwacht de server een STATUS terug.
# - SEND_STATUS moet ik sturen
# - Hierna alles opzetten.
# STAP 2:
# 

# https://editor.swagger.io/
# https://gitlab.com/wolfpackit/projects/tu-e-electrical-engineering/remote-labs/remote-labs-mock-hostpc/-/blob/develop/src/main/resources/openapi/socketio.yaml

os.chdir(os.path.dirname(os.path.abspath(__file__)))

# Create the socket.io client
sio = socketio.Client(logger=True, engineio_logger=True, ssl_verify=False)

hostpc = RemoteLabsHostPC(sio)
hostpc.start()

@sio.event
def connect():
    hostpc.event_connect()
    print('connection established')

@sio.event
def connect_error(data):
    hostpc.event_error(data)
    print("The connection failed!")

@sio.event
def disconnect():
    hostpc.event_disconnect()
    print('disconnected from server')

@sio.event(namespace='/hostpc')
def get_status(data):
    """When the status is requested from the server, the hostpc returns the status."""
    hostpc.event_get_status(data)

@sio.event(namespace='/hostpc')
def build_experiment(data):
    hostpc.event_build_experiment(data)

@sio.event(namespace='/hostpc')
def actuator_update(data):
    hostpc.event_actuator_update(data)

@sio.event(namespace='/hostpc')
def change_signal_mode(data):
    hostpc.event_change_signal_mode(data)

@sio.event(namespace='/hostpc')
def free_message(data):
    hostpc.event_free_message(data)

@sio.event(namespace='/hostpc')
def get_results(data):
    hostpc.event_get_results(data)

@sio.event(namespace='/hostpc')
def reset(data):
    hostpc.event_reset(data)

@sio.event(namespace='/hostpc')
def signal_request(data):
    hostpc.event_signal_request(data)

@sio.event(namespace='/hostpc')
def start_experiment(data):
    hostpc.event_start_experiment(data)

@sio.event(namespace='/hostpc')
def start_recording(data):
    hostpc.event_start_recording(data)

@sio.event(namespace='/hostpc')
def stop_experiment(data):
    hostpc.event_stop_experiment(data)

@sio.event(namespace='/hostpc')
def stop_recording(data):
    hostpc.event_stop_recording(data)

sio.connect(hostpc.config["socketio_server"], {
"Authorization": "Basic " + hostpc.get_encoded_login(),
"RemoteLabs-Type": "HostPC"
})

print('my sid is', sio.sid)

sio.wait()

