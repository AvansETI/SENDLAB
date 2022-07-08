package io.openems.edge.controller.api.mqtt.custom;

import org.eclipse.paho.mqttv5.client.IMqttToken;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttDisconnectResponse;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for mqtt callbacks. 
 * @author Nic
 *
 */
public class MqttApiCallbackImpl implements MqttCallback {

	/**
	 * Empty constructor
	 */
	public MqttApiCallbackImpl() {
	}
	
	private final Logger log = LoggerFactory.getLogger(MqttApiCallbackImpl.class);
	
	@Override
	public void authPacketArrived(int arg0, MqttProperties arg1) {
		this.log.info("authPacketArrived");
	}

	@Override
	public void connectComplete(boolean arg0, String arg1) {
		this.log.info("connectComplete");
	}

	@Override
	public void deliveryComplete(IMqttToken arg0) {
		this.log.info("deliveryComplete");
	}

	@Override
	public void disconnected(MqttDisconnectResponse arg0) {
		this.log.info("disconnected");
	}

	/**
	 * Prints the message that was received. 
	 */
	@Override
	public void messageArrived(String arg0, MqttMessage arg1) throws Exception {
		this.log.info("messageArrived");
		this.log.info("TOPIC: " + arg0.toString() + " = " + arg1.toString());
	}

	@Override
	public void mqttErrorOccurred(MqttException arg0) {
		this.log.warn("mqttErrorOccurred");
	}

}