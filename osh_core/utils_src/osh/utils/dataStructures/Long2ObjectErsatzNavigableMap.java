package osh.utils.dataStructures;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.*;
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
public class Long2ObjectErsatzNavigableMap<V> {

    private final long[] key;
    private final V[] value;

    /**
     * Constructs this simplified map with the given key-value mapping.
     *
     * <p>
     * It is assumed that the last entry of the key array is {@link Long#MAX_VALUE} to assure function of this map.
     *
     * @param key the keys of the map
     * @param value the values of the map
     */
    public Long2ObjectErsatzNavigableMap(long[] key, V[] value) {
        this.key = key;
        this.value = value;
        if (key.length != value.length)
            throw new IllegalArgumentException(
                "Keys and values have different lengths (" + key.length + ", " + value.length + ")");
    }

    /**
     * Constructs this simplified map upon a given sorted map.
     *
     * @param sortedMap the sorted map
     */
    @SuppressWarnings("unchecked")
    public Long2ObjectErsatzNavigableMap(SortedMap<Long, V> sortedMap) {
        this.key = new long[sortedMap.size() + 1];
        this.value = (V[]) new Object[sortedMap.size() + 1];

        int i = 0;
        for (Map.Entry<Long, V> en : sortedMap.entrySet()) {
            this.key[i] = en.getKey();
            this.value[i] = en.getValue();
            i++;
        }
        this.key[i] = Long.MAX_VALUE;
        this.value[i] = null;
    }

    /**
     * Constructs a clone of the given {@link Long2ObjectErsatzNavigableMap}.
     *
     * @param other the map to clone
     */
    @SuppressWarnings("unchecked")
    public Long2ObjectErsatzNavigableMap(Long2ObjectErsatzNavigableMap<V> other) {
        this.key = new long[other.key.length];
        this.value = (V[]) new Object[other.value.length];
        System.arraycopy(other.key, 0, this.key, 0, other.key.length);
        System.arraycopy(other.value, 0, this.value, 0, other.value.length);
    }

    @SuppressWarnings("unchecked")
    private static <V> Long2ObjectErsatzNavigableMap<V> internalCloneAfter(Long2ObjectErsatzNavigableMap<V> toClone,
                                                                           int fromIndex) {
        assert(fromIndex > -1 && fromIndex < toClone.key.length);

        int cloneLength = toClone.key.length - fromIndex;
        long[] clonedKey = new long[cloneLength];
        V[] clonedValue = (V[]) new Object[cloneLength];
        System.arraycopy(toClone.key, fromIndex, clonedKey, 0, cloneLength);
        System.arraycopy(toClone.value, fromIndex, clonedValue, 0, cloneLength);

        return new Long2ObjectErsatzNavigableMap<>(clonedKey, clonedValue);
    }

    @SuppressWarnings("unchecked")
    private static <V> Long2ObjectErsatzNavigableMap<V> internalCloneBefore(Long2ObjectErsatzNavigableMap<V> toClone,
                                                                            int tillIndex) {
        assert(tillIndex > -1 && tillIndex < toClone.key.length);

        long[] clonedKey = new long[tillIndex + 2];
        V[] clonedValue = (V[]) new Object[tillIndex + 2];
        System.arraycopy(toClone.key, 0, clonedKey, 0, tillIndex + 1);
        System.arraycopy(toClone.value, 0, clonedValue, 0, tillIndex + 1);
        clonedKey[tillIndex + 1] = Long.MAX_VALUE;
        clonedValue[tillIndex + 1] = null;

        return new Long2ObjectErsatzNavigableMap<>(clonedKey, clonedValue);
    }

    @SuppressWarnings("unchecked")
    private static <V> Long2ObjectErsatzNavigableMap<V> internalCloneBetween(Long2ObjectErsatzNavigableMap<V> toClone,
                                                                      int fromIndex, int tillIndex) {

        int cloneLength = tillIndex - fromIndex;
        long[] clonedKey = new long[cloneLength + 2];
        V[] clonedValue = (V[]) new Object[cloneLength + 2];
        System.arraycopy(toClone.key, fromIndex, clonedKey, 0, cloneLength + 1);
        System.arraycopy(toClone.value, fromIndex, clonedValue, 0, cloneLength + 1);
        clonedKey[cloneLength + 1] = Long.MAX_VALUE;
        clonedValue[cloneLength + 1] = null;

        return new Long2ObjectErsatzNavigableMap<>(clonedKey, clonedValue);
    }

