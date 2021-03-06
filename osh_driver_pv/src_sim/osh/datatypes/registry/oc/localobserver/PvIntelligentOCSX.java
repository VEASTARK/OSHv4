package osh.datatypes.registry.oc.localobserver;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import java.time.ZonedDateTime;
import java.util.UUID;

//import osh.datatypes.energy.INeededEnergy;


/**
 * @author Jan Mueller, Sebastian Kochanneck
 */
public class PvIntelligentOCSX extends StateExchange implements IPromiseToBeImmutable {

    private final UUID pvIntelligentId;
    private final double pMaxLim;
    private final double qMaxLim;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public PvIntelligentOCSX(
            UUID sender,
            ZonedDateTime timestamp,
            double maxActivePower,
            double maxReactivePower,
            double currentActivePower,
            double currentReactivePower,
            UUID pvIntelligentId
    ) {
        super(sender, timestamp);

        this.pMaxLim = maxActivePower;
        this.qMaxLim = maxReactivePower;
        this.pvIntelligentId = pvIntelligentId;
    }

    public UUID getPvIntelligentId() {
        return this.pvIntelligentId;
    }


    public double getMaxActivePower() {
        return this.pMaxLim;
    }

    public double getMaxReactivePower() {
        return this.qMaxLim;
    }

    public boolean equalData(PvIntelligentOCSX o) {
        if (o != null) {

            //compare using an epsilon environment
            return Math.abs(this.pMaxLim - o.pMaxLim) < 0.001 &&
                    Math.abs(this.qMaxLim - o.qMaxLim) < 0.001
                    && ((this.pvIntelligentId != null && this.pvIntelligentId.equals(o.pvIntelligentId)) || (this.pvIntelligentId == null && o.pvIntelligentId == null));
        }

        return false;
    }
}
