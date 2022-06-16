package io.openems.edge.controller.api.mqtt;

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
@Component(name = "Controller.Api.MQTT", //
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

	protected static final String COMPONENT_NAME = "Controller.Api.MQTT";

	private final Logger log = LoggerFactory.getLogger(MqttApiControllerImpl.class);
	private final SendChannelValuesWorker sendChannelValuesWorker = new SendChannelValuesWorker(this);
	private final MqttConnector mqttConnector = new MqttConnector();
	private final MqttConnector mqttConnectorForConfig = new MqttConnector();

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
	private IMqttClient mqttClientForConfig = null;
	
	@Activate
	void activate(ComponentContext context, Config config) throws Exception {
		this.config = config;

		super.activate(context, config.id(), config.alias(), config.enabled());
		
		this.mqttConnector.connect(config.uri(), config.clientId(), config.username(), config.password())
			.thenAccept(client -> {
				this.mqttClient = client;				
				if(this.mqttClient.isConnected()) {
					this.logInfo(this.log, "Connected to MQTT Broker [" + config.uri() + "]");
				}
				
				if(config.subscriber()) {

					//Enum class makes dropdown menu in ui.
					this.mqttClient.setCallback(getCallback(config.type()));
					
					//Need to figure out how to refresh connection when new conenction is made or app is refreshed.
					for(String topic : config.topic()) {
						this.subscribe(topic, 0);
					}
					
					if(config.uri().contains(sendlabUri)) {
						this.subscribe("node/" + config.clientId() + "/message", 0); 
						this.subscribe("node/" + config.clientId() + "/data", 0);
						
					}
			
				}
			});		
		
		if(config.uri().contains(sendlabUri)) {
			this.mqttConnectorForConfig.connect(config.uri(), config.clientId(), config.username(), config.password())
			.thenAccept(client -> {
				this.mqttClientForConfig = client;				
				if(this.mqttClientForConfig.isConnected()) {
					this.logInfo(this.log, "Connected to MQTT Broker CONFIG [" + config.uri() + "]");
				}
			});
		}
	}
	
	protected MqttApiCallbackImpl getCallback(NodeType type) {
		switch (type){
		case SMARTMETER:
			return new MqttApiCallbackSmartMeterImpl();
			
		case SOLAREDGE:
			//TODO implement Solaredge callback.
		
		case DEFAULT :
			return new MqttApiCallbackImpl();
				
		default:
			return new MqttApiCallbackImpl();
		}
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
		this.sendChannelValuesWorker.deactivate();
		
		if(this.mqttConnector != null) {
			this.mqttConnector.deactivate();
		}
		if (this.mqttClient != null) {
			try {	
				this.mqttClient.disconnect();	
				this.mqttClient.close();	
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to close connection to MQTT broker: " + e.getMessage());
				e.printStackTrace();
			}
		}
		
		if(this.mqttConnectorForConfig != null) {
			this.mqttConnectorForConfig.deactivate();
		}
		if(this.mqttClientForConfig != null) {
			try {
				this.mqttClientForConfig.disconnect();	
				this.mqttClientForConfig.close();		
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to close connection to MQTT broker (CONFIG): " + e.getMessage());
				e.printStackTrace();
			}
			
		}
	}

	
	@Override
	public void run() throws OpenemsNamedException {	   
	}

	@Override
	protected void logInfo(Logger log, String message) {
		super.logInfo(log, message);
	}

	@Override
	protected void logWarn(Logger log, String message) {
		super.logInfo(log, message);
	}
	
	@Override
	protected void logError(Logger log, String message) {
		super.logError(log, message);
	}
	
	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		
		switch (event.getTopic()) {
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_PROCESS_IMAGE: 
				if(this.config.uri().contains(sendlabUri)){
//					int timediff = Timestamp.from(Instant.now()).getSeconds() - instant.getSeconds();
//					if ( timediff > 1 ) {
//						//put code here
//						instant = Timestamp.from(Instant.now());
//				    };
				
					this.sendChannelValuesWorker.collectData(this.config.clientId());
				}
				break;
			
			case EdgeEventConstants.TOPIC_CONFIG_UPDATE: 
			
				if(this.config.uri().contains(sendlabUri)){	
					this.logInfo(log, "UPDATE!!!!!!!!!!!!!!!");
					// Send new EdgeConfig
					EdgeConfig config = (EdgeConfig) event.getProperty(EdgeEventConstants.TOPIC_CONFIG_UPDATE_KEY);
					
					MqttMessage msg = new MqttMessage(config.toJson().toString().getBytes(StandardCharsets.UTF_8), 1, true, new MqttProperties());
					
					if(mqttClientForConfig != null) {
						try {
							this.mqttClientForConfig.publish(this.config.clientId() + MqttApiController.TOPIC_EDGE_CONFIG, msg);
						} catch (MqttException e) {
							this.logError(this.log, e.getMessage());
						}
					}
			
//					// Trigger sending of all channel values, because a Component might have
//					// disappeared
					this.sendChannelValuesWorker.sendValuesOfAllChannelsOnce();
				}
				break;
				
				//TODO find out how to get data from sensors.
				//
				//Can be used to send data after writing
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE:
				
		}
	}

	/**
	 * Publish a message to a topic.
	 * 
	 * @param subTopic the MQTT topic. The global MQTT Topic prefix is added in
	 *                 front of this string
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
				this.logInfo(log, "Publish [" + message + "]");
				mqttClient.publish(subTopic, message);
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
	 * @param subTopic   the MQTT topic. The global MQTT Topic prefix is added in
	 *                   front of this string
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
	
	protected boolean subscribe(String topic, int qos) {
		IMqttClient mqttClient = this.mqttClient;
		if (mqttClient == null) {
			return false;
		}
		try {
			if(mqttClient.isConnected()) {
				this.logInfo(log, "Subscribed to [" + topic + "]");
				mqttClient.subscribe(topic, qos);
				return true;
			}
			return false;
		} catch (MqttException e) {
			this.logError(log, e.getMessage());
			return false;
		}
	};
	
	//sensor stuff
	
//	protected String sensor_init() {
//	
//		Map<String, Object> jsonData = new LinkedHashMap<>();
//		jsonData.put("type", "simulation");
//		jsonData.put("mode",0);
//		jsonData.put("id", sensorId);
//		jsonData.put("name", sensorId);
//		
//		LinkedHashMap<String,String> temperature = new LinkedHashMap<String,String>();		
//		temperature.put("name", "temperature");
//		temperature.put("description", "Temperature sensor -40 to 100 with 0.1 accuracy.");
//		temperature.put("unit", "degree of Celsius");
//		
//		LinkedHashMap<String,String> humidity = new LinkedHashMap<String,String>();
//		humidity.put("name", "humidity");
//		humidity.put("description", "Humidity sensor 0 to 100 with 0.5 accuracy.");
//		humidity.put("unit", "%");
//		
//		LinkedHashMap<String,String> windspeed = new LinkedHashMap<String,String>();
//		humidity.put("name", "windspeed");
//		humidity.put("description", "Windspeed sensor 0 to 1000 with 0.5 accuracy.");
//		humidity.put("unit", "km per hour");
//
//		ArrayList<HashMap<String, String>> measurements = new ArrayList<HashMap<String,String>>();
//		measurements.add(temperature);
//		measurements.add(humidity);
//		measurements.add(windspeed);
//		jsonData.put("measurements", measurements);
//
//		LinkedHashMap<String,String> cv = new LinkedHashMap<String,String>();
//		cv.put("name", "cv");
//		cv.put("description", "Central heating control from 10 tp 40 degree of Celsius.");
//		cv.put("unit", "degree of Celsius");
//
//		ArrayList<HashMap<String, String>> actuators = new ArrayList<HashMap<String,String>>();
//		actuators.add(cv);
//		jsonData.put("actuators", actuators);
//		
//		 Gson g = new Gson();
//		 return g.toJson(jsonData);
//	}
//	
	
//	protected String sensor_data(double temp, double humidity, double windspeed) {
//		
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
//		jsonData.put("id",sensorId);
//		jsonData.put("timestamp", time);
//		
//		LinkedHashMap<String,Object> data = new LinkedHashMap<String,Object>();
//		data.put("timestamp", time);
//		data.put("temperature", temp);
//		data.put("humidity", humidity);
//		if(windspeed != -1) {
//			data.put("windspeed",windspeed);
//		}
//	
//		ArrayList<HashMap<String, Object>> measurements = new ArrayList<HashMap<String,Object>>();
//		measurements.add(data);
//	
//		jsonData.put("measurements", measurements);
//		
//		Gson g = new Gson();
//		return g.toJson(jsonData);
//	}
//	
}
