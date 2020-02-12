package osh.datatypes.power;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import osh.datatypes.commodity.Commodity;

import java.util.EnumMap;

/**
 * Represents a special iterator that moves through a sparse load-profile from a specified starting point to a
 * specified end.
 *
 * @author Sebastian Kramer
 */
public class SequentialSparseLoadProfileIterator {

    private final EnumMap<Commodity, Long2IntMap.Entry> currentEntry;
    private final EnumMap<Commodity, Long2IntMap.Entry> nextEntry;
    private final EnumMap<Commodity, ObjectIterator<Long2IntMap.Entry>> iterators;
    private final long endingTimeOfProfile;

    /**
     * Generates this iterator with the given set of current entries, next entries and iterators.
     *
     * @param currentEntry the set of current entries at the starting point
     * @param nextEntry the set of next entries at the starting point
     * @param iterators the set of iterators
     * @param endingTimeOfProfile the point after which loads are not known anymore
     */
    public SequentialSparseLoadProfileIterator(
            EnumMap<Commodity, Long2IntMap.Entry> currentEntry,
            EnumMap<Commodity, Long2IntMap.Entry> nextEntry,
            EnumMap<Commodity, ObjectIterator<Long2IntMap.Entry>> iterators,
            long endingTimeOfProfile) {
        this.currentEntry = currentEntry;
        this.nextEntry = nextEntry;
        this.iterators = iterators;
        this.endingTimeOfProfile = endingTimeOfProfile;
    }

    /**
     * Returns the rounded average load from the given point to the given point for the given commodity
     *
     * @param commodity the commodity for which the load should be calculated
     * @param start the starting point
     * @param end the ending point
     * @return the rounded average load from the starting point to the ending point for the commodity
     */
    public int getAverageLoadFromTillSequential(Commodity commodity, long start, long end) {
        return (int) Math.round(this.getAverageLoadFromTillSequentialNotRounded(commodity, start, end));
    }

    /**
     * Returns the average load from the given point to the given point for the given commodity
     *
     * @param commodity the commodity for which the load should be calculated
     * @param start the starting point
     * @param end the ending point
     * @return the average load from the starting point to the ending point for the commodity
     */
    public double getAverageLoadFromTillSequentialNotRounded(Commodity commodity, long start, long end) {

        if (start >= this.endingTimeOfProfile) {
            return 0;
        }
        Long2IntMap.Entry current = this.currentEntry.get(commodity);

        //checking if profile has values
        if (current == null)
            return 0;

        double avg = 0.0;
        long currentTime = start;
        double span = end - start;
        long maxTime = Math.min(end, this.endingTimeOfProfile);

        Long2IntMap.Entry next = this.nextEntry.get(commodity);

        //no other values for the requested time period
        if (next == null || next.getLongKey() >= maxTime) {
            return current.getIntValue();
        }

        ObjectIterator<Long2IntMap.Entry> entryIterator = this.iterators.get(commodity);

        while (next != null && next.getLongKey() < maxTime) {
            long nextChange = next.getLongKey();

            avg += current.getIntValue() * (nextChange - currentTime);
            currentTime = nextChange;
            current = next;

            if (entryIterator.hasNext()) {
                next = entryIterator.next();
            } else {
                next = null;
            }
        }

        if (currentTime < maxTime) {
            avg += current.getIntValue() * (maxTime - currentTime);
        }

        this.currentEntry.put(commodity, current);
        this.nextEntry.put(commodity, next);

        return (avg / span);
    }
}
