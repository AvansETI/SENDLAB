package io.openems.edge.controller.api.mqtt.custom;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Table;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

import io.openems.common.channel.AccessMode;
import io.openems.common.utils.StringUtils;
import io.openems.common.utils.ThreadPoolUtils;
import io.openems.edge.common.component.OpenemsComponent;

/**
 * Method {@link #collectData()} is called Synchronously with the Core.Cycle to
 * collect values of Channels. Sending of values is then delegated to an
 * asynchronous task.
 * 
 * <p>
 * The logic tries to send changed values once per Cycle and all values once
 * every {@link #SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS}.
 */
public class SendChannelValuesWorker {

	private static final int MQTT_QOS = 0; // loss is ok
	private static final boolean MQTT_RETAIN = true; // send last value to subscriber
	private static final int SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS = 300; /* 5 minutes */
	private static final MqttProperties MQTT_PROPERTIES;

	static {
		MQTT_PROPERTIES = new MqttProperties();
		// channel value is only valid for restricted time
		MQTT_PROPERTIES.setMessageExpiryInterval(Long.valueOf(SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS * 2));
	}

	private final Logger log = LoggerFactory.getLogger(SendChannelValuesWorker.class);
	private final MqttApiControllerImpl parent;

	private final ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.SECONDS,
			new ArrayBlockingQueue<>(1), //
			new ThreadFactoryBuilder().setNameFormat(MqttApiControllerImpl.COMPONENT_NAME + ":SendWorker-%d").build(), //
			new ThreadPoolExecutor.DiscardOldestPolicy());

	/**
	 * If true: next 'send' sends all channel values.
	 */
	private AtomicBoolean sendValuesOfAllChannels = new AtomicBoolean(true);

	/**
	 * Keeps the last timestamp when all channel values were sent.
	 */
	private Instant lastSendValuesOfAllChannels = Instant.MIN;

	/**
	 * Keeps the values of last successful send.
	 */
	private Table<String, String, JsonElement> lastAllValues = ImmutableTable.of();

	protected SendChannelValuesWorker(MqttApiControllerImpl parent) {
		this.parent = parent;
	}

	/**
	 * Triggers sending all Channel values once.
	 */
	public synchronized void sendValuesOfAllChannelsOnce() {
		this.sendValuesOfAllChannels.set(true);
	}

	/**
	 * Stops the {@link SendChannelValuesWorker}.
	 */
	public void deactivate() {
		// Shutdown executor
		ThreadPoolUtils.shutdownAndAwaitTermination(this.executor, 5);
	}
	
	boolean isInit = true;

	/**
	 * Called synchronously on AFTER_PROCESS_IMAGE event. Collects all the data and
	 * triggers asynchronous sending.
	 */
	public synchronized void collectData(String id) {
		Instant now = Instant.now(this.parent.componentManager.getClock());

		// Update the values of all channels
		final List<OpenemsComponent> enabledComponents = this.parent.componentManager.getEnabledComponents();
		final ImmutableTable<String, String, JsonElement> allValues = this.collectData(enabledComponents);
		
		//Prints entire table per row
//		this.parent.logInfo(log, "table: " + allValues.toString());

		// Add to send Queue
		this.executor.execute(new SendTask(this, now, allValues, id));
	}

	/**
	 * Cycles through all Channels and collects the value.
	 * 
	 * @param enabledComponents the enabled components
	 * @return collected data
	 */
	private ImmutableTable<String, String, JsonElement> collectData(List<OpenemsComponent> enabledComponents) {
		try {
			return enabledComponents.parallelStream() //
					.flatMap(component -> component.channels().parallelStream()) //
					.filter(channel -> // Ignore WRITE_ONLY Channels
					channel.channelDoc().getAccessMode() != AccessMode.WRITE_ONLY //
							// Ignore Low-Priority Channels
							&& channel.channelDoc().getPersistencePriority()
									.isAtLeast(this.parent.config.persistencePriority()))
					.collect(ImmutableTable.toImmutableTable(c -> c.address().getComponentId(),
							c -> c.address().getChannelId(), c -> c.value().asJson()));
			// TODO remove values for disappeared components
//			final Set<String> enabledComponentIds = enabledComponents.stream() //
//					.map(c -> c.id()) //
//					.collect(Collectors.toSet());
//			this.lastValues.rowMap().entrySet().stream() //
//					.filter(row -> !enabledComponentIds.contains(row.getKey())) //
//					.forEach(row -> {
//						row.getValue().entrySet().parallelStream() //
//								.forEach(column -> {
//									this.publish(row.getKey() + "/" + column.getKey(), JsonNull.INSTANCE.toString());
//								});
//					});
		} catch (Exception e) {
			// ConcurrentModificationException can happen if Channels are dynamically added
			// or removed
			return ImmutableTable.of();
		}
	}

	/*
	 * From here things run asynchronously.
	 */

	private static class SendTask implements Runnable {

		private final SendChannelValuesWorker parent;
		private final Instant timestamp;
		private final ImmutableTable<String, String, JsonElement> allValues;
		
		private final String id;

		public SendTask(SendChannelValuesWorker parent, Instant timestamp,
				ImmutableTable<String, String, JsonElement> allValues, String id) {
			this.parent = parent;
			this.timestamp = timestamp;
			this.allValues = allValues;
			this.id = id;
		}
		
		@Override
		public void run() {
			// Holds the data of the last successful send. If the table is empty, it is also
			// used as a marker to send all data.
			final Table<String, String, JsonElement> lastAllValues;

			if (this.parent.sendValuesOfAllChannels.getAndSet(false)) {
				// Send values of all Channels once in a while
				lastAllValues = ImmutableTable.of();

			} else if (Duration.between(this.parent.lastSendValuesOfAllChannels, this.timestamp)
					.getSeconds() > SEND_VALUES_OF_ALL_CHANNELS_AFTER_SECONDS) {
				// Send values of all Channels if explicitly asked for
				lastAllValues = ImmutableTable.of();

			} else {
				// Actually use the kept 'lastSentValues'
				lastAllValues = this.parent.lastAllValues;
			}
			
			Map<String, Object> jsonData = new LinkedHashMap<>();
			if(this.parent.isInit) {
				jsonData.put("type", "simulation");
				jsonData.put("mode", 0);
				jsonData.put("id", id);
				jsonData.put("name", id);
			}else {
				Timestamp instant= Timestamp.from(Instant.now());
				String time = instant.toString();

				String[] s = time.split(" ");
				String b = s[0]; 
				String c = s[1]; 
				time = b + "T" + c;
				
				jsonData.put("id",id);
				jsonData.put("timestamp", time);
			}
			
			ArrayList<HashMap<String, String>> measurementsInit = new ArrayList<HashMap<String,String>>();
			ArrayList<HashMap<String, Object>> measurementsData = new ArrayList<HashMap<String,Object>>();
			
			LinkedHashMap<String,String> mapInit = new LinkedHashMap<String,String>();
			LinkedHashMap<String,Object> mapData = new LinkedHashMap<String,Object>();
			
			// Send changed values
			boolean allSendSuccessful = true;
			for (Entry<String, Map<String, JsonElement>> row : this.allValues.rowMap().entrySet()) {
				for (Entry<String, JsonElement> column : row.getValue().entrySet()) {
					if (!Objects.equals(column.getValue(), lastAllValues.get(row.getKey(), column.getKey()))) {
						String subtopic = row.getKey() + "/" + column.getKey();
						
						if(this.parent.isInit ) {
							
							//this does not work
							mapInit = new LinkedHashMap<String,String>();
							mapInit.put("name", subtopic);
							mapInit.put("description", subtopic);
							mapInit.put("unit", "class com.google.gson.JsonPrimitive");

							measurementsInit.add(mapInit);
							
						}else {
							mapData.put(subtopic,column.getValue().toString());
						}
						
						//sends the topic
						if (!this.publish(row.getKey() + "/" + column.getKey(), column.getValue().toString())) {
							allSendSuccessful = false;
						}
					}
				}
			}

			Gson g = new Gson();
			String parsedData;
			
			if(this.parent.isInit) {				
				jsonData.put("measurements", measurementsInit);
				
				parsedData = g.toJson(jsonData);
				if(this.publish("node/init", parsedData)) {
					this.parent.isInit = false;
				}
				
			}else {			
				measurementsData.add(mapData);
				jsonData.put("measurements", measurementsData);
				
				parsedData = g.toJson(jsonData);
				this.publish("node/data", parsedData);
			}

			// Successful?
			if (allSendSuccessful) {
				this.printLog("Sucessfully sent MQTT data to broker");
			
				// update information for next runs
				this.parent.lastAllValues = this.allValues;
				if (lastAllValues.isEmpty()) {
					// 'lastSentValues' was empty, i.e. all values were sent
					this.parent.lastSendValuesOfAllChannels = this.timestamp;
				}
			} else {
				this.parent.parent.logWarn(this.parent.log, "Error while sending MQTT data to broker");
			}
		}

		/**
		 * Publish a Channel value message.
		 * 
		 * @param subTopic the Channel Subtopic
		 * @param value    the value Json.toString()
		 * @return true if sent successfully; false otherwise
		 */
		private boolean publish(String topic, String value) {
			return this.parent.parent.publish(topic, value, 0, true, new MqttProperties());	
		}
		
		private void printLog(String value) {
			this.parent.parent.logInfo(this.parent.log, value);
		}

	}

}