package io.openems.edge.controller.api.mqtt.custom;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.common.channel.PersistencePriority;

/**
 * Config class for the MQTT custom component.
 * @author Nic
 *
 */
@ObjectClassDefinition(//
		name = "Controller Api MQTT Custom", //
		description = "This controller connects to an MQTT broker")
@interface Config {
	
	//Based on the original MQTT component, Changed values to SENDLab info.

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlControllerApiMqttCustom";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "SENDLab_OpenEMS";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	//Changed from Edge-ID to Client-ID
	//edge0 is the device/openems edge app
	@AttributeDefinition(name = "Client-ID", description = "Client-ID for authentication at MQTT broker")
	String clientId() default "SENDLab_OpenEMS";

	@AttributeDefinition(name = "Username", description = "Username for authentication at MQTT broker")
	String username() default "node";

	@AttributeDefinition(name = "Password", description = "Password for authentication at MQTT broker", type = AttributeType.PASSWORD)
	String password() default "smartmeternode";

	@AttributeDefinition(name = "Uri", description = "The connection Uri to MQTT broker.")
	String uri() default "tcp://sendlab.nl:11884";
	
	@AttributeDefinition(name = "Node Type", description = "The node meter type")
	NodeType type() default NodeType.DEFAULT;
	
	@AttributeDefinition(name = "Is subscriber?", description = "Is this Component a subscriber to a topic?")
	boolean subscriber() default true;
	
	@AttributeDefinition(name = "Topic", description = "The topic to which to subscribe to. (Has to be the same node type)")
	String[] topic();

	@AttributeDefinition(name = "Persistence Priority", description = "Send only Channels with a Persistence Priority greater-or-equals this.")
	PersistencePriority persistencePriority() default PersistencePriority.VERY_LOW;

	@AttributeDefinition(name = "Debug Mode", description = "Activates the debug mode")
	boolean debugMode() default false;

	String webconsole_configurationFactory_nameHint() default "Controller Api MQTT custom [{id}]";
}