
# OpenEMS Edge Component [Charging Station - V2G]

Een beschrijving van de werking en toepassing van dit component.

## Beschrijving

Het componenent bestaat uit een OSGi Bundel die binnen OpenEMS is gerealiseerd. Het component maakt hierbij gebruik van het standaard generieke component van OpenEMS voor apparaten. Binnen het component haalt het data op met gebruik van een Eventhandler die per seconde data ophaalt. Dit gebeurd met behulp van een HTTPClient die het JSON object ophaalt met de data vanuit de API van de laadpaal.

## Ontwerp

### Hierarchy

De hierarchy van dit component ziet er zo uit:

![design](https://raw.githubusercontent.com/AvansETI/SENDLAB/OpenEms/feature/V2G/Laadpaal%20(Venema%20V2G)/ontwerp/component-design.png)

Het V2G component maakt gebruik van het abstracte standaard openems component. Hieraan worden de verschillende Channels toegevoegd die benodigd zijn voor de data die de HTTPClient binnen haalt. De HTTPClient krijgt een JSON Object met alle data binnen en de data worden binnen de Eventhandler individueel gekoppeld aan de Channels

### Channel communicatie

![design](https://openems.github.io/openems.io/openems/latest/_images/subscribeChannels+currentData.png)

De wijze waarop de data gedeeld wordt tussen de UI en Edge is in de bovenstaande diagram uit te lezen.

## Toepassing en werking

Voor van toevoegen van nieuwe dataskeleten binnen het component moet er binnen de V2G.java interface nieuwe Channels worden aangemaakt. Hierbij moet er ook een getter en setter worden toegevoegd.

```java
NAME_OF_DATA(Doc.of(OpenemsType.DOUBLE).unit(Unit.{TYPE}).accessMode(AccessMode.READ_WRITE)
				.persistencePriority(PersistencePriority.HIGH)),
```

Binnen V2GImpl.java kan dan vanuit het ontvangen JSON object de data worden gekoppeld aan de juiste channel met gebruik van de setter van de channel.

```java
this._set{ChannelName}(jo.get("ChannelName").getAsDouble());
```

Met gebruik van de Channels kan dan alle data worden gedeeld met de rest van de componenten of UI.

