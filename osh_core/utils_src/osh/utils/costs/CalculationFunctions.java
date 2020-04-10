package osh.utils.costs;

/**
 * Represents a collection of simple calculation function in use in the {@link OptimizationCostFunction}.
 *
 * @author Sebastian Kramer
 */
public class CalculationFunctions {

    private static double upperOverlimitFactor;
    private static double lowerOverlimitFactor;

    /**
     * Sets the overlimit factors for pls violations.
     *
     * @param upperOverlimitFactor the overlimit factor for upper pls violations
     * @param lowerOverlimitFactor the overlimit factor for lower pls violations
     */
    public static void setOverlimitFactors(double upperOverlimitFactor, double lowerOverlimitFactor) {
        CalculationFunctions.upperOverlimitFactor = upperOverlimitFactor;
        CalculationFunctions.lowerOverlimitFactor = lowerOverlimitFactor;
    }

    public static double pricePowerFunction(double price, double power, long timeFactor) {
        return price * power * timeFactor;
    }

    public static double priceAbsPowerFunction(double price, double power, long timeFactor) {
        return price * Math.abs(power) * timeFactor;
    }

    public static double positivePowerFunction(double power, long timeFactor) {
        if (power <= 0) {
            return 0.0;
        } else {
            return power * timeFactor;
        }
    }

    public static double pricePositivePowerFunction(double price, double power, long timeFactor) {
        if (power <= 0) {
            return 0.0;
        } else {
            return price * power * timeFactor;
        }
    }

    public static double priceNegativePowerFunction(double price, double power, long timeFactor) {
        if (power >= 0) {
            return 0.0;
        } else {
            return price * power * timeFactor;
        }
    }

    public static double priceAbsPowerLimitFunction(double price, double upperLimit, double lowerLimit, double power,
                                                    long timeFactor) {
        double base = 0.0;
        if (power > 0) {
            base = price * Math.abs(power) * timeFactor;
        }
        if (power <= upperLimit || power >= lowerLimit) {
            return base;
        } else {
            if (power < lowerLimit) {
                return base + lowerOverlimitFactor * price * (Math.abs(lowerLimit) - Math.abs(power)) * timeFactor;
            } else {
                return base + upperOverlimitFactor * price * (Math.abs(power) - Math.abs(upperLimit)) * timeFactor;
            }
        }
    }

    public static double pricePositivePowerLimitFunction(double price, double upperLimit, double power, long timeFactor) {
        if (power <= 0) {
            return 0.0;
        }
        if (power <= upperLimit) {
            return price * power * timeFactor;
        } else {
            return (upperOverlimitFactor * price * (Math.abs(power) - Math.abs(upperLimit)) + price
                    * power) *
                    timeFactor;
        }
    }

    public static double priceNegativePowerLimitFunction(double price, double lowerLimit, double limitPower,
                                                         double power, long timeFactor) {
        if (limitPower < 0 && limitPower < lowerLimit) {
            return price * lowerLimit * timeFactor;
        } else {
            return price * power * timeFactor;
        }
    }

    public static double twoPriceNegativePowerLimitFunction(double firstPrice, double secondPrice, double lowerLimit,
                                                            double limitPower, double firstPower, double secondPower,
                                                            long timeFactor) {
        if (limitPower < 0 && limitPower < lowerLimit) {
            double firstPercent = firstPower / (firstPower + secondPower);
            return (firstPrice * lowerLimit * firstPercent
                    + secondPrice * lowerLimit * (1.0 - firstPercent)) * timeFactor;
        } else {
            return (firstPrice * firstPower + secondPrice * secondPower) * timeFactor;
        }
    }

    public static double negativeQuadPowerFractionFunction(double firstPower, double secondPower, double thirdPower,
                                                      double forthPower, long timeFactor) {
        if (firstPower < 0) {
            //1 = apExternal(-), 2 = autoconPV(-), 3 = autoconCHP(-), 4 = autoConBat(-)

            //total production sum --> ap + sum of autoCon
            // signs reversed as numerator is neg
            double denominator = firstPower + secondPower + thirdPower + forthPower;

            return (1.0 - (firstPower / denominator)) * timeFactor;
        } else {
            return 1.0 * timeFactor;
        }
    }

    public static double positiveQuadPowerFractionFunction(double firstPower, double secondPower, double thirdPower,
                                                           double forthPower, long timeFactor) {
        if (firstPower > 0) {
            //1 = apExternal(+), 2 = autoconPV(-), 3 = autoconCHP(-), 4 = autoConBat(-)

            //total cons --> ap + sum of autoCon
            double denominator = firstPower - secondPower - thirdPower - forthPower;

            return (1.0 - (firstPower / denominator)) * timeFactor;
        } else {
            return 1.0 * timeFactor;
        }
    }
}
