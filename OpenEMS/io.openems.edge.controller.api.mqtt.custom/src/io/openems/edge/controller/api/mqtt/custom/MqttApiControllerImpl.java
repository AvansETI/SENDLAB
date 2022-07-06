package io.openems.edge.controller.api.mqtt.custom;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.common.MqttException;
import org.eclipse.paho.mqttv5.common.MqttMessage;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonArray;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.EdgeConfig;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.timedata.api.Timedata;

@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Api.MQTT.custom", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CONFIG_UPDATE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE //
		} //
)
public class MqttApiControllerImpl extends AbstractOpenemsComponent
		implements MqttApiController, Controller, OpenemsComponent, EventHandler {

	protected static final String COMPONENT_NAME = "Controller.Api.MQTT.Custom";

	private final Logger log = LoggerFactory.getLogger(MqttApiControllerImpl.class);
	private final SendChannelValuesWorker sendChannelValuesWorker = new SendChannelValuesWorker(this);
	private final MqttConnector mqttConnector = new MqttConnector();
//	private final MqttConnector mqttConnectorForConfig = new MqttConnector();

	protected Config config;
	private final String sendlabUri = "tcp://sendlab.nl:11884";

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.OPTIONAL)
	private volatile Timedata timedata = null;

	@Reference
	protected ComponentManager componentManager;

	public MqttApiControllerImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Controller.ChannelId.values(), //
				MqttApiController.ChannelId.values() //
		);
	}

	private IMqttClient mqttClient = null;
