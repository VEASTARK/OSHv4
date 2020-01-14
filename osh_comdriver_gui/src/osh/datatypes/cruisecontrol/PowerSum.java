package osh.datatypes.cruisecontrol;

import osh.datatypes.cruisecontrol.OptimizedDataStorage.EqualData;


/**
 * @author Ingo Mauser
 */
public class PowerSum implements EqualData<PowerSum> {

    public final double posSum;
    public final double negSum;
    public final double sum;


    /**
     * CONSTRUCTOR
     */
    public PowerSum(double posSum, double negSum, double sum) {
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
    public boolean equalData(PowerSum o) {
        return (Math.abs(this.posSum - o.posSum) < 4 &&
                Math.abs(this.negSum - o.negSum) < 4 &&
                Math.abs(this.sum - o.sum) < 4);

    }

}
