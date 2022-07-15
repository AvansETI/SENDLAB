#!/usr/bin/python3

# SENDLAB - RemoteLabs HostPC for Solar and Battery experimental setup
#
#

import os
import socketio #  pip3 install python-socketio==4.6.1, pip3 install requests
import base64

from remotelabs.remotelabs_hostpc import RemoteLabsHostPC

# REMOVE AND ADD VALID SSL CERTICATE
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning
requests.packages.urllib3.disable_warnings(InsecureRequestWarning)


# https://editor.swagger.io/
# https://gitlab.com/wolfpackit/projects/tu-e-electrical-engineering/remote-labs/remote-labs-mock-hostpc/-/blob/develop/src/main/resources/openapi/socketio.yaml

# Go to the working directory of this python script
os.chdir(os.path.dirname(os.path.abspath(__file__)))

# Create the socket.io client
sio = socketio.Client(logger=True, engineio_logger=True, ssl_verify=False)


hostpc = RemoteLabsHostPC(sio)
hostpc.debug = True
hostpc.simulation = False
hostpc.start()

@sio.event
def connect():
    hostpc.event_connect()
    print('connection established')
    # TODO: Add the authorization in here!
    hostpc.send_authoriztion_message()

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

# TODO: When the socket.io server is not available, it stops. It should start to connect
#       Reconnection is working when it was first connected.
sio.connect(hostpc.config["socketio_server"], {
"Authorization": "Basic " + hostpc.get_encoded_login(),
"RemoteLabs-Type": "HostPC"
})

print('my sid is', sio.sid)

sio.wait()

