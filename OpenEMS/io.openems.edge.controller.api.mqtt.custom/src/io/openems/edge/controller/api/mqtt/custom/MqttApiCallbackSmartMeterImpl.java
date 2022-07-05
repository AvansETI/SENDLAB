package io.openems.edge.controller.api.mqtt.custom;

import java.util.Collection;
import java.util.List;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableTable;
import com.google.gson.*;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.utils.JsonUtils;
import io.openems.edge.common.channel.Channel;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.channel.ChannelId;

public class MqttApiCallbackSmartMeterImpl extends MqttApiCallbackImpl implements MqttApiCallbackSmartMeter {
	
	private JsonObject meterData;
	
	@Reference
	protected ComponentManager componentManager;
	
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

	/**
	 * Method parses the JsonObject based on Smartmeter values.
	 */
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		this.log.info("messageArrived");
		super.messageArrived(arg0, arg1);
		
		//Parses Smartmeter data
		JsonObject o = JsonUtils.parseToJsonObject(arg1.toString());
		this.meterData = o;
		String id = JsonUtils.getAsString(o,"id");
		JsonElement ar = o.get("measurements").getAsJsonArray().get(0);
			
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
		
		OpenemsComponent smartmeter;
		try {
			smartmeter = this.componentManager.getComponent("smartmeter0");
			smartmeter.channel("EnergyDeliveredTarrif1").setNextValue(energy_delivered_tarrif_1);
			smartmeter.channel("EnergyDeliveredTarrif2").setNextValue(energy_delivered_tarrif_2);
			smartmeter.channel("EnergyReceivedTarrif1").setNextValue(energy_received_tarrif_1);
			smartmeter.channel("EnergyReceivedTarrif2").setNextValue(energy_received_tarrif_2);
			smartmeter.channel("TariffIndicator").setNextValue(tariff_indicator);
			smartmeter.channel("ActualPowerDelivered").setNextValue(actual_power_delivered);
			smartmeter.channel("ActualPowerReceived").setNextValue(actual_power_received);
			smartmeter.channel("GasDelivered").setNextValue(gas_delivered);
			smartmeter.channel("EnergyDelivered").setNextValue(energy_delivered);
			smartmeter.channel("EnergyReceived").setNextValue(energy_received);
			smartmeter.channel("Timestamp").setNextValue(timestamp);
			this.log.info(smartmeter.channels().toString());
			
		} catch (OpenemsNamedException e) {
			// TODO Auto-generated catch block
			this.log.info(e.getMessage());
//			e.printStackTrace();
		}
		
		
//		this.log.info(id);
//		this.log.info(energy_delivered_tarrif_1 + "");
//		this.log.info(energy_delivered_tarrif_2 + "");
//		this.log.info(energy_received_tarrif_1 + "");
//		this.log.info(energy_received_tarrif_2 + "");
//		this.log.info(tariff_indicator + "");
//		this.log.info(actual_power_delivered + "");
//		this.log.info(actual_power_received + "");
//		this.log.info(gas_delivered + "");
//		this.log.info(energy_delivered + "");
//		this.log.info(energy_received + "");
//		this.log.info(timestamp);
		
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

	@Override
	public JsonObject getMeterData() {
		return this.meterData;
	}
	
}
