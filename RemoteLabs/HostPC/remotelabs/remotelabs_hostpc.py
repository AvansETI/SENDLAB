"""
@author	    Maurice Snoeren
@contact	macsnoeren@gmail.com
@copyright	2022 (c) Avans Hogeschool
@license	GNUv3
@date	    02-07-2022
@version	0.1
@python     >3.4
@info       
"""

from distutils.log import debug
import time, threading, json, base64
from enum import Enum

class FSMStates(Enum):
    """Creating the FSM states of the host PC running the experiments."""
    START                = 1
    INIT                 = 2
    PREPARE_VM           = 3
    READY_FOR_EXPERIMENT = 4
    BUILD_EXPERIMENT     = 5
    BUILDING             = 6
    START_EXPERIMENT     = 7
    RUN_EXPERIMENT       = 8
    STOP_EXPERIMENT      = 9

class RemoteLabsHostPC(threading.Thread):
    """This class implements the logic for the host PC. It controls the experiments.
       From the socket.io this class can be controlled and create the required
       interaction."""

    def __init__(self, sio, config_file="remotelabs_hostpc.json"):
        """Create instance of the class."""
        super(RemoteLabsHostPC, self).__init__()

        # When this flag is set, the node will stop and close
        self.terminate_flag = threading.Event()

        # Debugging on or off!
        self.debug = False

        # Socket io instance to communicate with the RemoteLabs server
        self.sio = sio

        # Experiment status variables
        self.experiment_id = -1
        self.status = "idle"
        self.status_info = ""

        self.fsm_state = FSMStates.START

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
        if ( self.fsm_state == FSMStates.START ):
            self.fsm_state = FSMStates.INIT

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

    def state_start(self):
        """START: The first state of the FSM of the host PC."""
        self.debug_print("FSM START")

    def state_init(self):
        """INIT: The initialization state that is used to get everything up and running."""
        self.debug_print("FSM INIT")
        self.sio.emit("free_message", { "test": "test"}, "/hostpc") # For testeing
        self.fsm_state = FSMStates.PREPARE_VM

    def clean_up_vm(self):
        """This methods checks if a VM is currently running. If so, it stops the VM and
           cleans it up."""
        pass

    def create_new_vm(self):
        """This methods create and run a new VM to be used for the experiment."""
        pass

    def test_current_vm(self):
        """This methods test the running VM whether it can be used for the experiment."""
        return True

    def state_prepare_vm(self):
        """If a VM is already running, make sure it is stopped appropiately and cleaned up. When this is finished,
           a new VM can be spinned up and tested. When the test is sent successfull, the state is considered done.
           Interfaceing with VirtualBox is done with the CLI VBoxManage application. It works on both Windows as
           Linux."""
        self.debug_print("FSM PREPARE VM")
        self.clean_up_vm()
        self.create_new_vm()
        if ( self.test_current_vm() ):
            self.fsm_state = FSMStates.READY_FOR_EXPERIMENT

    def state_ready_for_experiment(self):
        """Waits for the get status event from the server and sends the new status back to the server. When it get the message
           'build_experiment' from the server, it will go to the next state 'BUILD_EXPERIMENT'"""
        pass

    def state_build_experiment(self):
        """Sends a new status and requests the experiment files from the server. If done, it goes to the next state 'building'"""
        pass

    def state_building(self):
        """Creates a value for the student to login into the system VM. It creates the data points for the experiment, like
           vm_login_code, progress, ... It sends out 'ready_to_start' status and goes to the next state 'start_experiment'."""
        pass

    def state_start_experiment(self):
        """"""
        pass

    def state_run_experiment(self):
        pass

    def state_stop_experiment(self):
        pass

    def run(self):
        """The main loop of the thread."""
        while not self.terminate_flag.is_set():  # Check whether the thread needs to be closed
            try:
                if ( self.fsm_state == FSMStates.START ):
                    self.state_start()

                elif (self.fsm_state == FSMStates.INIT ):
                    self.state_init()

                elif (self.fsm_state == FSMStates.PREPARE_VM ):
                    self.state_prepare_vm()

                elif (self.fsm_state == FSMStates.READY_FOR_EXPERIMENT ):
                    self.state_ready_for_experiment()

                elif (self.fsm_state == FSMStates.BUILD_EXPERIMENT ):
                    self.state_build_experiment()

                elif (self.fsm_state == FSMStates.BUILDING ):
                    self.state_building()

                elif (self.fsm_state == FSMStates.START_EXPERIMENT ):
                    self.state_start_experiment()

                elif (self.fsm_state == FSMStates.RUN_EXPERIMENT ):
                    self.state_run_experiment()

                elif (self.fsm_state == FSMStates.STOP_EXPERIMENT ):
                    self.state_stop_experiment()

                else:
                    self.debug_print("HostPC Thread: FSM State '" + self.fsm_state + "' unknown.")
                
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
