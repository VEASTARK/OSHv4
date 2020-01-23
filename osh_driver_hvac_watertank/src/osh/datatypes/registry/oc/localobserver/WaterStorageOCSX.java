package osh.datatypes.registry.oc.localobserver;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Florian Allerding, Till Schuberth, Ingo Mauser
 */
public class WaterStorageOCSX extends StateExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = 7810796095763895290L;
    private final UUID tankId;
    private final double currentTemp;
    private final double minTemp;
    private final double maxTemp;
    private final double demand;
    private final double supply;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     * @param currentTemp
     * @param minTemp
     * @param maxTemp
     */
    public WaterStorageOCSX(
            UUID sender,
            ZonedDateTime timestamp,
            double currentTemp,
            double minTemp,
            double maxTemp,
            double demand,
            double supply,
            UUID tankId) {
        super(sender, timestamp);

        this.currentTemp = currentTemp;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.demand = demand;
        this.supply = supply;
        this.tankId = tankId;
    }

    public double getCurrentTemp() {
        return this.currentTemp;
    }

    public double getMinTemp() {
        return this.minTemp;
    }

    public double getMaxTemp() {
        return this.maxTemp;
    }

    public double getDemand() {
        return this.demand;
    }

    public double getSupply() {
        return this.supply;
    }

    public UUID getTankId() {
        return this.tankId;
    }


    public boolean equalData(WaterStorageOCSX o) {
        if (o != null) {

            //compare using an epsilon environment
            return Math.abs(this.currentTemp - o.currentTemp) < 0.001 &&
                    Math.abs(this.minTemp - o.minTemp) < 0.001 &&
                    Math.abs(this.maxTemp - o.maxTemp) < 0.001 &&
                    Math.abs(this.demand - o.demand) < 0.001 &&
                    Math.abs(this.supply - o.supply) < 0.001
                    && ((this.tankId != null && this.tankId.equals(o.tankId)) || (this.tankId == null && o.tankId == null));
        }

        return false;
    }
}
