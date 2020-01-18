package osh.comdriver.signals;

import osh.datatypes.limit.PowerLimitSignal;

/**
 * @author Sebastian Kramer
 */
public class PowerLimitSignalGenerator {

    public static PowerLimitSignal generateFlatPowerLimitSignal(long startOfSignal, long endOfSignal, int upperLimit,
                                                                int lowerLimit) {
        PowerLimitSignal powerLimitSignal = new PowerLimitSignal();
        powerLimitSignal.setPowerLimit(startOfSignal, upperLimit, lowerLimit);
        powerLimitSignal.setKnownPowerLimitInterval(startOfSignal, endOfSignal);

        return powerLimitSignal;
    }

}
