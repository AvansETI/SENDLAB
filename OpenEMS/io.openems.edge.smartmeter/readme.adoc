= Smartmeter

Collects data from the MQTT topic and communicaties via websocket to the ui.
UI displays the data in a widget.

The diagram below is an example of how messages are receieved from the broker. Depending on the node type a new component will be needed.
This diagram uses the Smartmeter component. It also shows the communication between the edge device and the ui.
![alt text](../io.openems.edge.controller.api.mqtt.custom/assets/Communication%20overview%20with%20smartmeter%20as%20example.JPG)


https://github.com/AvansETI/SENDLAB/tree/OpeEms/feature/merge/OpenEMS/io.openems.edge.smartmeter[Source Code icon:github[]]