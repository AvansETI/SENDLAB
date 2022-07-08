# OpenEMS Edge Component [Smartmeter]

Collects data from the MQTT topic and communicaties via websocket to the ui.
UI displays the data in a widget.

## Design
The diagram below is an example of how messages are receieved from the broker. Depending on the node type a new component will be needed.
This diagram uses the Smartmeter component. It also shows the communication between the edge device and the ui.
![alt text](../io.openems.edge.controller.api.mqtt.custom/assets/Communication%20overview%20with%20smartmeter%20as%20example.JPG)

## Beschrijving

Het componenent bestaat uit een OSGi Bundel die binnen OpenEMS is gerealiseerd. Het component maakt hierbij gebruik van het standaard generieke component van OpenEMS voor apparaten.

## Toepassing en werking

Voor van toevoegen van nieuwe dataskeleten binnen het component moet er binnen de Smartmeter.java interface nieuwe Channels worden aangemaakt. Hierbij moet er ook een getter en setter worden toegevoegd.

```java
NAME_OF_DATA(Doc.of(OpenemsType.DOUBLE).unit(Unit.{TYPE}).accessMode(AccessMode.READ_WRITE)
				.persistencePriority(PersistencePriority.HIGH)),
```

[Source code](https://github.com/AvansETI/SENDLAB/tree/OpeEms/feature/merge/OpenEMS/io.openems.edge.smartmeter)