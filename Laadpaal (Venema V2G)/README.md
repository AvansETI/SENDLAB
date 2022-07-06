# Laadpaal (Venema V2G)

Implementatie van de bi-directionele laadpaal binnen het SENDLAB.

## Documentatie

De laadpaal is verbonden met een RaspberryPI via ethernet om een MODBUS/TCP verbinding te realiseren. Om de laadpaal te kunnen verbinden met het SENDLAB is er gebruik gemaakt van een API die op de RaspberryPI staat.
De API is gerealiseerd in Python met behulp van Flask voor de API en PyModbus voor de MODBUS/TCP verbinding.

Deze API stuurt met behulp van een simpele HTTP GET request een JSON object terug met verschillende variabelen aan data. De data wordt na de call opgevraagd vanuit de laadpaal doormiddel van de MODBUS registers.

## API

#### Get JSON object with all the data from the charging station

```http
  GET https://192.168.0.183/data
```
Returns JSON Object

## Toepassing

Om registers toe te voegen aan het JSON Object moeten er register calls worden toegevoegd. Hieronder staat beschreven op welke manier dit moet binnen de Python script:

```python
    {data_name} = client.read_holding_registers({register}, {length})
```
data_name = de naam van het registerdata

register = register in HEX

length = lengte van het register

Hierna moet het resultaat van het register worden toegevoegd binnen JSON object. De wijze erop verschilt van de lengte van het register

```python
    data = {
        "data_name": str(data_name.registers[0]), #als de lengte 1 is
        "data_name": read2reg(data_name.registers[0],data_name.registers[1]), #als de lengte 2 is
    }
```
Als alles is toegevoegd staat de data nu succesvol in de JSON
