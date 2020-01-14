package osh.hal.exchange;

import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;

import java.util.EnumMap;
import java.util.Set;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class BaseloadObserverExchange
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails, IHALGasPowerDetails {

    private final EnumMap<Commodity, Integer> powerMap;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public BaseloadObserverExchange(
            UUID deviceID,
            Long timestamp) {
        super(deviceID, timestamp);

        this.powerMap = new EnumMap<>(Commodity.class);
    }


    public Integer getPower(Commodity c) {
        return this.powerMap.get(c);
    }

    public void setPower(Commodity c, int power) {
        this.powerMap.put(c, power);
    }

    public Set<Commodity> getCommodities() {
        return this.powerMap.keySet();
    }

    @Override
    public int getGasPower() {
        return this.powerMap.get(Commodity.NATURALGASPOWER);
    }

    @Override
    public int getActivePower() {
        return this.powerMap.get(Commodity.ACTIVEPOWER);
    }

    @Override
    public int getReactivePower() {
        return this.powerMap.get(Commodity.REACTIVEPOWER);
    }

}
