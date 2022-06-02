package io.openems.edge.controller.api.mqtt.custom;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.*;

import io.openems.common.utils.JsonUtils;

public class MqttApiCallbackSmartMeterImpl extends MqttApiCallbackImpl {

	public MqttApiCallbackSmartMeterImpl() {
	}
	
	private final Logger log = LoggerFactory.getLogger(MqttApiCallbackSmartMeterImpl.class);
	 
	@Override
	public void authPacketArrived(int arg0, MqttProperties arg1) {
		super.authPacketArrived(arg0, arg1);
	}

	@Override
	public void connectComplete(boolean arg0, String arg1) {
		super.connectComplete(arg0, arg1);
	}

	@Override
	public void deliveryComplete(IMqttToken arg0) {
		super.deliveryComplete(arg0);
	}

	@Override
	public void disconnected(MqttDisconnectResponse arg0) {
		super.disconnected(arg0);
	}

	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		super.messageArrived(arg0, arg1);
		
		//Parses Smartmeter data
		JsonObject o = JsonUtils.parseToJsonObject(arg1.toString());
		String id = JsonUtils.getAsString(o,"id");
		JsonElement ar= o.get("measurements").getAsJsonArray().get(0);
	
		double energy_delivered_tarrif_1 = JsonUtils.getAsDouble(ar,"energy_delivered_tarrif_1");
		double energy_delivered_tarrif_2  = JsonUtils.getAsDouble(ar,"energy_delivered_tarrif_2");
		double energy_received_tarrif_1 = JsonUtils.getAsDouble(ar,"energy_received_tarrif_1");
		double energy_received_tarrif_2 = JsonUtils.getAsDouble(ar,"energy_received_tarrif_2");
		int tariff_indicator = JsonUtils.getAsInt(ar,"tariff_indicator");
		double actual_power_delivered = JsonUtils.getAsDouble(ar,"actual_power_delivered");
		double actual_power_received = JsonUtils.getAsDouble(ar,"actual_power_received");
		double gas_delivered = JsonUtils.getAsDouble(ar,"gas_delivered");
		double energy_delivered = JsonUtils.getAsDouble(ar,"energy_delivered");
		double energy_received = JsonUtils.getAsDouble(ar,"energy_received");
		String timestamp = JsonUtils.getAsString(ar,"timestamp");
		
		this.log.info(id);
		this.log.info(energy_delivered_tarrif_1 + "");
		this.log.info(energy_delivered_tarrif_2 + "");
		this.log.info(energy_received_tarrif_1 + "");
		this.log.info(energy_received_tarrif_2 + "");
		this.log.info(tariff_indicator + "");
		this.log.info(actual_power_delivered + "");
		this.log.info(actual_power_received + "");
		this.log.info(gas_delivered + "");
		this.log.info(energy_delivered + "");
		this.log.info(energy_received + "");
		this.log.info(timestamp);
		
		//Smartmeter data example
//		{"id": "smartmeter-2019-ETI-EMON-V01-DADDE2-16301C", 
//			"measurements": [{
//				"energy_delivered_tarrif_1": 4143.831, 
//				"energy_delivered_tarrif_2": 3853.056, 
//				"energy_received_tarrif_1": 6.4, 
//				"energy_received_tarrif_2": 3.293, 
//				"tariff_indicator": 2, 
//				"actual_power_delivered": 0.262, 
//				"actual_power_received": 0.0, 
//				"gas_delivered": 452.571, 
//				"energy_delivered": 7996.887000000001, 
//				"energy_received": 9.693000000000001, 
//				"timestamp": "2022-05-25T11:14:27.641950+00:00"}]
//		}
		
	}

	@Override
	public void mqttErrorOccurred(MqttException arg0) {
		super.mqttErrorOccurred(arg0);
	}
	
}
