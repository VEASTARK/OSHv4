package osh.datatypes.limit;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.longs.Long2DoubleMaps;
import it.unimi.dsi.fastutil.longs.Long2DoubleSortedMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import osh.utils.dataStructures.fastutil.Long2DoubleTreeMap;


/**
 * Represents a price-signal, mapping times inside a known interval to prices.
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class PriceSignal {

    /**
     * default price when none is known
     */
    private final double UNKNOWN_PRICE = 100;
    private Long2DoubleTreeMap prices;
    /**
     * Flag whether the redundant entries have been removed
     */
    private boolean isCompressed = true;

    private long priceUnknownBefore;
    private long priceUnknownAtAndAfter;


    /**
     * Constructs an empty price signal.
     */
    public PriceSignal() {
        this.prices = new Long2DoubleTreeMap();
    }

    /**
     * Constructs a price signal based on the given mappings.
     *
     * @param prices the mapping
     */
    public PriceSignal(Long2DoubleSortedMap prices) {
        this.prices = new Long2DoubleTreeMap(prices);
        this.priceUnknownBefore = prices.firstLongKey();
        this.priceUnknownAtAndAfter = prices.lastLongKey();
        this.isCompressed = false;
    }

    /**
     * Constructs a price signal based on the given mappings and applies the given offset to the time interval.
     *
     * @param prices the mapping
     * @param offset the offset
     */
    public PriceSignal(Long2DoubleSortedMap prices, long offset) {
        this.prices = new Long2DoubleTreeMap();
        Long2DoubleMaps.fastForEach(prices, e -> this.prices.put(e.getLongKey() + offset, e.getDoubleValue()));
        this.priceUnknownBefore = prices.firstLongKey() + offset;
        this.priceUnknownAtAndAfter = prices.lastLongKey() + offset;
        this.isCompressed = false;
    }

    /**
     * Sets the known price to the given value at the given time
     *
     * @param time the time for the new mapping
     * @param price the price for the new mapping
     */
    public void setPrice(long time, double price) {
        this.prices.put(time, price);
        this.isCompressed = false;
    }

    /**
     * Sets the time interval during which the price is known.
     *
     * @param start the (inclusive) start of the interval
     * @param end the (inclusive) end of the interval
     */
    public void setKnownPriceInterval(long start, long end) {
        this.priceUnknownBefore = start;
        this.priceUnknownAtAndAfter = end;
    }

    /**
     * Removes redundant entries.
     */
    public void compress() {
        if (this.isCompressed) {
            return;
        }

        ObjectIterator<Long2DoubleMap.Entry> i = Long2DoubleMaps.fastIterator(this.prices);
        double last = Double.NaN;

        while (i.hasNext()) {
            Long2DoubleMap.Entry e = i.next();
            if (e.getDoubleValue() == last) {
                i.remove();
            } else {
                last = e.getDoubleValue();
            }
        }

        this.isCompressed = true;
    }

    /**
     * Returns the current price at the given time t.<br>
     * If there's no price available: return UNKNOWN_PRICE (100 cents)
     *
     * @param t the time
     *
     * @return the price at time t
     */
    public double getPrice(long t) {
        if (t < this.priceUnknownBefore || t > this.priceUnknownAtAndAfter) {
            System.out.println("ERROR: Price unknown, using default price");
            System.out.println("requested time outside of known interval: " + t + " not in [" + this.priceUnknownBefore + " - " + this.priceUnknownAtAndAfter + "]");
            return this.UNKNOWN_PRICE;
        }

        // Return most recent price
        Long2DoubleMap.Entry entry = this.prices.floorEntry(t);

        if (entry != null) {
            return entry.getDoubleValue();
        } else {
            System.out.println("ERROR: Price unknown, using default price");
            System.out.println("Price in known interval, but floorEntry null, time: " + t);
            return this.UNKNOWN_PRICE;
        }
    }


    /**
     * Returns the next time the price changes after the given time t.
     *
     * @param t the time
     *
     * @return the next time the price changes after t or null if there is no next price change
     */
    public Long getNextPriceChange(long t) {
        if (t >= this.priceUnknownAtAndAfter) {
            return null;
        }

        this.compress();

        long key = this.prices.higherKey(t);
        return key == Long2DoubleTreeMap.INVALID_KEY ? this.priceUnknownAtAndAfter : key;
    }

    /**
     * Returns an iterator for the price mappings in the given time interval.
     *
     * @param from the (exclusive) start of the interval
     * @param to the (inclusive) end of the interval
     *
     * @return an iterator for the price mappings in the time interval
     */
    public ObjectIterator<Long2DoubleMap.Entry> getIteratorForSubMap(long from, long to) {
        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        return Long2DoubleMaps.fastIterator(this.prices.subMap(from + 1, to));
    }

    /**
     * Returns the mapping associated with the greatest time less than or equal to the given time, or null if there
     * is no such mapping.
     *
     * @param t the time
     *
     * @return the mapping associated with the greatest time less than or equal to the given time, or null if there is
     * no such mapping
     */
    public Long2DoubleMap.Entry getFloorEntry(long t) {
        return this.prices.floorEntry(t);
    }

    /**
     * Returns the time-point after which no price is known.
     *
     * @return the point in time after which no price is known
     */
    public long getPriceUnknownAtAndAfter() {
        return this.priceUnknownAtAndAfter;
    }

    /**
     * Returns the time-point before which no price is known.
     *
     * @return the point in time before which no price is known
     */
    public long getPriceUnknownBefore() {
        return this.priceUnknownBefore;
    }

    /**
     * Returns all mappings of time to prices.
     *
     * @return all mappings of time to prices
     */
    public Long2DoubleTreeMap getPrices() {
        return this.prices;
    }


    @Override
    public PriceSignal clone() {
        PriceSignal clone = new PriceSignal();

        clone.isCompressed = this.isCompressed;
        clone.priceUnknownBefore = this.priceUnknownBefore;
        clone.priceUnknownAtAndAfter = this.priceUnknownAtAndAfter;

        clone.prices = new Long2DoubleTreeMap(this.prices);

        return clone;
    }

    private Long2DoubleMap.Entry getNext(
            ObjectIterator<Long2DoubleMap.Entry> it,
            long duration) {
        if (it.hasNext()) {
            Long2DoubleMap.Entry e = it.next();
            if (e.getLongKey() < duration)
                return e;
            else
                return null;
        } else
            return null;
    }

    private void extendAndOverride(ObjectIterator<Long2DoubleMap.Entry> iSet2, long toExtendUnknownBefore,
                                  long toExtendUnknownAtAndAfter) {
        ObjectIterator<Long2DoubleMap.Entry> iSet1 = Long2DoubleMaps.fastIterator(this.prices);

        Long2DoubleMap.Entry entry1 = this.getNext(iSet1, this.priceUnknownAtAndAfter);
        Long2DoubleMap.Entry entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
        Long2DoubleTreeMap newPrices = new Long2DoubleTreeMap();
        long oldUnknownAfter = this.priceUnknownAtAndAfter;
        long oldUnknownBefore = this.priceUnknownBefore;

        this.priceUnknownBefore = Math.min(this.priceUnknownBefore, toExtendUnknownBefore);
        this.priceUnknownAtAndAfter = Math.max(this.priceUnknownAtAndAfter, toExtendUnknownAtAndAfter);
        this.isCompressed = false;

        while (entry1 != null && entry2 != null) {
            if (entry1.getLongKey() < toExtendUnknownBefore) {
                newPrices.put(entry1.getLongKey(), entry1.getDoubleValue());
                entry1 = this.getNext(iSet1, oldUnknownAfter);
            } else {
                newPrices.put(entry2.getLongKey(), entry2.getDoubleValue());
                entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
            }
        }

        while (entry1 != null) { // 1st profile still has data points
            if (entry1.getLongKey() > toExtendUnknownAtAndAfter) {
                newPrices.put(entry1.getLongKey(), entry1.getDoubleValue());
            }
            entry1 = this.getNext(iSet1, oldUnknownAfter);
        }

        while (entry2 != null) { // 2nd profile still has data points
            if (entry2.getLongKey() > toExtendUnknownAtAndAfter) {
                newPrices.put(entry2.getLongKey(), entry2.getDoubleValue());
            }
            entry2 = this.getNext(iSet2, toExtendUnknownAtAndAfter);
        }

        //price signals dont overlap (|----2----|     |----1----|), so we have an uncertain period
        if (toExtendUnknownAtAndAfter < oldUnknownBefore) {
            newPrices.put(toExtendUnknownAtAndAfter, this.UNKNOWN_PRICE);
        }

        //price signals dont overlap (|----1----|     |----2----|), so we have an uncertain period
        if (oldUnknownAfter < toExtendUnknownBefore) {
            newPrices.put(oldUnknownAfter, this.UNKNOWN_PRICE);
        }

        this.prices = newPrices;

        this.compress();
    }

    /**
     * Overrides (and extends) the mappings of this price signal with all the mappings of the given price signal.
     *
     * @param toExtend the new and extended prices
     */
    public void extendAndOverride(PriceSignal toExtend) {
        this.extendAndOverride(Long2DoubleMaps.fastIterator(toExtend.prices),
                toExtend.priceUnknownBefore, toExtend.priceUnknownAtAndAfter);
    }

    /**
     * Clones this price signal after the given time and returns the result.
     *
     * @param timestamp the time
     *
     * @return a clone of this price signal after the timestamp
     */
    public PriceSignal cloneAfter(long timestamp) {
        PriceSignal priceSignal = new PriceSignal();

        double startCorrection = this.getPrice(timestamp);

        if (startCorrection != this.UNKNOWN_PRICE)
            priceSignal.prices.put(timestamp, startCorrection);

        //fastutil maps handles the from-value as inclusive but we need it to be exclusive so we add one
        priceSignal.prices.putAll(this.prices.tailMap(timestamp + 1));
        priceSignal.priceUnknownAtAndAfter = this.priceUnknownAtAndAfter;
        priceSignal.priceUnknownBefore = timestamp;

        return priceSignal;
    }

    /**
     * Clones this price signal before the given time and returns the result.
     *
     * @param timestamp the time
     *
     * @return a clone of this price signal before the timestamp
     */
    public PriceSignal cloneBefore(long timestamp) {
        PriceSignal priceSignal = new PriceSignal();

        priceSignal.prices.putAll(this.prices.headMap(timestamp));

        priceSignal.priceUnknownAtAndAfter = Math.min(this.priceUnknownAtAndAfter, timestamp);
        priceSignal.priceUnknownBefore = this.priceUnknownBefore;

        return priceSignal;
    }

    @Override
    public String toString() {
        return this.prices.toString();
    }
}
