package osh.utils.dataStructures;

import it.unimi.dsi.fastutil.longs.Long2DoubleMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.Arrays;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Implements are bare-bones and stripped down version of a navigable map with only a handful of important functions
 * implemented.
 *
 * Backed by a very simple array storage for keys and values and immutabler after creation this map is intedned for
 * uses where you want to have the conveniance methods of navigable maps (floor/ceil-methods, iterators and submaps).
 *
 *
 * @author Sebastian Kramer
 */
public class Long2DoubleErsatzNavigableMap {

    private final long[] key;
    private final double[] value;

    /**
     * Constructs this simplified map with the given key-value mapping.
     *
     * <p>
     * It is assumed that the last entry of the key array is {@link Long#MAX_VALUE} to assure function of this map.
     *
     * @param key the keys of the map
     * @param value the values of the map
     */
    public Long2DoubleErsatzNavigableMap(long[] key, double[] value) {
        this.key = key;
        this.value = value;
        if (key.length != value.length)
            throw new IllegalArgumentException(
                "Keys and values have different lengths (" + key.length + ", " + value.length + ")");
    }

    /**
     * Constructs this simplified map with the given key-value mapping, stripped to the given length.
     *
     * @param key the keys of the map
     * @param value the values of the map
     * @param length the new length
     */
    public Long2DoubleErsatzNavigableMap(long[] key, double[] value, int length) {
        this.key = new long[length + 1];
        this.value = new double[length + 1];
        System.arraycopy(key, 0, this.key, 0, length);
        System.arraycopy(value, 0, this.value, 0, length);
        this.key[length] = Long.MAX_VALUE;
        this.value[length] = Double.NaN;
    }

    /**
     * Constructs this simplified map upon a given sorted map.
     *
     * @param sortedMap the sorted map
     */
    public Long2DoubleErsatzNavigableMap(SortedMap<Long, Double> sortedMap) {
        this.key = new long[sortedMap.size() + 1];
        this.value = new double[sortedMap.size() + 1];

        int i = 0;
        for (Map.Entry<Long, Double> en : sortedMap.entrySet()) {
            this.key[i] = en.getKey();
            this.value[i] = en.getValue();
            i++;
        }
        this.key[i] = Long.MAX_VALUE;
        this.value[i] = Double.NaN;
    }

    /**
     * Constructs a clone of the given {@link Long2DoubleErsatzNavigableMap}.
     *
     * @param other the map to clone
     */
    public Long2DoubleErsatzNavigableMap(Long2DoubleErsatzNavigableMap other) {
        this.key = new long[other.key.length];
        this.value = new double[other.value.length];
        System.arraycopy(other.key, 0, this.key, 0, other.key.length);
        System.arraycopy(other.value, 0, this.value, 0, other.value.length);
    }

    private static Long2DoubleErsatzNavigableMap internalCloneAfter(Long2DoubleErsatzNavigableMap toClone,
                                                                   int fromIndex) {
        assert(fromIndex > -1 && fromIndex < toClone.key.length);

        int cloneLength = toClone.key.length - fromIndex;
        long[] clonedKey = new long[cloneLength];
        double[] clonedValue = new double[cloneLength];
        System.arraycopy(toClone.key, fromIndex, clonedKey, 0, cloneLength);
        System.arraycopy(toClone.value, fromIndex, clonedValue, 0, cloneLength);

        return new Long2DoubleErsatzNavigableMap(clonedKey, clonedValue);
    }

    private static Long2DoubleErsatzNavigableMap internalCloneBefore(Long2DoubleErsatzNavigableMap toClone,
                                                                   int tillIndex) {
        assert(tillIndex > -1 && tillIndex < toClone.key.length);

        long[] clonedKey = new long[tillIndex + 2];
        double[] clonedValue = new double[tillIndex + 2];
        System.arraycopy(toClone.key, 0, clonedKey, 0, tillIndex + 1);
        System.arraycopy(toClone.value, 0, clonedValue, 0, tillIndex + 1);
        clonedKey[tillIndex + 1] = Long.MAX_VALUE;
        clonedValue[tillIndex + 1] = Double.NaN;

        return new Long2DoubleErsatzNavigableMap(clonedKey, clonedValue);
    }

    private static Long2DoubleErsatzNavigableMap internalCloneBetween(Long2DoubleErsatzNavigableMap toClone,
                                                                     int fromIndex, int tillIndex) {

        int cloneLength = tillIndex - fromIndex;
        long[] clonedKey = new long[cloneLength + 2];
        double[] clonedValue = new double[cloneLength + 2];
        System.arraycopy(toClone.key, fromIndex, clonedKey, 0, cloneLength + 1);
        System.arraycopy(toClone.value, fromIndex, clonedValue, 0, cloneLength + 1);
        clonedKey[cloneLength + 1] = Long.MAX_VALUE;
        clonedValue[cloneLength + 1] = Double.NaN;

        return new Long2DoubleErsatzNavigableMap(clonedKey, clonedValue);
    }

