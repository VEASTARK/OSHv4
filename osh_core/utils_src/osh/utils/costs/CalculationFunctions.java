package osh.utils.costs;

/**
 * @author Sebastian Kramer
 */
public class CalculationFunctions {

    private static double upperOverlimitFactor;
    private static double lowerOverlimitFactor;

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
}
