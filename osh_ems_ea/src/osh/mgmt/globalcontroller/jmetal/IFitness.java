package osh.mgmt.globalcontroller.jmetal;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;

import java.util.EnumMap;

/**
 * 
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 *
 */
public interface IFitness {
	
	/**
	 * 
	 * @param beginAt begin at this time
	 * @param plan current aggregated load profile for the next time, beginning at 0
	 * @param priceSignal price signal with unix timestamps
	 * @param powerSignal power limit signal with unix timestamps
	 * @return fitness value
	 */
    double getFitnessValue(
            long beginAt,
            long endAt,
            AncillaryCommodityLoadProfile ancillaryMeter,
            EnumMap<AncillaryCommodity, PriceSignal> priceSignals,
            EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimitSignals
    );

}
