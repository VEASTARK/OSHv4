package osh.datatypes.registry.oc.localobserver;

import osh.datatypes.registry.StateExchange;

import java.util.UUID;

//import osh.datatypes.energy.INeededEnergy;


/**
 * @author Jan Mueller, Sebastian Kochanneck
 */
public class PvIntelligentOCSX extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -889391896803816383L;
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
            long timestamp,
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