//	private IMqttClient mqttClientForConfig = null;
	protected Instant currentTime;
	
	@Activate
	void activate(ComponentContext context, Config config) throws Exception {
		this.config = config;

		super.activate(context, config.id(), config.alias(), config.enabled());

		this.currentTime = Instant.now(this.componentManager.getClock());
		
		/**
		 * Connects to the MQTT broker. Sets info based on config data.
		 */
		this.mqttConnector.connect(config.uri(), config.clientId(), config.username(), config.password(), getCallback(config.type()))
			.thenAccept(client -> {
				this.mqttClient = client;				
				if(this.mqttClient.isConnected()) {
					this.logInfo(this.log, "Connected to MQTT Broker [" + config.uri() + "]");
				
					if(config.subscriber()) {
						
						for(String topic : config.topic()) {
							this.subscribe(topic, 1);
						}
						
						if(config.uri().contains(sendlabUri) && config.type() == NodeType.DEFAULT) {
							this.subscribe("node/" + config.clientId() + "/message", 0); 
							this.subscribe("node/" + config.clientId() + "/data", 0);
							
						}
					}
				}
			});		
		
		/**
		 * Connects to the MQTT broker (only for config data).
		 */
//		if(config.uri().contains(sendlabUri)) {
//			this.mqttConnectorForConfig.connect(config.uri(), config.clientId(), config.username(), config.password())
//			.thenAccept(client -> {
//				this.mqttClientForConfig = client;				
//				if(this.mqttClientForConfig.isConnected()) {
//					this.logInfo(this.log, "Connected to MQTT Broker CONFIG [" + config.uri() + "]");
//				}
//			});
//		}
	}
	
	/**
	 * Selects the callback based on input type.
	 * 
	 * @param type 		A selection of meters that are connected to OpenEMS. Can be selected from the ui.
	 * @return MqttCallback.
	 */
	protected MqttApiCallbackImpl getCallback(NodeType type) {
		switch (type){
		case SMARTMETER:
			return new MqttApiCallbackSmartMeterImpl(); 
			
		case SOLAREDGE:
			//TODO implement Solaredge callback.
			
		case POWERWALL:
			//TODO implement Powerwall callback.
		
		case DEFAULT :
			return new MqttApiCallbackImpl();
				
		default:
			return new MqttApiCallbackImpl();
		}
	}

	/**
	 * Disables all clients and connectors.
	 */
	@Deactivate
	protected void deactivate() {
		super.deactivate();
		this.sendChannelValuesWorker.deactivate();
		
		if (this.mqttClient != null) {
			try {	
				this.mqttClient.disconnect();	
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to disconnect with the MQTT broker: " + e.getMessage());
				e.printStackTrace();
			}
			
			try {	
				this.mqttClient.close();	
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to close the MQTT broker connection: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if(this.mqttConnector != null) {
			this.mqttConnector.deactivate();
		}
		
//		if(this.mqttConnectorForConfig != null) {
//			this.mqttConnectorForConfig.deactivate();
//		}
//		if(this.mqttClientForConfig != null) {
//			try {
//				this.mqttClientForConfig.disconnect();	
//				this.mqttClientForConfig.close();		
//			} catch (MqttException e) {
//				this.logWarn(this.log, "Unable to close connection to MQTT broker (CONFIG): " + e.getMessage());
//				e.printStackTrace();
//			}
//			
//		}
	}

	//Does nothing
	@Override
	public void run() throws OpenemsNamedException {	   
	}

	@Override
	protected void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	protected void logWarn(Logger log, String message) {
		super.logWarn(log, message);
	}
	
	@Override
	protected void logError(Logger log, String message) {
		super.logError(log, message);
	}
	
	/**
	 * Handles events based on event topics.
	 */
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE: 
				if(this.config.uri().contains(sendlabUri)){
					/**
					 * Collects data from OpenEMS and sends to broker.
					 */
					this.sendChannelValuesWorker.collectData(this.config.clientId());
					//TODO - doesnt work with alias for some reason. Problem on broker side.
//					this.sendChannelValuesWorker.collectData(this.config.alias());
				}
				break;
			
			case EdgeEventConstants.TOPIC_CONFIG_UPDATE: 
			
				/**
				 * Collects config data and sends it to the broker.
				 * TODO - Doesn't work based on the mqtt broker settings. 
				 * 			Need to be in format of node/init and node/data
				 * 			node/init - info of what will be sent.
				 * 			node/data - actual data that will be sent to the broker.
				 */
//				if(this.config.uri().contains(sendlabUri)){	
//					// Send new EdgeConfig
//					EdgeConfig config = (EdgeConfig) event.getProperty(EdgeEventConstants.TOPIC_CONFIG_UPDATE_KEY);
//					
//					MqttMessage msg = new MqttMessage(config.toJson().toString().getBytes(StandardCharsets.UTF_8), 0, true, new MqttProperties());
//					
//					if(mqttClientForConfig != null) {
//						try {
//							this.mqttClientForConfig.publish(this.config.clientId() + "/" + MqttApiController.TOPIC_EDGE_CONFIG, msg);
//						} catch (MqttException e) {
//							this.logError(this.log, "test" + e.getMessage());
//						}
//					}
//			
////					// Trigger sending of all channel values, because a Component might have
////					// disappeared
//					this.sendChannelValuesWorker.sendValuesOfAllChannelsOnce();
//				}
				break;
				
				//Could be used to send data after writing
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE:
				
		}
	}

	/**
	 * Publish a message to a topic.
	 * 
	 * @param subTopic the MQTT topic. 
	 * @param message  the message
	 * @return true if message was successfully published; false otherwise
	 */
	protected boolean publish(String subTopic, MqttMessage message) {
		IMqttClient mqttClient = this.mqttClient;
		if (mqttClient == null) {
			return false;
		}
		try {
			if(mqttClient.isConnected()) {
				mqttClient.publish(subTopic, message);
				this.logInfo(log, "Publish [" + message + "]");
				return true;
			}
			return false;
		} catch (MqttException e) {
			this.logError(this.log, e.getMessage());
			return false;
		}
	}

	/**
	 * Publish a message to a topic.
	 * 
	 * @param subTopic   the MQTT topic. 
	 * @param message    the message; internally translated to a UTF-8 byte array
	 * @param qos        the MQTT QOS
	 * @param retained   the MQTT retained parameter
	 * @param properties the {@link MqttProperties}
	 * @return true if message was successfully published; false otherwise
	 */
	protected boolean publish(String subTopic, String message, int qos, boolean retained, MqttProperties properties) {
		MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8), qos, retained, properties);
		return this.publish(subTopic, msg);
	}
	
	/**
	 * Subscribe to a topic.
	 * 
	 * @param topic		 the MQTT topic. 
	 * @param qos  		 the MQTT QOS
	 * @return true if topic was successfully subscribed to; false otherwise.
	 */
	protected boolean subscribe(String topic, int qos) {
		IMqttClient mqttClient = this.mqttClient;
		if (mqttClient == null) {
			return false;
		}
		try {
			if(mqttClient.isConnected()) {
				mqttClient.subscribe(topic, qos);
				this.logInfo(log, "Subscribed to [" + topic + "]");
				return true;
			}
			return false;
		} catch (MqttException e) {
			String error = e.getMessage();
			this.logError(log, error);
			if(error.equals("Connection lost")) {
				this.subscribe(topic, qos);
			}
			return false;
		}
	};
	
}
