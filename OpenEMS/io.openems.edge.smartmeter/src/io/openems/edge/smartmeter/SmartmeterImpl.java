package io.openems.edge.smartmeter;

import java.util.List;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.metatype.annotations.Designate;

import com.google.common.collect.ImmutableTable;
import com.google.gson.JsonElement;

import io.openems.common.channel.AccessMode;
import io.openems.edge.common.component.AbstractOpenemsComponent;
import io.openems.edge.common.component.ComponentManager;
import io.openems.edge.common.component.OpenemsComponent;
import io.openems.edge.common.event.EdgeEventConstants;

@Designate(ocd = Config.class, factory = true)
@Component(//
		name = "Smartmeter", //
		immediate = true, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, //
		property = { //
				EventConstants.EVENT_TOPIC + "=" + EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE //
		} //
)
public class SmartmeterImpl extends AbstractOpenemsComponent implements Smartmeter, OpenemsComponent, EventHandler {

	@Reference
	protected ComponentManager componentManager;
	
	private Config config = null;

	public SmartmeterImpl() {
		super(//
				OpenemsComponent.ChannelId.values(), //
				Smartmeter.ChannelId.values() //
		);
		
	}

	@Activate
	void activate(ComponentContext context, Config config) {
		super.activate(context, config.id(), config.alias(), config.enabled());
		this.config = config;
	}

	@Deactivate
	protected void deactivate() {
		super.deactivate();
	}

	@Override
	public void handleEvent(Event event) {
		if (!this.isEnabled()) {
			return;
		}
		switch (event.getTopic()) {
		case EdgeEventConstants.TOPIC_CYCLE_BEFORE_PROCESS_IMAGE:
			

			final List<OpenemsComponent> enabledComponents = this.componentManager.getEnabledComponents();

			final ImmutableTable<String, String, JsonElement> allValues = this.collectData(enabledComponents);
			
			OpenemsComponent q;
			
			for(OpenemsComponent c : enabledComponents) {
				
				if(c.id().contains("ApiMqtt")) {
					q = c;
					break;
				}
				
			}
			
			
			// TODO: fill channels
			break;
		}
	}

	@Override
	public String debugLog() {
		return "Hello World" + this.getTimestamp();
	}

	@Override
	public Config getConfig() {
		return this.config;
	}
	
	
	private ImmutableTable<String, String, JsonElement> collectData(List<OpenemsComponent> enabledComponents) {
		try {
			return enabledComponents.parallelStream() //
					.flatMap(component -> component.channels().parallelStream()) //
					.filter(channel -> // Ignore WRITE_ONLY Channels
					channel.channelDoc().getAccessMode() != AccessMode.WRITE_ONLY //
							// Ignore Low-Priority Channels
							&& channel.channelDoc().getPersistencePriority()
									.isAtLeast(this.config.persistencePriority()))
					.collect(ImmutableTable.toImmutableTable(c -> c.address().getComponentId(),
							c -> c.address().getChannelId(), c -> c.value().asJson()));
		} catch (Exception e) {
			// ConcurrentModificationException can happen if Channels are dynamically added
			// or removed
			return ImmutableTable.of();
		}
	}
	
	
}
