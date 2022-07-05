package io.openems.edge.controller.api.mqtt.custom;

import com.google.gson.JsonObject;

public interface MqttApiCallbackSmartMeter extends MqttApiCallback {
	
	public default JsonObject getMeterData() {return null;};

}