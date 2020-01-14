package osh.datatypes.registry.oc.state.globalobserver;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.registry.StateExchange;

import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * Current power consumption of all devices covered by this EMS
 *
 * @author Ingo Mauser
 */
public class CommodityPowerStateExchange extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 2451111383309555786L;
    final EnumMap<Commodity, Double> powerState;
    final DeviceTypes deviceType;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public CommodityPowerStateExchange(
            UUID sender,
            long timestamp,
            DeviceTypes deviceType) {
        this(
                sender,
                timestamp,
                new EnumMap<>(Commodity.class),
                deviceType);
    }

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param powerState
     */
    public CommodityPowerStateExchange(
            UUID sender,
            long timestamp,
            EnumMap<Commodity, Double> powerState,
            DeviceTypes deviceType) {
        super(sender, timestamp);

        this.powerState = powerState;
        this.deviceType = deviceType;
    }


    public void addPowerState(Commodity commodity, double value) {
        this.powerState.put(commodity, value);
    }


    public Double getPowerState(Commodity commodity) {
        return this.powerState.get(commodity);
    }


    public EnumMap<Commodity, Double> getPowerState() {
        return this.powerState;
    }

    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    @Override
    public CommodityPowerStateExchange clone() {
        CommodityPowerStateExchange cloned = new CommodityPowerStateExchange(
                this.getSender(),
                this.getTimestamp(),
                this.deviceType);

        for (Entry<Commodity, Double> e : this.powerState.entrySet()) {
            cloned.addPowerState(e.getKey(), e.getValue());
        }

        return cloned;

    }

    @Override
    public String toString() {
        return "CommodityPowerState: " + this.powerState.toString();
    }

}
