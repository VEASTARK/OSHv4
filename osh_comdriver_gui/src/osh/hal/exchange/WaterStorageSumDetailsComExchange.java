package osh.hal.exchange;

import osh.cal.CALComExchange;
import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;

import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class WaterStorageSumDetailsComExchange extends CALComExchange implements EqualData<WaterStorageSumDetailsComExchange> {

    private final int maxDeltaForEquality = 4;
    private final double posSum;
    private final double negSum;
    private final double sum;


    /**
     * CONSTRUCTOR
     */
    public WaterStorageSumDetailsComExchange(UUID deviceID, Long timestamp, double posSum, double negSum, double sum) {
        super(deviceID, timestamp);
        this.posSum = posSum;
        this.negSum = negSum;
        this.sum = sum;
    }


    public double getPosSum() {
        return this.posSum;
    }

    public double getNegSum() {
        return this.negSum;
    }

    public double getSum() {
        return this.sum;
    }

    @Override
    public boolean equalData(WaterStorageSumDetailsComExchange o) {
        return (Math.abs(this.posSum - o.posSum) < this.maxDeltaForEquality &&
                Math.abs(this.negSum - o.negSum) < this.maxDeltaForEquality &&
                Math.abs(this.sum - o.sum) < this.maxDeltaForEquality);

    }

}
