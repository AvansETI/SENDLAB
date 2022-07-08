package io.openems.edge.controller.api.mqtt.custom;

import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

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
import com.google.gson.JsonElement;

import io.openems.common.exceptions.OpenemsError.OpenemsNamedException;
import io.openems.common.types.EdgeConfig;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;
import io.openems.edge.controller.api.Controller;
import io.openems.edge.timedata.api.Timedata;

/**
 * Implementation of the custom MQTT component. Based on the requirements of the SENDLab.
 * @author Nic
 *
 */
@Designate(ocd = Config.class, factory = true)
@Component(name = "Controller.Api.MQTT.custom", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE, //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CONFIG_UPDATE, //
		} //
)
public class MqttApiControllerImpl extends AbstractOpenemsComponent
		implements MqttApiController, Controller, OpenemsComponent, EventHandler {

	protected static final String COMPONENT_NAME = "Controller.Api.MQTT.Custom";

	private final Logger log = LoggerFactory.getLogger(MqttApiControllerImpl.class);
	private final SendChannelValuesWorker sendChannelValuesWorker = new SendChannelValuesWorker(this);
	private final MqttConnector mqttConnector = new MqttConnector();
	private final MqttConnector mqttConnectorForTopic = new MqttConnector();
	private final MqttConnector mqttConnectorForConfig = new MqttConnector();
	
	protected IMqttClient mqttClient = null;
	private IMqttClient mqttClientForTopic = null;
	private IMqttClient mqttClientForConfig = null;
	protected Config config;
	protected Instant currentTime;
	
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
	
	@Activate
	void activate(ComponentContext context, Config config) throws Exception {
		this.config = config;

		super.activate(context, config.id(), config.alias(), config.enabled());

		this.currentTime = Instant.now(this.componentManager.getClock());
		
		String clientId = config.clientId();
		
		/**
		 * Connects to the MQTT broker. Publishes OpenEMS data.
		 */
		this.mqttConnector.connect(config.uri(), clientId+"_Publish", config.username(), config.password())
			.thenAccept(client -> {
				this.mqttClient = client;				
				if(client.isConnected()) {
					this.logInfo(this.log, "Connected to MQTT Broker [" + config.uri() + "]");
				}
			});		
		
		/**
		 * Connects to the MQTT broker. Reads Broker data.
		 * Needed for reading data, publish method is blocking thus cannot read data asynchronously.
		 */
		if(config.subscriber()) {
			this.mqttConnectorForTopic.connect(config.uri(), clientId+"_Subscribe", config.username(), config.password(), getCallback(config.type()))
				.thenAccept(client -> {
					this.mqttClientForTopic = client;				
					if(client.isConnected()) {
						this.logInfo(this.log, "Connected to MQTT Broker for Topics [" + config.uri() + "]");
			
						for(String topic : config.topic()) {
							this.subscribe(topic, 0);
						}
					
						if(config.type() == NodeType.OPENEMS) {
							this.subscribe("node/" + config.clientId() + "/message", 0); 
							this.subscribe("node/" + config.clientId() + "/data", 0);
						}
					}
				});	
		}
		
		/**
		 * Connects to the MQTT broker (only for config data).
		 */
		this.mqttConnectorForConfig.connect(config.uri(), clientId+"_Config", config.username(), config.password())
			.thenAccept(client -> {
				this.mqttClientForConfig = client;				
				if(this.mqttClientForConfig.isConnected()) {
					this.logInfo(this.log, "Connected to MQTT Broker for OpenEMS config [" + config.uri() + "]");
					//TODO - not yet implemented
//					this.publishConfigDataInit();
				}
			});
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
			return new MqttApiCallbackSmartMeterImpl(componentManager); 
			
		case SOLAREDGE:
			//TODO implement Solaredge callback.
			
		case POWERWALL:
			//TODO implement Powerwall callback.
			
		case OPENEMS:
		case DEFAULT:
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
		
		mqttDisconnect(mqttClient, mqttConnector);
		mqttDisconnect(mqttClientForTopic,mqttConnectorForTopic);
		mqttDisconnect(mqttClientForConfig, mqttConnectorForConfig);
	}
	
	/**
	 * Method used to disconnect the MQTT client and connector.
	 * @param client 	 the MQTT client
	 * @param connector	 the MQTT connector
	 */
	private void mqttDisconnect(IMqttClient client, MqttConnector connector) {
		
		if(client != null) {
			if(client.isConnected()) {
				try {	
					client.disconnect();	
				} catch (MqttException e) {
					this.logWarn(this.log, "Unable to disconnect with the MQTT broker: " + e.getMessage());
					e.printStackTrace();
				}
			}
			
			try {	
				client.close();	
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to close the MQTT broker connection: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if(connector != null) {
			connector.deactivate();
		}
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
					/**
					 * Collects data from OpenEMS and sends to broker.
					 */
					if(this.config.alias() != null && !this.config.alias().isBlank()) {
						this.sendChannelValuesWorker.collectData(this.config.alias());
					}else {
						this.sendChannelValuesWorker.collectData(this.config.clientId());
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
					// Send new EdgeConfig
//					EdgeConfig config = (EdgeConfig) event.getProperty(EdgeEventConstants.TOPIC_CONFIG_UPDATE_KEY);
					
//					MqttMessage msg = new MqttMessage(config.toJson().toString().getBytes(StandardCharsets.UTF_8), 0, true, new MqttProperties());
//					
//					if(mqttClientForConfig != null) {
//						try {
//							this.publishConfigData(); //to replace method below.
//							this.mqttClientForConfig.publish(this.config.clientId() + "/" + MqttApiController.TOPIC_EDGE_CONFIG, msg);
//						} catch (MqttException e) {
//							this.logError(this.log, "test" + e.getMessage());
//						}
//					}
			
					// Trigger sending of all channel values, because a Component might have
					// disappeared
					this.sendChannelValuesWorker.sendValuesOfAllChannelsOnce();
				
				break;
				
		}
	}

	/**
	 * Publish a message to a topic.
	 * 
	 * @param client	the MQTT client.
	 * @param topic 	the MQTT topic. 
	 * @param message  	the message
	 * @return true if message was successfully published; false otherwise
	 */
	protected boolean publish(IMqttClient client, String subTopic, MqttMessage message) {
		if (mqttClient == null) {
			return false;
		}
		try {
			if(mqttClient.isConnected()) {
				mqttClient.publish(subTopic, message);
//				this.logInfo(log, "Publish [" + message + "]");
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
	 * @param client	 the MQTT client.
	 * @param topic   	 the MQTT topic. 
	 * @param message    the message; internally translated to a UTF-8 byte array
	 * @param qos        the MQTT QOS
	 * @param retained   the MQTT retained parameter
	 * @param properties the {@link MqttProperties}
	 * @return true if message was successfully published; false otherwise
	 */
	protected boolean publish(IMqttClient client, String topic, String message, int qos, boolean retained, MqttProperties properties) {
		MqttMessage msg = new MqttMessage(message.getBytes(StandardCharsets.UTF_8), qos, retained, properties);
		return this.publish(client, topic, msg);
	}
	
	/**
	 * Subscribe to a topic.
	 * 
	 * @param topic		 the MQTT topic. 
	 * @param qos  		 the MQTT QOS
	 * @return true if topic was successfully subscribed to; false otherwise.
	 */
	protected boolean subscribe(String topic, int qos) {
		IMqttClient mqttClient = this.mqttClientForTopic;
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
			this.logError(log, e.getMessage());
			e.printStackTrace();
			return false;
		}
	};
	
	/**
	 * TODO - Implement node/init for config data.
	 * 		(node/"clientId"/message)
	 */
//	private void publishConfigDataInit() {
//		Map<String, Object> jsonData = new LinkedHashMap<>();
//		jsonData.put("mode", 0);
//		jsonData.put("type", "simulation"); //Don't know the other types that the server accepts.
//		jsonData.put("id", id);
//		jsonData.put("name", id);
//		
//		
//		ArrayList<HashMap<String, String>> measurementsInit = new ArrayList<HashMap<String,String>>();
//		LinkedHashMap<String,String> mapInit = new LinkedHashMap<String,String>();
//	
//		// Send changed values
//		for (Entry<String, Map<String, JsonElement>> row : this.allValues.rowMap().entrySet()) {
//			for (Entry<String, JsonElement> column : row.getValue().entrySet()) {
//				if (!Objects.equals(column.getValue(), lastAllValues.get(row.getKey(), column.getKey()))) {
//					String subtopic = row.getKey() + "/" + column.getKey();
//					
//					
//						mapData.put(subtopic,column.getValue().toString());
//				
//		
//				}
//			}
//		}
//		
//		measurementsData.add(mapData);
//		jsonData.put("measurements", measurementsData);
//		jsonData.put("actuators", new HashMap<>());
//		Gson g = new Gson();
//		String parsedData = g.toJson(jsonData);
//		this.publish("node/data", parsedData);
//	}
	
	/**
	 * TODO - Implement node/data for config data.
	 * 		  (node/"clientId"/data)
	 */
//	private void publishConfigData() {
//		Map<String, Object> jsonData = new LinkedHashMap<>();
//
//		Timestamp instant= Timestamp.from(Instant.now());
//		String time = instant.toString();
//		
//		String[] s = time.split(" ");
//		String b = s[0]; 
//		String c = s[1]; 
//		time = b + "T" + c;
//			
//		jsonData.put("id",id);
//		jsonData.put("timestamp", time);
//		
//		ArrayList<HashMap<String, Object>> measurementsData = new ArrayList<HashMap<String,Object>>();
//		LinkedHashMap<String,Object> mapData = new LinkedHashMap<String,Object>();
//		
//		// Send changed values
//		for (Entry<String, Map<String, JsonElement>> row : this.allValues.rowMap().entrySet()) {
//			for (Entry<String, JsonElement> column : row.getValue().entrySet()) {
//				if (!Objects.equals(column.getValue(), lastAllValues.get(row.getKey(), column.getKey()))) {
//					String subtopic = row.getKey() + "/" + column.getKey();
//					
//					mapData.put(subtopic,column.getValue().toString());
//		
//				}
//			}
//		}
//		
//		/**
//		 * Publishes data to the MQTT broker.
//		 */
//		jsonData.put("measurements", measurementsInit);
//		Gson g = new Gson();
//		String parsedData = g.toJson(jsonData);
//		this.publish("node/init", parsedData);
//	}
	
}
