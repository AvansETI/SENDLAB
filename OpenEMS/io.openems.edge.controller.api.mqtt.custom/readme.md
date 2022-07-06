# OpenEMS Edge Component [Api MQTT Custom]

Publishes OpenEMS Edge data to a MQTT broker.
Recieves data from a MQTT broker's topics.

Made based on SENDLab Broker requirements.

## Data sent to broker

* `node/init`
Topic used to initialize the node on the broker.
MQTT message format example:
```java
{
	"type": "Simulation",
	"mode": "0",
	"id": "SENDLab_OpenEMS",
	"name": "SENDLab_OpenEMS",
	"measurements": [	//(Array of arrays)
	{
		"name": "ess0",
		"description": "ess0",
		"unit": "khw" 
	},
		//etc
	],
	"actuators": [	//(Array of arrays)
	{
		"name": "ess0",
		"description": "ess0",
		"unit": "khw" 
	},
		//etc
	]
}
```

* `node/[nodeid]/message`
nodeid = the id of the node, can be changed within the backend or ui configuration of the component.
Result of the message sent to the broker on topic node/init.
example 
```java
{
	"status": 1,
	"time": "2022-06-20T15:57:52.5565074+00:00", 
	"message": "Welcome back to the network!"
}
```

## Receieved data from broker
* `node/data`
Topic used to send data to the node on the broker.
MQTT message format:
```java
{
	"id": "SENDLab_OpenEMS",
	"timestamp": "2022-06-20T15:57:52.5565074" //[year-month-dayTHrs:min:sec.milsec]  
	"measurements": [{},{},etc] //Data that you would like on the broker. example [{"value":"data"},{"value":"moredata"}]
}
```

* `node/[nodeid]/data`
nodeid = the id of the node, can be changed within the backend or ui configuration of the component.
Result of the message sent to the broker on topic node/data.
Data of Channels is published on change and at least every 5 minutes to these topics.

![alt text](./assets/mqtt.JPG)

## Design
The diagram below is an example of how messages are receieved from the broker. Depending on the node type a new component will be needed.
This diagram uses the Smartmeter component. It also shows the communication between the edge device and the ui.

![alt text](./assets/Communication%20overview%20with%20smartmeter%20as%20example.JPG)

## Component default values

Image of the Ui component
![alt text](./assets/Ui%20component.JPG)

Values for Sendlab broker
Username: node
Password: smartmeternode
uri: sendlab.nl:11884

Default values
Alias: SENDLab_OpenEMS
Is enabled?: true
Client-ID: SENDLab_OpenEMS
Username: node
Password: smartmeternode
uri: sendlab.nl:11884
Node Type: Default
Topic: ""
Persistence Priority: Very Low
Debug Mode: false

Orignal source branch (Temp)
[Branch code](https://github.com/AvansETI/SENDLAB/tree/OpeEms/feature/merge/OpenEMS/io.openems.edge.controller.api.mqtt.custom)

End destination
[Target source code](https://github.com/AvansETI/SENDLAB/tree/Development/OpenEMS/io.openems.edge.controller.api.mqtt.custom)