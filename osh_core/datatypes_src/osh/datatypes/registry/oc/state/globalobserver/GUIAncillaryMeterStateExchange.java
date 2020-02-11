package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


public class GUIAncillaryMeterStateExchange extends StateExchange {

    private AncillaryCommodityLoadProfile ancillaryMeter;

    public GUIAncillaryMeterStateExchange(UUID sender, ZonedDateTime timestamp,
                                          AncillaryCommodityLoadProfile ancillaryMeter) {
        super(sender, timestamp);
        this.ancillaryMeter = ancillaryMeter;
    }

    public AncillaryCommodityLoadProfile getAncillaryMeter() {
        return this.ancillaryMeter;
    }

}