    /**
     * Clones the given map after the given starting point(inclusive).
     *
     * @param toClone the map to clone
     * @param from the starting point from which to clone the new map(inclusive)
     *
     * @return a clone of the map after the set starting point
     */
    public static <V> Long2ObjectErsatzNavigableMap<V> cloneAfter(Long2ObjectErsatzNavigableMap<V> toClone, long from) {
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
    public static <V> Long2ObjectErsatzNavigableMap<V> cloneBefore(Long2ObjectErsatzNavigableMap<V> toClone, long till) {
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
    public static <V> Long2ObjectErsatzNavigableMap<V> cloneBetween(Long2ObjectErsatzNavigableMap<V> toClone, long from,
                                                                 long till) {
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
    public V getFloorValue(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1) - 1;
        if (index < 0 || index >= this.key.length) {
            return null;
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
    public V getCeilValue(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1);
        if (index >= this.key.length) {
            return null;
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
    public Map.Entry<Long, V> getFloorEntry(long t) {
        int index = Arrays.binarySearch(this.key, t);
        if (index < 0) index = ((index + 1) * -1);
        if (index >= this.key.length) {
            return null;
        }
        return new BasicEntry<>(this.key[index], this.value[index]);
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
    public V[] getValue() {
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
    public Long2ObjectMapIterator subMapIterator(long from, boolean fromInclusive, long to,
                                                            boolean toInclusive) {

        int startingIndex = Arrays.binarySearch(this.key, from);
        int endIndex = Arrays.binarySearch(this.key, to);

        if (startingIndex < 0) startingIndex = ((startingIndex + 1) * -1) - 1;
        if (startingIndex < 0) {
            startingIndex = 0;
        }
        if (startingIndex >= this.key.length) {
            return new Long2ObjectMapIterator();
        }
        if (endIndex < 0) endIndex = ((endIndex + 1) * -1) - 1;
        endIndex--;
        if (endIndex < 0) {
            return new Long2ObjectMapIterator();
        }
        if (endIndex >= this.key.length) {
            endIndex = this.key.length - 1;
        }

        if (this.key[startingIndex] == from && !fromInclusive) {
            startingIndex++;
            if (startingIndex >= this.key.length) {
                return new Long2ObjectMapIterator();
            }
        }

        if (this.key[endIndex] == to && !toInclusive) {
            endIndex--;
            if (endIndex < 0) {
                return new Long2ObjectMapIterator();
            }
        }

        return new Long2ObjectMapIterator(startingIndex, endIndex);
    }

    /**
     * Returns an iteator over all entries of this map.
     *
     * @return an iteator over all entries of this map
     */
    public ObjectIterator<Long2ObjectMap.Entry<V>> entryIterator() {
        return new Long2ObjectMapIterator(0, this.key.length - 1);
    }

    private class Long2ObjectMapIterator implements ObjectIterator<Long2ObjectMap.Entry<V>> {
        int next;
        int curr = -1;
        int max = Long2ObjectErsatzNavigableMap.this.key.length;
        final BasicEntry<V> entry = new BasicEntry<>();

        public Long2ObjectMapIterator() {
        }

        public Long2ObjectMapIterator(int start, int end) {
            this.next = start;
            this.max = end;
        }

        public boolean hasNext() {
            return this.next <= this.max;
        }

        public Long2ObjectErsatzNavigableMap.BasicEntry<V> next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            } else {
                this.entry.key = Long2ObjectErsatzNavigableMap.this.key[this.curr = this.next];
                this.entry.value = Long2ObjectErsatzNavigableMap.this.value[this.next++];
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

    private static class BasicEntry<V> implements Long2ObjectMap.Entry<V> {
        protected long key;
        protected V value;
        public BasicEntry() {
        }
        public BasicEntry(final Long key, final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public long getLongKey() {
            return this.key;
        }

        @Override
        public V getValue() {
            return this.value;
        }

        @Override
        public V setValue(V value) {
            throw new UnsupportedOperationException();
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(final Object o) {
            if (!(o instanceof Map.Entry))
                return false;
            if (o instanceof Long2ObjectMap.Entry) {
                final Long2ObjectErsatzNavigableMap.BasicEntry<V> e = (Long2ObjectErsatzNavigableMap.BasicEntry<V>) o;
                return ((this.key) == (e.key)) && Objects.equals(this.value, e.value);
            }
            final Map.Entry<?, ?> e = (Map.Entry<?, ?>) o;
            final Object key = e.getKey();
            if (key == null || !(key instanceof Long))
                return false;
            final Object value = e.getValue();
            return ((this.key) == ((Long) (key))) && Objects.equals(this.value, (value));
        }
        @Override
        public int hashCode() {
            return it.unimi.dsi.fastutil.HashCommon.long2int(this.key) ^ this.value.hashCode();
        }
        @Override
        public String toString() {
            return this.key + "->" + this.value;
        }
    }
}
