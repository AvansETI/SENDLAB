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

class RemoteLabsHostPC(threading.Thread):
    """This class implements the logic for the host PC. It controls the experiments.
       From the socket.io this class can be controlled and create the required
       interaction."""

    def __init__(self, sio, config_file="remotelabs_hostpc.json"):
        """Create instance of the SmartNetwork class."""
        super(RemoteLabsHostPC, self).__init__()

        # Socket io instance to communicate with the RemoteLabs server
        self.sio = sio

        # When this flag is set, the node will stop and close
        self.terminate_flag = threading.Event()

        # Debugging on or off!
        self.debug = False

        # Experiment status variables
        self.experiment_id = -1
        self.status = "idle"
        self.status_info = ""

        # Read the config file
        try:
            config_json = open(config_file, "r").read()
            self.config = json.loads(config_json)

        except Exception as e:
            print("Cannot read the config.json (" + config_file + ") file, which is required for execution.")
            quit()

    def get_encoded_login(self):
        """Encodes the login credential and returns a base64 string."""
        unencoded = self.config["username"] + ":" + self.config["password"]
        encoded = base64.b64encode(unencoded.encode('utf-8')).decode('utf-8')
        return str(encoded)

    def _set_experiment_id(self, experiment_id):
        self.experiment_id = experiment_id

    def _set_status(self, status, info):
        self.status = status
        self.status_info = info

    def get_status(self):
        return { "experiment_id": self.experiment_id, "status": self.status, "info": self.status_info }

    def event_connect(self):
        pass

    def event_disconnect(self):
        pass

    def event_error(self, data):
        pass

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
                time.sleep(5)
                print("asdhkjhas")
                pass # Do things here!
                
            except Exception as e:
                raise e

            time.sleep(1) # Need to be smaller when we do real stuff!!

        self.mqtt.loop_stop() # Stop the loop thread of MQTT
        print("Smart network stopped")

    def __str__(self):
        """Default Python method to convert the class to a string represntation"""
        return 'SmartNetwork'

    def __repr__(self):
        """Default Python method to convert the class to a string represntation"""
        return 'SmartNetwork'
