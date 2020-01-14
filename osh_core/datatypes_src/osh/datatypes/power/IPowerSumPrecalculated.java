package osh.datatypes.power;

import osh.datatypes.commodity.Commodity;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public interface IPowerSumPrecalculated {

    /**
     * @param commodity
     * @param t
     * @return [CommodityLoadUnit * TimeUnit, e.g. Ws]
     */
    long getWork(Commodity commodity, long t);

    /**
     * @param commodity
     * @param t
     * @return [CommodityLoadUnit * TimeUnit, e.g. Ws]
     */
    long getPositiveWork(Commodity commodity, long t);

    /**
     * @param commodity
     * @param t
     * @return [CommodityLoadUnit * TimeUnit, e.g. Ws]
     */
    long getNegativeWork(Commodity commodity, long t);

    /**
     * @return timestamp
     */
    long getMiddleOfPowerConsumption(Commodity commodity);

}
