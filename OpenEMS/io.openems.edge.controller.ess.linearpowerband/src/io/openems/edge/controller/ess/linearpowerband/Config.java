package io.openems.edge.controller.ess.linearpowerband;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(//
		name = "Controller Ess Linear Power Band", //
		description = "Defines a fixed max/min power to a symmetric energy storage system.")
@interface Config {

	@AttributeDefinition(name = "Component-ID", description = "Unique ID of this Component")
	String id() default "ctrlLinearPowerBand0";

	@AttributeDefinition(name = "Alias", description = "Human-readable name of this Component; defaults to Component-ID")
	String alias() default "";

	@AttributeDefinition(name = "Is enabled?", description = "Is this Component enabled?")
	boolean enabled() default true;

	@AttributeDefinition(name = "Ess-ID", description = "ID of Ess device.")
	String ess_id() default "ess0";

	@AttributeDefinition(name = "Min power [W]", description = "Negative values for Charge; positive for Discharge")
	int minPower() default -10_000;

	@AttributeDefinition(name = "Max power [W]", description = "Negative values for Charge; positive for Discharge")
	int maxPower() default 10_000;

	@AttributeDefinition(name = "Adjust per Cycle [W]", description = "Adjustments of power per Cycle")
	int adjustPower() default 10;

	@AttributeDefinition(name = "Start direction", description = "Start with Charge or Discharge")
	StartDirection startDirection() default StartDirection.CHARGE;

	@AttributeDefinition(name = "Ess target filter", description = "This is auto-generated by 'Ess-ID'.")
	String ess_target() default "(enabled=true)";

	String webconsole_configurationFactory_nameHint() default "Controller Ess Linear Power Band [{id}]";
}