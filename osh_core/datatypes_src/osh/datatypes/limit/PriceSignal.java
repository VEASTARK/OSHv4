package osh.datatypes.limit;

import osh.datatypes.commodity.AncillaryCommodity;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;


/**
 * @author Ingo Mauser
 */
public class PriceSignal {

    private final double UNKNOWN_PRICE = 100;
    private AncillaryCommodity commodity;
    private TreeMap<Long, Double> prices;
    /**
     * Flag whether the redundant entries have been removed
     */
    private boolean isCompressed = true;

    private long priceUnknownBefore;
    private long priceUnknownAtAndAfter;


    /**
     * CONSTRUCTOR
     */
    public PriceSignal(AncillaryCommodity commodity) {
        this.commodity = commodity;
        this.prices = new TreeMap<>();
    }

    /**
     * CONSTRUCTOR
     */
    public PriceSignal(int timeCreatedFor) {
        this.prices = new TreeMap<>();
    }


    public void setPrice(long time, double price) {
        this.prices.put(time, price);
        this.isCompressed = false;
    }


    /**
     * Sets the interval during which the price is known
     */
    public void setKnownPriceInterval(long start, long end) {
        this.priceUnknownBefore = start;
        this.priceUnknownAtAndAfter = end;
    }

    /**
     * Removes redundant entries
     */
    public void compress() {
        if (this.isCompressed) {
            return;
        }

        synchronized (this.prices) {
            Iterator<Entry<Long, Double>> i = this.prices.entrySet().iterator();
            Double last = null;

            while (i.hasNext()) {
                Entry<Long, Double> e = i.next();
                if (e.getValue().equals(last)) {
                    i.remove();
                } else {
                    last = e.getValue();
                }
            }

            this.isCompressed = true;
        }
    }

    /**
     * Returns the current price<br>
     * If there's no price available: return UNKNOWN_PRICE (100 cents)
     *
     * @param t timeStamp (UnixTime)
     */
    public double getPrice(long t) {
        if (t < this.priceUnknownBefore || t > this.priceUnknownAtAndAfter) {
            System.out.println("ERROR: Price unknown, using default price");
            System.out.println("requested time outside of known interval: " + t + " not in [" + this.priceUnknownBefore + " - " + this.priceUnknownAtAndAfter + "]");
            return this.UNKNOWN_PRICE;
        }

        // Return most recent price
        Entry<Long, Double> entry = this.prices.floorEntry(t);

        if (entry != null) {
            return entry.getValue();
        } else {
            System.out.println("ERROR: Price unknown, using default price");
            System.out.println("Price in known interval, but floorEntry null, time: " + t);
            return this.UNKNOWN_PRICE;
        }
    }


    /**
     * Returns the time the price changes after t
     *
     * @param t time after price will change
     * @return null if there is no next price change
     */
    public Long getNextPriceChange(long t) {
        if (t >= this.priceUnknownAtAndAfter) {
            return null;
        }

        this.compress();

        Long key = this.prices.higherKey(t);
        /* && t < priceUnknownAfter */
        return Objects.requireNonNullElseGet(key, () -> this.priceUnknownAtAndAfter);
    }

    public Iterator<Entry<Long, Double>> getIteratorForSubMap(long from, long to) {
        return this.prices.subMap(from, false, to, false).entrySet().iterator();
    }

    public Entry<Long, Double> getFloorEntry(long t) {
        return this.prices.floorEntry(t);
    }

    /**
     * returned value is the first time tick which has no price.
     */
    public long getPriceUnknownAtAndAfter() {
        return this.priceUnknownAtAndAfter;
    }

    public long getPriceUnknownBefore() {
        return this.priceUnknownBefore;
    }

    public AncillaryCommodity getCommodity() {
        return this.commodity;
    }

    public TreeMap<Long, Double> getPrices() {
        return this.prices;
    }


    @SuppressWarnings("unchecked")
    @Override
    public PriceSignal clone() {
        PriceSignal clone = new PriceSignal(this.commodity);

        clone.isCompressed = this.isCompressed;
        clone.priceUnknownBefore = this.priceUnknownBefore;
        clone.priceUnknownAtAndAfter = this.priceUnknownAtAndAfter;

        //clone TreeMap
        //		for (Entry<Long, Double> e : prices.entrySet()) {
        //			double originalValue = e.getValue();
        //			clone.prices.put(e.getKey(), originalValue);
        //		}
        // clone only map, not the keys and values (not necessary)
        clone.prices = (TreeMap<Long, Double>) this.prices.clone();

        return clone;
    }

