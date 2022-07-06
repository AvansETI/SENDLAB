package io.openems.edge.smartmeter;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.openems.common.channel.PersistencePriority;

@ObjectClassDefinition(//
		name = "Smartmeter", //
		description = "")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "smartmeter0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;
	
	@AttributeDefinition(name = "Persistence Priority", description = "Send only Channels with a Persistence Priority greater-or-equals this.")
	PersistencePriority persistencePriority() default PersistencePriority.VERY_LOW;

	String webconsole_configurationFactory_nameHint() default "Smartmeter [{id}]";

}