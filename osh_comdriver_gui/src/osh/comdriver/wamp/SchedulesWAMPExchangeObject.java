package osh.comdriver.wamp;

import com.fasterxml.jackson.annotation.JsonProperty;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;

import java.util.Map;

public class SchedulesWAMPExchangeObject {

    @JsonProperty("priceSignals")
    private Map<AncillaryCommodity, PriceSignal> priceSignals;
    @JsonProperty("powerLimitSignals")
    private Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals;
    @JsonProperty("schedules")
    private Map<String, Map<Commodity, Map<Long, Integer>>> schedules;

    public SchedulesWAMPExchangeObject(Map<AncillaryCommodity, PriceSignal> priceSignals,
                                       Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals, Map<String, Map<Commodity, Map<Long, Integer>>> schedules) {
        super();
        this.priceSignals = priceSignals;
        this.powerLimitSignals = powerLimitSignals;
        this.schedules = schedules;
    }

    public Map<AncillaryCommodity, PriceSignal> getPriceSignals() {
        return this.priceSignals;
    }

    public void setPriceSignals(Map<AncillaryCommodity, PriceSignal> priceSignals) {
        this.priceSignals = priceSignals;
    }

    public Map<AncillaryCommodity, PowerLimitSignal> getPowerLimitSignals() {
        return this.powerLimitSignals;
    }

    public void setPowerLimitSignals(Map<AncillaryCommodity, PowerLimitSignal> powerLimitSignals) {
        this.powerLimitSignals = powerLimitSignals;
    }

    public Map<String, Map<Commodity, Map<Long, Integer>>> getSchedules() {
        return this.schedules;
    }

    public void setSchedules(Map<String, Map<Commodity, Map<Long, Integer>>> schedules) {
        this.schedules = schedules;
    }

}
