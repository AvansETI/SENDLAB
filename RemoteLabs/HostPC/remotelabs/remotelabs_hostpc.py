"""
@author	    Maurice Snoeren
@contact	macsnoeren@gmail.com
@copyright	2022 (c) Avans Hogeschool
@license	GNUv3
@date	    02-07-2022
@version	0.1
@python     >3.4
@info
@ideas      Maybe it is possible to load previous student results, based on the previous login_code?!       
"""

"""VBoxManage

restore snapshot => https://docs.oracle.com/en/virtualization/virtualbox/6.0/user/vboxmanage-snapshot.html
.\VBoxManage.exe snapshot "Linux Tiny Core" list => Take the last one or with a specific name
   Name: initial (UUID: 073a7297-dcc6-4065-be87-581e7d982dec)
      Name: update (UUID: 1d42c8d4-f597-41d1-b8e0-a36d7fda3d8c) *
.\VBoxManage.exe snapshot "Linux Tiny Core" restore "update" => Restoring

start vm => https://docs.oracle.com/en/virtualization/virtualbox/6.0/user/vboxmanage-startvm.html
.\VBoxManage.exe startvm "Linux Tiny Core"
Waiting for VM "Linux Tiny Core" to power on...
VM "Linux Tiny Core" has been successfully started.

snapshot current VM:
.\VBoxManage.exe snapshot "Linux Tiny Core" take "snapshot_12345" # Make a snapshot when the vm is stopped!

stop vm => https://docs.oracle.com/en/virtualization/virtualbox/6.0/user/vboxmanage-controlvm.html
.\VBoxManage controlvm "Linux Tiny Core" acpipowerbutton => Did not work for me
.\VBoxManage controlvm "Linux Tiny Core" savestate => Only saves the state
.\VBoxManage controlvm "Linux Tiny Core" poweroff => Cuts the power!



start vm headless / VRDP



"""


from distutils.log import debug
import time, threading, json, base64, requests
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

        # Simulation of protocol
        self.simulation = True

        # Experiment status variables
        self.experiment_id = -1
        self.status = ""
        self.status_info = "HostPC initiated"
        self.build_experiment_data = None
        self.stop_experiment = False
        self.timestamp = int(time.time())

        self._signal_values = {
            "login_code": "123456TEST",
            "login_url": "https://remotelabs.sendlab.nl/solar",
            "user_logged_in": "false",
            "status": "Status of the VM server",
            "progress": "0",
            "message": "Please login into the experiment server."
        }

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

    def send_authoriztion_message(self):
        self.sio.emit("remote_labs_connect_params", {
            "Authorization": self.config["password"],
            "RemoteLabs-Type": "HostPC",
            "RemoteLabs-SetupId": self.config["username"]
        }, "/hostpc")

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

    def send_status(self):
        self.sio.emit("send_status", self.get_status(), "/hostpc")

    def event_get_status(self, data):
        if ( self.fsm_state == FSMStates.START ): # This events changes state
            self.send_status() # (1)
            self.fsm_state = FSMStates.INIT
            
        else: # Always send a status back to the server on request
             self.send_status()

    def get_experiment_files(self, download_urls):
        """Retrieve the experiment files from the server using the data.
           TODO: At this moment no files are required to run the experiment."""
        # 1: Each experiment has its own directory, so create it is it does not exist
        experiment_dir = "./"
        for url in download_urls:
            if url.find('/'):
                filename = url.rsplit('/', 1)[1]
                print("get_experiment_files: URL: " + url)
                print("get_experiment_files: filename: " + filename)
                response = requests.get(url, stream=True, allow_redirects=True)
                open(experiment_dir + filename, "wb").write(response.content)
        pass

    def event_build_experiment(self, data):
        print("build_experiment: " + str(data))
        if self.fsm_state == FSMStates.READY_FOR_EXPERIMENT: # (2)
            self.build_experiment_data = data
            self.experiment_id = data["experiment_id"]
            self._set_status("receiving_experiment_files", "Ready for experiment")
            self.send_status() # (3)
            if "download_urls" in data:
                print(data["download_urls"])
