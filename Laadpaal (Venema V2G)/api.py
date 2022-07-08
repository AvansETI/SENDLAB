from flask import Flask
from flask_cors import CORS, cross_origin

import json
import struct
import sys
sys.path.append('/usr/bin/env')
#!/usr/bin/env python

from pymodbus.client.sync import ModbusTcpClient as ModbusClient

client = ModbusClient(host='192.168.2.10', port=502)

client.connect()

app = Flask(__name__)
cors = CORS(app)
app.config['CORS_HEADERS'] = 'Content-Type'

@app.route('/')
@cross_origin()
def index():
    print("connection works")

@app.route('/data')
@cross_origin()
def data():
    #get data using the registers and package length. client.read_holding_registers(register, length)
    status = client.read_holding_registers(0x2020, 1)
    temperature = client.read_holding_registers(0x5054, 2)
    gridvoltage = client.read_holding_registers(0x5002, 2)

    #add register data to object below using a name, register output array and length.
    data = {
        "status": str(status.registers[0]),
        "temperature": read2reg(temperature.registers[0],temperature.registers[1]),
        "gridvoltage": read2reg(gridvoltage.registers[0],gridvoltage.registers[1]),
    }

    return json.dumps(data)

if __name__ == '__main__':
    app.run(debug=True, port=80, host='0.0.0.0')

def read2reg(a, b):
  return str(struct.unpack('>f', bytes.fromhex(f"{a:0>4x}" + f"{b:0>4x}"))[0])