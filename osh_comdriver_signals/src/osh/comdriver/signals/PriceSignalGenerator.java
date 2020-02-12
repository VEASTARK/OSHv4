package osh.comdriver.signals;

import osh.datatypes.limit.PriceSignal;
import osh.utils.time.TimeConversion;

import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class PriceSignalGenerator {

    public static PriceSignal getConstantPriceSignal(
            long startTime,
            long endTime,
            long constantPeriod,
            double price) {

        PriceSignal priceSignal = new PriceSignal();

        for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++) {

            long timestamp = startTime + i * constantPeriod;

            priceSignal.setPrice(timestamp, price);
        }
        priceSignal.setKnownPriceInterval(startTime, endTime);

        priceSignal.compress();

        return priceSignal;
    }

    public static PriceSignal getPriceSignalOfTreeMap(
            long startTime,
            long endTime,
            long constantPeriod,
            TreeMap<Long, Double> prices) {

        Double lastValue = null;
        PriceSignal priceSignal = new PriceSignal();

        for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++) {

            long timestamp = startTime + i * constantPeriod;
            long timeFromMidnight = TimeConversion.convertUnixTime2SecondsSinceMidnight(timestamp);
            Entry<Long, Double> en = prices.floorEntry(timeFromMidnight);

            if (lastValue != null) {
                if (!en.getValue().equals(lastValue)) {
                    priceSignal.setPrice(timestamp - (timeFromMidnight - en.getKey()), en.getValue());
                    lastValue = en.getValue();
                }
            } else {
                priceSignal.setPrice(timestamp, en.getValue());
                lastValue = en.getValue();
            }
        }

        priceSignal.setKnownPriceInterval(startTime, endTime);

        priceSignal.compress();

        return priceSignal;
    }

    public static PriceSignal getPriceSignalWeekday(
            long startTime,
            long endTime,
            long constantPeriod,
            Double[] pricesPerWeekDay) {

        PriceSignal priceSignal = new PriceSignal();

        long time = startTime;
        priceSignal.setPrice(time, pricesPerWeekDay[TimeConversion.convertUnixTime2CorrectedDayOfWeek(time)]);
        time -= TimeConversion.convertUnixTime2SecondsSinceMidnight(time);

        while (true) { //Yeah, Yeah I know ...
            time = TimeConversion.convertUnixTimeToZonedDateTime(time).plusDays(1).toEpochSecond();
            if (time > endTime)
                break;

            priceSignal.setPrice(time, pricesPerWeekDay[TimeConversion.convertUnixTime2CorrectedDayOfWeek(time)]);
        }

        priceSignal.setKnownPriceInterval(startTime, endTime);

        priceSignal.compress();

        return priceSignal;
    }

    public static PriceSignal getFlexiblePriceSignal(
            long secondsFromYearStart,
            long startTime,
            long endTime,
            long constantPeriod,
            List<Double> priceSignalYear) {

        PriceSignal priceSignal = new PriceSignal();
        double price;

        int startIndex = (int) (secondsFromYearStart / constantPeriod);
        long steps = ((endTime - startTime) / constantPeriod);

        for (int i = 0; i < steps; i++) {

            long timestamp = startTime + i * constantPeriod;

            int actualIndex = i + startIndex;
            if (actualIndex > priceSignalYear.size() - 1)
                actualIndex %= priceSignalYear.size();

            price = priceSignalYear.get(actualIndex);
            priceSignal.setPrice(timestamp, price);
        }
        priceSignal.setKnownPriceInterval(startTime, endTime);

        priceSignal.compress();

        return priceSignal;
    }


    public static PriceSignal getStepPriceSignal(
            long startTime,
            long endTime,
            long constantPeriod,
            int periodsToSwitch,
            double priceMin,
            double priceMax,
            boolean startWithMinPrice) {

        boolean lowPricePeriod = startWithMinPrice;

        PriceSignal priceSignal = new PriceSignal();

        int counter = 0;

        for (long i = 0; i < ((endTime - startTime) / constantPeriod); i++) {

            long timestamp = startTime + i * constantPeriod;

            if (lowPricePeriod) {
                priceSignal.setPrice(timestamp, priceMin);
            } else {
                priceSignal.setPrice(timestamp, priceMax);
            }

            counter++;

            if (counter >= periodsToSwitch) {
                counter = 0;
                lowPricePeriod = !lowPricePeriod;
            }
        }

        priceSignal.setKnownPriceInterval(startTime, endTime);
        priceSignal.compress();

        return priceSignal;
    }

}
