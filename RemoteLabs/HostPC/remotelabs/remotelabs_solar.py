"""
@author	    Maurice Snoeren
@contact	macsnoeren@gmail.com
@copyright	2022 (c) Avans Hogeschool
@license	GNUv3
@date	    02-07-2022
@version	0.1
@info       
"""

import time, threading
import json
import socketio #  pip3 install python-socketio==4.6.1, pip3 install requests
import base64

from remotelabs_hostpc import RemoteLabsHostPC

class RemoteLabsSolar(RemoteLabsHostPC):
    """This class implements the logic for the host PC of the solar experimant.
       It controls the experiments. From the socket.io this class can be controlled 
       and create the required interaction."""

    def __init__(self, sio, config_file="remotelabs_hostpc.json"):
        """Create instance of the class."""
        super(RemoteLabsSolar, self).__init__()

        # Do things concerning solar

    def event_get_status(self, data):
        self.sio.emit("send_status", self.get_status(), "/hostpc")

    def event_build_experiment(self, data):
        print("build_experiment: " + str(data))

    def event_actuator_update(self, data):
        print("actuator_update: " + str(data))

    def event_change_signal_mode(self, data):
        print("change_signal_mode: " + str(data))

    def event_free_message(self, data):
        print("free_message: " + str(data))

    def event_get_results(self, data):
        print("get_results: " + str(data))

    def event_reset(self, data):
        print("reset: " + str(data))

    def event_signal_request(self, data):
        print("signal_request: " + str(data))

    def event_start_experiment(self, data):
        print("start_experiment: " + str(data))

    def event_start_recording(self, data):
        print("start_recording: " + str(data))

    def event_stop_experiment(self, data):
        print("stop_experiment: " + str(data))

    def event_stop_recording(self, data):
        print("stop_recording: " + str(data))

    def debug_print(self, message):
        """When the debug flag is set to True, all debug messages are printed in the console."""
        if self.debug:
            print("DEBUG: " + message)

    def stop(self):
        """Stop the smart network."""
        self.terminate_flag.set()

    def run(self):
        """The main loop of the thread."""
        while not self.terminate_flag.is_set():  # Check whether the thread needs to be closed
            try:
                pass # Do things here!
                
            except Exception as e:
                raise e

            time.sleep(1) # Need to be smaller when we do real stuff!!

        self.mqtt.loop_stop() # Stop the loop thread of MQTT
        print("Smart network stopped")

    def __str__(self):
        """Default Python method to convert the class to a string represntation"""
        return str(__class__)

    def __repr__(self):
        """Default Python method to convert the class to a string represntation"""
        return str(__class__)
