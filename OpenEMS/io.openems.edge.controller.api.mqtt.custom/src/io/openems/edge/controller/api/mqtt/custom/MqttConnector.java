package io.openems.edge.controller.api.mqtt.custom;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.paho.mqttv5.client.IMqttClient;
import org.eclipse.paho.mqttv5.client.MqttCallback;
import org.eclipse.paho.mqttv5.client.MqttClient;
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions;
import org.eclipse.paho.mqttv5.common.MqttException;

/**
 * This helper class wraps a connection to an MQTT broker.
 * 
 * <p>
 * One main feature of this class is to retry the initial connection to an MQTT
 * broker. A feature that is unfortunately not present in Eclipse Paho. After
 * the first successful connection, Paho reconnects on its own in case of a lost
 * connection.
 */
public class MqttConnector {

	private static final int INCREASE_WAIT_SECONDS = 5;
	private static final int MAX_WAIT_SECONDS = 60 * 5;
	private AtomicInteger waitSeconds = new AtomicInteger(0);

	/**
	 * Private inner class handles actual connection. It is executed via the
	 * ScheduledExecutorService.
	 */
	private final class MyConnector implements Runnable {

		private final CompletableFuture<IMqttClient> result = new CompletableFuture<>();
		private final IMqttClient client;
		private final MqttConnectionOptions options;

		private MyConnector(IMqttClient client, MqttConnectionOptions options) {
			this.client = client;
			this.options = options;
		}

		@Override
		public void run() {
			try {
				this.client.connect(this.options);
				this.result.complete(this.client);
				System.out.println(new Date() + ": Connection to broker complete."); // TODO
				
			} catch (Exception e) {
				System.out.println(new Date() + ": Failed to connect to broker."); // TODO
				System.out.println(e.getMessage()); // TODO
				MqttConnector.this.waitAndRetry();
			}
		}
	}

	private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

	private MyConnector connector;

	/**
	 * Shutsdown connector and executor.
	 */
	protected synchronized void deactivate() {
		this.connector = null;
		this.executor.shutdownNow();
	}

	protected synchronized CompletableFuture<IMqttClient> connect(String serverUri, String clientId, String username,
			String password) throws IllegalArgumentException, MqttException {
		return this.connect(serverUri, clientId, username, password, null);
	}

	/**
	 * Async method for connecting to the MQTTclient.
	 * @param serverUri	The MQTT broker address.
	 * @param clientId 	The MQTT broker client ID.
	 * @param username	The username to connect to the MQTT broker.
	 * @param password	The password to connect to the MQTT broker.
	 * @param callback	The callback to receive MQTT messages from a topic.
	 * @return
	 * @throws IllegalArgumentException
	 * @throws MqttException
	 */
	protected synchronized CompletableFuture<IMqttClient> connect(String serverUri, String clientId, String username,
			String password, MqttCallback callback) throws IllegalArgumentException, MqttException {
		IMqttClient client = new MqttClient(serverUri, clientId);
		if (callback != null) {
			client.setCallback(callback);
		}

		MqttConnectionOptions options = new MqttConnectionOptions();
		options.setUserName(username);
		if (password != null) {
			options.setPassword(password.getBytes(StandardCharsets.UTF_8));
		}
		options.setAutomaticReconnect(true);
		options.setCleanStart(true);
		options.setKeepAliveInterval(30);
		options.setConnectionTimeout(10); //10

		this.connector = new MyConnector(client, options);

		this.executor.schedule(this.connector, 0 /* immediately */, TimeUnit.SECONDS);
		return this.connector.result;
	}

	/**
	 * Tries to reconnect to the broker.
	 */
	private void waitAndRetry() {
		this.waitSeconds.getAndUpdate(oldValue -> Math.min(oldValue + INCREASE_WAIT_SECONDS, MAX_WAIT_SECONDS));
		this.executor.schedule(this.connector, this.waitSeconds.get(), TimeUnit.SECONDS);
	}

}