    /**
     * Clones the given map after the given starting point(inclusive).
     *
     * @param toClone the map to clone
     * @param from the starting point from which to clone the new map(inclusive)
     *
     * @return a clone of the map after the set starting point
     */
    public static Long2DoubleErsatzNavigableMap cloneAfter(Long2DoubleErsatzNavigableMap toClone, long from) {
        return internalCloneAfter(toClone, toClone.getFloorIndex(from));
    }

    /**
     * Clones the given map before the given time(inclusive).
     *
     * @param toClone the map to clone
     * @param till the end point till which to clone the new map(inclusive)
     *
     * @return a clone of the map before the set end point
     */
    public static Long2DoubleErsatzNavigableMap cloneBefore(Long2DoubleErsatzNavigableMap toClone, long till) {
        return internalCloneBefore(toClone, toClone.getFloorIndex(till) + 1);
    }

    /**
     * Clones the given map between the given starting(inclsuive) and end(inclusive) points
     *
     * @param toClone the map to clone
     * @param from the starting point from which to clone the new map(inclusive)
     * @param till the end point till which to clone the new map(inclusive)
     *
     * @return a clone of the map between the starting and end point
     */
    public static Long2DoubleErsatzNavigableMap cloneBetween(Long2DoubleErsatzNavigableMap toClone, long from, long till) {
        return internalCloneBetween(toClone, toClone.getFloorIndex(from), toClone.getFloorIndex(till) + 1);
    }

