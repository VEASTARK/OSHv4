package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.power.AncillaryCommodityLoadProfile;

import java.util.UUID;


/**
 * @author Sebastian Kramer
 */
public class GUIAncillaryMeterComExchange extends CALComExchange {

    private final AncillaryCommodityLoadProfile ancillaryMeter;


    /**
     * CONSTRUCTOR
     */
    public GUIAncillaryMeterComExchange(
            UUID deviceID,
            Long timestamp,
            AncillaryCommodityLoadProfile ancillaryMeter) {
        super(deviceID, timestamp);

        this.ancillaryMeter = ancillaryMeter.clone();
    }


    public AncillaryCommodityLoadProfile getAncillaryMeter() {
        return this.ancillaryMeter;
    }

}