    private <T> Entry<Long, T> getNext(
            Iterator<Entry<Long, T>> it,
            long duration) {
        if (it.hasNext()) {
            Entry<Long, T> e = it.next();
            if (e.getKey() < duration)
                return e;
            else
                return null;
        } else
            return null;
    }

    public void extendAndOverride(PriceSignal toExtend) {
        if (this.commodity != toExtend.commodity)
            throw new IllegalArgumentException("Mismatched commodity");

        Iterator<Entry<Long, Double>> iSet1 = this.prices.entrySet()
                .iterator();
        Iterator<Entry<Long, Double>> iSet2 = toExtend.prices.entrySet()
                .iterator();

        Entry<Long, Double> entry1;
        Entry<Long, Double> entry2;
        TreeMap<Long, Double> newPrices = new TreeMap<>();
        long oldUnknownAfter = this.priceUnknownAtAndAfter;
        long oldUnknownBefore = this.priceUnknownBefore;

        this.priceUnknownBefore = Math.min(this.priceUnknownBefore, toExtend.priceUnknownBefore);
        this.priceUnknownAtAndAfter = Math.max(this.priceUnknownAtAndAfter, toExtend.priceUnknownAtAndAfter);
        this.isCompressed = false;

        entry1 = this.getNext(iSet1, oldUnknownAfter);
        entry2 = this.getNext(iSet2, toExtend.priceUnknownAtAndAfter);

        while (entry1 != null && entry2 != null) {

            if (entry1.getKey() < toExtend.priceUnknownBefore) {
                newPrices.put(entry1.getKey(), entry1.getValue());
                entry1 = this.getNext(iSet1, oldUnknownAfter);
            } else {
                newPrices.put(entry2.getKey(), entry2.getValue());
                entry2 = this.getNext(iSet2, toExtend.priceUnknownAtAndAfter);
            }
        }

        while (entry1 != null) { // 1st profile still has data points
            if (entry1.getKey() > toExtend.priceUnknownAtAndAfter) {
                newPrices.put(entry1.getKey(), entry1.getValue());
            }
            entry1 = this.getNext(iSet1, oldUnknownAfter);
        }

        while (entry2 != null) { // 2nd profile still has data points
            if (entry2.getKey() > toExtend.priceUnknownAtAndAfter) {
                newPrices.put(entry2.getKey(), entry2.getValue());
            }
            entry2 = this.getNext(iSet2, toExtend.priceUnknownAtAndAfter);
        }

        //price signals dont overlap (|----2----|     |----1----|), so we have an uncertain period
        if (toExtend.priceUnknownAtAndAfter < oldUnknownBefore) {
            newPrices.put(toExtend.priceUnknownAtAndAfter, this.UNKNOWN_PRICE);
        }

        //price signals dont overlap (|----1----|     |----2----|), so we have an uncertain period
        if (oldUnknownAfter < toExtend.priceUnknownBefore) {
            newPrices.put(oldUnknownAfter, this.UNKNOWN_PRICE);
        }

        this.prices = newPrices;

        this.compress();
    }

    /**
     * clones this price signal after the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this price signal after the timestamp
     */
    public PriceSignal cloneAfter(long timestamp) {

        PriceSignal priceSignal = new PriceSignal(this.commodity);

        double startCorrection = this.getPrice(timestamp);

        if (startCorrection != this.UNKNOWN_PRICE)
            priceSignal.prices.put(timestamp, startCorrection);

        for (Entry<Long, Double> en : this.prices.tailMap(timestamp).entrySet()) {
            priceSignal.prices.put(en.getKey(), en.getValue());
        }
        priceSignal.priceUnknownAtAndAfter = this.priceUnknownAtAndAfter;
        priceSignal.priceUnknownBefore = timestamp;

        return priceSignal;
    }

    /**
     * clones this price signal before the given time and returns the result
     *
     * @param timestamp
     * @return a clone of this price signal before the timestamp
     */
    public PriceSignal cloneBefore(long timestamp) {

        PriceSignal priceSignal = new PriceSignal(this.commodity);

        for (Entry<Long, Double> en : this.prices.headMap(timestamp).entrySet()) {
            priceSignal.prices.put(en.getKey(), en.getValue());
        }

        priceSignal.priceUnknownAtAndAfter = Math.min(this.priceUnknownAtAndAfter, timestamp);
        priceSignal.priceUnknownBefore = this.priceUnknownBefore;

        return priceSignal;
    }

    @Override
    public String toString() {
        return this.commodity + "=" + this.prices.toString();
    }
}
