package io.openems.edge.smartmeter;

import org.osgi.service.event.EventHandler;

import io.openems.common.channel.AccessMode;
import io.openems.common.channel.PersistencePriority;
import io.openems.common.channel.Unit;
import io.openems.common.types.OpenemsType;
import io.openems.edge.common.channel.Doc;
import io.openems.edge.common.channel.DoubleWriteChannel;
import io.openems.edge.common.channel.IntegerWriteChannel;
import io.openems.edge.common.channel.StringWriteChannel;
import io.openems.edge.common.component.OpenemsComponent;

public interface Smartmeter extends OpenemsComponent, EventHandler {

    public enum ChannelId implements io.openems.edge.common.channel.ChannelId {
    	ID(Doc.of(OpenemsType.STRING).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
                .persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_DELIVERED_TARRIF_1(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
                .persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_DELIVERED_TARRIF_2(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
                .persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_RECEIVED_TARRIF_1(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_RECEIVED_TARRIF_2(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	TARRIF_INDICATOR(Doc.of(OpenemsType.INTEGER).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	ACTUAL_POWER_DELIVERED(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	ACTUAL_POWER_RECEIVED(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	GAS_DELIVERED(Doc.of(OpenemsType.DOUBLE).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_DELIVERED(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	ENERGY_RECEIVED(Doc.of(OpenemsType.DOUBLE).unit(Unit.KILOWATT_HOURS).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),
    	TIMESTAMP(Doc.of(OpenemsType.STRING).unit(Unit.NONE).accessMode(AccessMode.READ_WRITE)
    			.persistencePriority(PersistencePriority.HIGH)),;
    
        private final Doc doc;

        private ChannelId(Doc doc) {
            this.doc = doc;
        }

        @Override
        public Doc doc() {
            return this.doc;
        }
    }

    public default StringWriteChannel getId() {
        return this.channel(ChannelId.ID);
    }

    public default void _setId(String value) {
        this.getId().setNextValue(value);
    }
    
    public default DoubleWriteChannel getEnergyDeliveredTarrif1() {
        return this.channel(ChannelId.ENERGY_DELIVERED_TARRIF_1);
    }

    public default void _setEnergyDeliveredTarrif1(double value) {
        this.getEnergyDeliveredTarrif1().setNextValue(value);
    }
    
    public default DoubleWriteChannel getEnergyDeliveredTarrif2() {
        return this.channel(ChannelId.ENERGY_DELIVERED_TARRIF_2);
    }

    public default void _setEnergyDeliveredTarrif2(double value) {
        this.getEnergyDeliveredTarrif2().setNextValue(value);
    }
    
    public default DoubleWriteChannel getEnergyReceivedTarrif1() {
        return this.channel(ChannelId.ENERGY_RECEIVED_TARRIF_1);
    }

    public default void _setEnergyReceivedTarrif1(double value) {
        this.getEnergyReceivedTarrif1().setNextValue(value);
    }
    public default DoubleWriteChannel getEnergyReceivedTarrif2() {
        return this.channel(ChannelId.ENERGY_RECEIVED_TARRIF_2);
    }

    public default void _setEnergyReceivedTarrif2(double value) {
        this.getEnergyReceivedTarrif2().setNextValue(value);
    }
    

    public default IntegerWriteChannel getTarrifIndicator() {
        return this.channel(ChannelId.TARRIF_INDICATOR);
    }

    public default void _setTarrifIndicator(int value) {
        this.getTarrifIndicator().setNextValue(value);
    }
    
    public default DoubleWriteChannel getActualPowerDelivered() {
        return this.channel(ChannelId.ACTUAL_POWER_DELIVERED);
    }

    public default void _setActualPowerDelivered(double value) {
        this.getActualPowerDelivered().setNextValue(value);
    }
    
    public default DoubleWriteChannel getActualPowerReceived() {
        return this.channel(ChannelId.ACTUAL_POWER_RECEIVED);
    }

    public default void _setActualPowerReceived(double value) {
        this.getActualPowerReceived().setNextValue(value);
    }
    
    public default DoubleWriteChannel getGasDelivered() {
        return this.channel(ChannelId.GAS_DELIVERED);
    }

    public default void _setGasDelivered(double value) {
        this.getGasDelivered().setNextValue(value);
    }
    
    public default DoubleWriteChannel getEnergyDelivered() {
        return this.channel(ChannelId.ENERGY_DELIVERED);
    }

    public default void _setEnergyDelivered(double value) {
        this.getEnergyDelivered().setNextValue(value);
    }
    
    public default DoubleWriteChannel getEnergyReceived() {
        return this.channel(ChannelId.ENERGY_RECEIVED);
    }

    public default void _setEnergyReceived(double value) {
        this.getEnergyReceived().setNextValue(value);
    }

    public default StringWriteChannel getTimestamp() {
        return this.channel(ChannelId.TIMESTAMP);
    }

    public default void _setTimestamp(String value) {
        this.getTimestamp().setNextValue(value);
    }


    public Config getConfig();
}
    