#                self.get_experiment_files(data["download_urls"]) # (4)
            if "maxduration" in data:
                experiment_max_duration = data["maxduration"]
                print("event_build_experiment: MAX DURATION GIVEN: " + str(experiment_max_duration))
            self.fsm_state = FSMStates.BUILD_EXPERIMENT

    def event_actuator_update(self, data):
        print("actuator_update: " + str(data))

    def event_change_signal_mode(self, data):
        print("change_signal_mode: " + str(data))

    def event_free_message(self, data):
        print("free_message: " + str(data))

    def event_get_results(self, data):
        """The server request results for an experiment_id, this method sends the experiment files to the server.
            If the experiment_id exists."""
        print("get_results: " + str(data))
        experiment_id = data["experiment_id"]
        experiment_results = {
            "experiment_id": experiment_id,
            "download_urls": ["http://192.168.5.109/experiment_" + str(experiment_id) + ".zip"]
        }

    def event_reset(self, data):
        print("reset: " + str(data))

    def event_signal_request(self, data):
        print("signal_request: " + str(data))

    def event_start_experiment(self, data):
        print("start_experiment: " + str(data))
        if self.fsm_state == FSMStates.START_EXPERIMENT: # (11)
            if self.experiment_id != data["experiment_id"]:
                print("event_start_experiment: ERROR Wrong experiment number!!")
            # TODO: What to do when errors occur?
            self._set_status("running", "Start experiment")
            self.send_status() # (12)
            self.stop_experiment = False
            self.fsm_state = FSMStates.RUN_EXPERIMENT

    def event_start_recording(self, data):
        print("start_recording: " + str(data))

    def event_stop_experiment(self, data):
        print("stop_experiment: " + str(data))
        self.stop_experiment = True

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
#        self.sio.emit("free_message", { "test": "test"}, "/hostpc") # For testeing
        self.fsm_state = FSMStates.PREPARE_VM

    def clean_up_vm(self):
        """This methods checks if a VM is currently running. If so, it stops the VM and
           cleans it up."""
        self.debug_print("clean_up_vm: Cleaning up current VM")
        time.sleep(10)
        pass

    def create_new_vm(self):
        """This methods create and run a new VM to be used for the experiment."""
        self.debug_print("clean_new_vm: Creating new VM")
        time.sleep(10)
        pass

    def test_current_vm(self):
        """This methods test the running VM whether it can be used for the experiment."""
        self.debug_print("test_current_vm: Testing current VM")
        time.sleep(10)
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
            self._set_status("idle", "VM is prepared for new experiment")
            self.send_status()
            self.fsm_state = FSMStates.READY_FOR_EXPERIMENT
        else:
            self.debug_print("state_prepare_vm: The created VM is not working properly.")

    def state_ready_for_experiment(self):
        """Waits for build_experiment signal of the server. When it get the message
           'build_experiment' from the server, it will go to the next state 'BUILD_EXPERIMENT'"""
        self.debug_print("FSM READY_FOR_EXPERIMENT")

        if self.simulation:
            time.sleep(10) # SIMULATE RECEIVED BUILD_SIGNAL
            self.event_build_experiment({"experiment_id": 10}) # SIMULATE RECEIVED BUILD_SIGNAL

    def state_build_experiment(self):
        """Wait on the status 'building' message. BUG: We need to send the building, when we are builing."""
        self.debug_print("FSM BUILD_EXPERIMENT")

        self._set_status("building", "Ready to build the experimen") # (5)
        self.send_status()
        self.fsm_state = FSMStates.BUILDING
        
    def state_building(self):
        """Creates a value for the student to login into the system VM. It creates the data points for the experiment, like
           vm_login_code, progress, ... It sends out 'ready_to_start' status and goes to the next state 'start_experiment'."""
        self.debug_print("FSM BUILDING")
        config_definition = {
            "experiment_id": self.experiment_id,
            "config_values": [
                { 
                    "key": "rdp.login_url",
                    "value": self._signal_values["login_url"]
                },
                { 
                    "key": "rdp.login_code",
                    "value": self._signal_values["login_code"]
                }
            ]
        }
        self.sio.emit("config_definition", config_definition, "/hostpc") # (BETWEEN 6 and 7)

        signal_definition = {
            "experiment_id": self.experiment_id,
            "signal_groups": [
                {
                    "name": "VM",
                    "signal_definitions": [
                        {
                            "name": "user_logged_in",
                            "type": "boolean"
                        },
                        {
                            "name": "status",
                            "type": "string"
                        }

                    ]
                },
                {
                    "name": "Experiment",
                    "signal_definitions": [
                        {
                            "name": "progress",
                            "type": "int"
                        },
                        {
                            "name": "message",
                            "type": "string"
                        }
                    ]
                }

            ]
        }
        self.sio.emit("signal_definition", signal_definition, "/hostpc") # (7)

        actuator_definition = {
            "experiment_id": self.experiment_id,
            "actuators": []
        }
        self.sio.emit("actuator_definition", actuator_definition, "/hostpc") # (8)
        self._set_status("ready_to_start", "Building")
        self.send_status() # (9)
        self.fsm_state = FSMStates.START_EXPERIMENT

    def state_start_experiment(self):
        """Wait for the start_experiment message from the server."""
        self.debug_print("FSM START_EXPERIMENT")
        self.timestamp = int(time.time()) # Set the timestamp

        if self.simulation:
            time.sleep(10) # SIMULATE RECEIVED BUILD_SIGNAL
            self.event_start_experiment({"experiment_id": 10}) # SIMULATE RECEIVED BUILD_SIGNAL

    def state_run_experiment(self):
        """The experiment is running. Check the VM wheter someone has logged in and check the experiment progress. This data
           can be send back. When the user is logged out and is not coming back for 15 minutes, the experiment can be seen as
           stopped."""
        self.debug_print("FSM RUN_EXPERIMENT")
        signal_values = {
            "experiment_id": self.experiment_id,
            "signal_values": [
                {
                    "group_name": "VM",
                    "name": "user_logged_in",
                    "value": self._signal_values["user_logged_in"]
                },
                {
                    "group_name": "VM",
                    "name": "status",
                    "value": self._signal_values["status"]
                },
                {
                    "group_name": "Experiment",
                    "name": "progress",
                    "value": self._signal_values["progress"]
                },
                {
                    "group_name": "Experiment",
                    "name": "message",
                    "value": self._signal_values["message"]
                }
            ]
        }
        self.sio.emit("signal_values", signal_values, "/hostpc") # (15)
        # TODO: Interact with the VM to get the required results!! OR The VM sends us this data to an API? That is also a nice test.
        # TODO: When user is logged out or is not logged in within 15 minutes, prepare to stop.
        if self.stop_experiment:
            self._signal_values["message"] = "Please stop working on the experiment, will stop in 10 minutes."
            self.fsm_state = FSMStates.STOP_EXPERIMENT

        # Simulate the message from the server
        if self.simulation:
            timestamp = int(time.time())
            if timestamp - self.timestamp > 60:
                self.event_stop_experiment({"experiment_id": 10}) # SIMULATE RECEIVED BUILD_SIGNAL

    def state_stop_experiment(self):
        """Stop the experiment is getting the progress and all the information from the VM to be stored at the lectures files,
           but also create some files for the student, so he/she can make a report. Some messages needs to be send to the server."""
        self.debug_print("FSM STOP_EXPERIMENT")
        time.sleep(10) # When the user is logged in, wait for 10 minutes before stopping.
        self._set_status("processing_results", "Stopping experiment")
        self.send_status()
        time.sleep(10) # Processing results
        self._set_status("resetting", "Resetting the experiment")
        self.send_status()
        self.experiment_id = -1
        self.fsm_state = FSMStates.PREPARE_VM

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