    /**
     * Returns the highest key k for which k <= t holds true, or {@link Long#MIN_VALUE} if no such key exists.
     *
     * @param t the key value to search for
     *
     * @return the highest key k for which k <= t, or {@link Long#MIN_VALUE} if no such key exists
     */
    public long getFloorKey(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1) - 1;
        if (index < 0 || index >= this.key.length) {
            return Long.MIN_VALUE;
        }
        return this.key[index];
    }

    /**
     * Returns the smallest key k for which k >= t holds true, or {@link Long#MAX_VALUE} if no such key exists.
     *
     * @param t the key value to search for
     *
     * @return the smallest key k for which k >= t, or {@link Long#MAX_VALUE} if no such key exists
     */
    public long getCeilKey(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1);
        if (index >= this.key.length) {
            return Long.MIN_VALUE;
        }
        return this.key[index];
    }

    /**
     * Returns the highest value for whose corresponding key k k <= t holds true, or {@link Double#NaN} if no
     * such value exists.
     *
     * @param t the key value to search for
     *
     * @return the highest value for whose corresponding key k k <= t holds true, or {@link Double#NaN} if no
     *         such value exists.
     */
    public double getFloorValue(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1) - 1;
        if (index < 0 || index >= this.key.length) {
            return Double.NaN;
        }
        return this.value[index];
    }

    /**
     * Returns the smallest value for whose corresponding key k k >= t holds true, or {@link Double#NaN} if no
     * such value exists.
     *
     * @param t the key value to search for
     *
     * @return the smallest value for whose corresponding key k k >= t holds true, or {@link Double#NaN} if no
     *         such value exists.
     */
    public double getCeilValue(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1);
        if (index >= this.key.length) {
            return Double.NaN;
        }
        return this.value[index];
    }

    /**
     * Returns the highest entry for whose key k k <= t holds true, or null if no such value exists.
     *
     * @param t the key value to search for
     *
     * @return the highest entry for whose corresponding key k k <= t holds true, or null if no such value exists.
     */
    public Map.Entry<Long, Double> getFloorEntry(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1);
        if (index >= this.key.length) {
            return null;
        }
        return new BasicEntry(this.key[index], this.value[index]);
    }

    /**
     * Returns the index of the highest key k for which k <= t holds true, or -1 if no such key exists.
     *
     * @param t the key value to search for
     *
     * @return the index of the highest key k for which k <= t, or -1 if no such key exists
     */
    public int getFloorIndex(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1) - 1;
        if (index < 0 || index >= this.key.length) {
            return -1;
        }
        return index;
    }

    /**
     * Returns the keys of this map.
     *
     * @return the keys of this map
     */
    public long[] getKey() {
        return this.key;
    }

    /**
     * Returns the values of this map.
     *
     * @return the values of this map
     */
    public double[] getValue() {
        return this.value;
    }

    /**
     * Returns if this map is empty.
     *
     * @return true if this map has noe entries
     */
    public boolean isEmpty() {
        return this.key.length == 0;
    }

    /**
     * Returns an {@link java.util.Iterator} over a submap of this map, going from the given starting point to the
     * given end point with inclusivity determined by the given values.
     *
     * @param from the starting point of the submap
     * @param fromInclusive flag if the starting point should be handled as inclusive or not
     * @param to the end point of the submap
     * @param toInclusive flag if the end point should be handled as inclusive or not
     *
     * @return an {@link java.util.Iterator} over a submap of this map, going from the starting point to the   end point
     */
    public Long2DoubleMapIterator subMapIterator(long from, boolean fromInclusive, long to,
                                                            boolean toInclusive) {

        int startingIndex = Arrays.binarySearch(this.key, from);
        int endIndex = Arrays.binarySearch(this.key, to);

        if (startingIndex < 0) startingIndex = ((startingIndex + 1) * -1) - 1;
        if (startingIndex < 0) {
            startingIndex = 0;
        }
        if (startingIndex >= this.key.length) {
            return new Long2DoubleMapIterator();
        }
        if (endIndex < 0) endIndex = ((endIndex + 1) * -1) - 1;
        endIndex--;
        if (endIndex < 0) {
            return new Long2DoubleMapIterator();
        }
        if (endIndex >= this.key.length) {
            endIndex = this.key.length - 1;
        }

        if (this.key[startingIndex] == from && !fromInclusive) {
            startingIndex++;
            if (startingIndex >= this.key.length) {
                return new Long2DoubleMapIterator();
            }
        }

        if (this.key[endIndex] == to && !toInclusive) {
            endIndex--;
            if (endIndex < 0) {
                return new Long2DoubleMapIterator();
            }
        }

        return new Long2DoubleMapIterator(startingIndex, endIndex);
    }

    /**
     * Returns an iteator over all entries of this map.
     *
     * @return an iteator over all entries of this map
     */
    public ObjectIterator<Long2DoubleMap.Entry> entryIterator() {
        return new Long2DoubleMapIterator(0, this.key.length - 1);
    }

    private class Long2DoubleMapIterator implements ObjectIterator<Long2DoubleMap.Entry> {
        int next;
        int curr = -1;
        int max = Long2DoubleErsatzNavigableMap.this.key.length;
        final BasicEntry entry = new BasicEntry();

        public Long2DoubleMapIterator() {
        }

        public Long2DoubleMapIterator(int start, int end) {
            this.next = start;
            this.max = end;
        }

        public boolean hasNext() {
            return this.next <= this.max;
        }

        public Long2DoubleErsatzNavigableMap.BasicEntry next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                this.entry.key = Long2DoubleErsatzNavigableMap.this.key[this.curr = this.next];
                this.entry.value = Long2DoubleErsatzNavigableMap.this.value[this.next++];
                return this.entry;
            }
        }

        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return IntStream.range(0, this.key.length).mapToObj(i -> this.key[i] + "=" + this.value[i])
                .collect(Collectors.joining(", "));
    }

    private static class BasicEntry implements Long2DoubleMap.Entry {
        protected long key;
        protected double value;
        public BasicEntry() {
        }
        public BasicEntry(final Long key, final Double value) {
            this.key = key;
            this.value = value;
        }
        public BasicEntry(final long key, final double value) {
            this.key = key;
            this.value = value;
        }
        @Override
        public long getLongKey() {
            return this.key;
        }
        @Override
        public double getDoubleValue() {
            return this.value;
        }
        @Override
        public double setValue(final double value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            if (o instanceof Long2DoubleMap.Entry) {
                final Long2DoubleMap.Entry e = (Long2DoubleMap.Entry) o;
                return ((this.key) == (e.getLongKey()))
                        && (Double.doubleToLongBits(this.value) == Double.doubleToLongBits(e.getDoubleValue()));
            }
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object key = e.getKey();
            if (!(key instanceof Long))
                return false;
            final Object value = e.getValue();
            if (!(value instanceof Double))
                return false;
            return ((this.key) == ((Long) (key))) && (Double.doubleToLongBits(this.value) == Double
                    .doubleToLongBits((Double) (value)));
        }
        @Override
        public int hashCode() {
            return it.unimi.dsi.fastutil.HashCommon.long2int(this.key) ^ it.unimi.dsi.fastutil.HashCommon.double2int(this.value);
        }
        @Override
        public String toString() {
            return this.key + "->" + this.value;
        }
    }
}
