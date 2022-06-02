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

	protected Config config;
	private String topicPrefix;

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
	
	private String sensorId = "SENDLAB_test"; 
	
	Timestamp instant= Timestamp.from(Instant.now());
	
	@Activate
	void activate(ComponentContext context, Config config) throws Exception {
		this.config = config;

		// Publish MQTT messages under the topic "edge/edge0/..."
		this.topicPrefix = String.format(MqttApiController.TOPIC_PREFIX, config.clientId());

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
						//this.subscribe(topic, 0);
					}
					
					
					//current topic
					//node/smartmeter-2019-ETI-EMON-V01-DADDE2-16301C/data
					
					//TODO - choose correct init data (if necessary)
					if(
//							//testing only on local broker
//							//this.subscribe("$SYS/#", 0) &&
//						this.subscribe("node/init",0) &&
//						this.subscribe("node/data",0) && 
						this.subscribe("node/" + sensorId + "/message", 0) && 
						this.subscribe("node/" + sensorId + "/data", 0)) {
//					//This works just needs to fix publish for data
							this.logInfo(log, "publish init message");
							this.publish("node/init", initv2(), 0, true, new MqttProperties());
							
							int temp = 21;
							int humidity = 56;   
							while(true) {
								int timediff = Timestamp.from(Instant.now()).getSeconds() - instant.getSeconds();
									if ( timediff > 1 ) {
										this.publish("node/data", testDatav2(temp,humidity),0,true,new MqttProperties());
										temp += 1;
										humidity += 1;
										instant = Timestamp.from(Instant.now());
								    };
							}
					}
			
				}
			});		
	}
	
	protected MqttApiCallbackImpl getCallback(NodeType type) {
		switch (type){
		case SMARTMETER:
//			return new MqttApiCallbackSmartMeterImpl();
			return new MqttApiCallbackImpl();
			
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
		this.mqttConnector.deactivate();
		this.sendChannelValuesWorker.deactivate();
		if (this.mqttClient != null) {
			try {
				this.mqttClient.close();				
			} catch (MqttException e) {
				this.logWarn(this.log, "Unable to close connection to MQTT brokwer: " + e.getMessage());
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
				if(!this.config.subscriber()){
					this.sendChannelValuesWorker.collectData();
				}
				break;
			
			case EdgeEventConstants.TOPIC_CONFIG_UPDATE: 
			
				if(!this.config.subscriber()){
				
					// Send new EdgeConfig
					EdgeConfig config = (EdgeConfig) event.getProperty(EdgeEventConstants.TOPIC_CONFIG_UPDATE_KEY);
				
					this.publish(topicPrefix + MqttApiController.TOPIC_EDGE_CONFIG, config.toJson().toString(), //
							1 /* QOS */, true /* retain */, new MqttProperties() /* no specific properties */);
			
					// Trigger sending of all channel values, because a Component might have
					// disappeared
					this.sendChannelValuesWorker.sendValuesOfAllChannelsOnce();
				}
				break;
				
				//TODO find out what data to send to the broker
				//TODO find out how to get data from sensors.
				//
				//Example
				//https://teams.microsoft.com/l/channel/19%3Aepsz0EZPYfoal2Gx_THoFESuBWJh2Ch4pEBNgCg7u4I1%40thread.tacv2/tab%3A%3Ac010cc5e-91d4-469d-84ca-1c2d7818164c?groupId=12067b0a-d449-42a8-9c38-dded60398970&tenantId=87c50b58-2ef2-423d-a4db-1fa7c84efcfa
				
				//Can be used to send data after writing
			case EdgeEventConstants.TOPIC_CYCLE_AFTER_WRITE:
				
//				this.publish("TEST/", "Test", 0, false, new MqttProperties());

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
	
	class measurement{
		
		String name;
		String description;
		String unit;
		
		measurement(String name, String description, String unit){
			this.name = name;
			this.description = description;
			this.unit = unit;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getDescription() {
			return description;
		}

		public void setDescription(String description) {
			this.description = description;
		}

		public String getUnit() {
			return unit;
		}

		public void setUnit(String unit) {
			this.unit = unit;
		};

	}
	
	protected String initv2() {
	
		Map<String, Object> sensor_init = new LinkedHashMap<>();
		
		sensor_init.put("type", "simulation");
		sensor_init.put("mode",0);
		sensor_init.put("id", sensorId);
		sensor_init.put("name", sensorId);
		
		 measurement[] measurements = new measurement[] {		
				new measurement(
				   		"temperature",
				   		"Temperature sensor -40 to 100 with 0.1 accuracy.",
				    	"degree of Celsius"
		    	), new measurement(
		    			"humidity",
		    	        "Humidity sensor 0 to 100 with 0.5 accuracy.",
		    	        "%"
		    	),
		 };
		 
		 sensor_init.put("measurements", measurements);
		
		 measurement[] actuators = new measurement[] {	
				 new measurement(
		 
					"cv",
				    "Central heating control from 10 tp 40 degree of Celsius.",
				    "degree of Celsius"
		    )
		 };
		 
		 sensor_init.put("actuators", actuators);
		
		 Gson g = new Gson();
		 return g.toJson(sensor_init);
	}
	
	class meterData {

		String timestamp;
		double temperature;
		double humidity;
		
		meterData(String timestamp, double temperature, double humidity){
			this.timestamp = timestamp;
			this.temperature = temperature;
			this.humidity = humidity;
		}
		

		public String getTimestamp() {
			return timestamp;
		}

		public void setTimestamp(String timestamp) {
			this.timestamp = timestamp;
		}

		public double getTemperature() {
			return temperature;
		}

		public void setTemperature(double temperature) {
			this.temperature = temperature;
		}

		public double getHumidity() {
			return humidity;
		}

		public void setHumidity(double humidity) {
			this.humidity = humidity;
		}

	}

	protected String testDatav2(int temp, int humidity) {
		
		Map<String, Object> data = new LinkedHashMap<>();
		
		Timestamp instant= Timestamp.from(Instant.now());
		String time = instant.toString();

		String[] s = time.split(" ");
		String b = s[0]; 
		String c = s[1]; 
		time = b + "T" + c;
		
		data.put("id",sensorId);
		data.put("timestamp", time);
		meterData[] measurements = new meterData[] {
			new meterData(time,temp,humidity)
		};
		data.put("measurements", measurements);
		
		Gson g = new Gson();
		return g.toJson(data);
	}
}
