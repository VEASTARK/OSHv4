package osh.utils.dataStructures;

import java.util.Arrays;
import java.util.function.Function;

/**
 * Represents a mapping from enum keys to double values using primitive datatypes in contrast to
 * {@link java.util.EnumMap}.
 *
 * @param <K> the type of the enum for the key values
 */
public class Enum2DoubleMap<K extends Enum<K>> implements Cloneable {

    private final Class<K> keyType;
    private final K[] keyUniverse;
    private final double[] values;

    /**
     * Constructs this map with the given key type.
     *
     * @param keyType the key type
     */
    public Enum2DoubleMap(Class<K> keyType) {
        this.keyType = keyType;
        this.keyUniverse = getKeyUniverse(keyType);
        this.values = new double[this.keyUniverse.length];
        Arrays.fill(this.values, 0.0);
    }

    /**
     * Clones the given map.
     *
     * @param m the map to clone
     */
    public Enum2DoubleMap(Enum2DoubleMap<K> m) {
        this.keyType = m.keyType;
        this.keyUniverse = m.keyUniverse;
        this.values = new double[this.keyUniverse.length];
        System.arraycopy(m.values, 0, this.values, 0, this.keyUniverse.length);
    }

    /**
     * Checks if the given value is contained in this map.
     *
     * @param value the value to check
     *
     * @return true if the given value is contained in this map
     */
    public boolean containsValue(double value) {
        for (double val : this.values)
            if (value == val)
                return true;

        return false;
    }

    /**
     * Returns the value mapped to the given key.
     *
     * @param key the key
     *
     * @return the value mapped to the given key or 0.0 if there is no such mapping
     */
    public double get(Object key) {
        return (this.isValidKey(key) ? this.values[((Enum<?>)key).ordinal()] : 0.0);
    }

    /**
     * Puts the given value at the given key in this map.
     *
     * @param key the key
     * @param value the value
     * @return the old value at the key
     */
    public double put(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value)) return Double.NaN;

        int index = key.ordinal();
        double oldValue = this.values[index];
        this.values[index] = value;
        return oldValue;
    }

    /**
     * Adds the given value to the existing value at the given key.
     *
     * @param key the key
     * @param value the value
     */
    public void add(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value)) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    /**
     * Adds the given value to the existing value at the given key if this given value is positive.
     *
     * @param key the key
     * @param value the value
     */
    public void addIfPositive(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value) || value < 0) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    /**
     * Adds the given value to the existing value at the given key if this given value is negative.
     *
     * @param key the key
     * @param value the value
     */
    public void addIfNegative(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value) || value > 0) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    /**
     * Adds all mappings of the given map to this map.
     *
     * @param other the map whose values are to be added
     */
    public void addAll(Enum2DoubleMap<K> other) {
        for (K key : this.keyUniverse) {
            this.add(key, other.values[key.ordinal()]);
        }
    }

    /**
     * Applies the given function to the value at the given key.
     *
     * @param key the key
     * @param calculation the function to apply
     */
    public void compute(K key, Function<Double, Double> calculation) {
        this.typeCheck(key);
        int index = key.ordinal();

        Double result = calculation.apply(this.values[index]);
        if (Double.isNaN(result)) return;
        this.values[index] = result;
    }

    /**
     * Removes and returns the mapping for the given key.
     *
     * @param key the key
     * @return the old value or 0.0 if there was no mapping
     */
    public double remove(Object key) {
        if (!this.isValidKey(key))
            return 0.0;
        int index = ((Enum<?>)key).ordinal();
        double oldValue = this.values[index];
        this.values[index] = 0.0;
        return oldValue;
    }

    private boolean isValidKey(Object key) {
        if (key == null)
            return false;

        // Cheaper than instanceof Enum followed by getDeclaringClass
        Class<?> keyClass = key.getClass();
        return keyClass == this.keyType || keyClass.getSuperclass() == this.keyType;
    }

    public void clear() {
        Arrays.fill(this.values, 0.0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o instanceof Enum2DoubleMap)
            return this.equals((Enum2DoubleMap<?>)o);
        return false;
    }

    private boolean equals(Enum2DoubleMap<?> em) {
        if (em.keyType != this.keyType)
            return false;

        // Key types match, compare each value
        for (int i = 0; i < this.keyUniverse.length; i++) {
            double ourValue = this.values[i];
            double hisValue = em.values[i];
            if (hisValue != ourValue)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int h = 0;

        for (int i = 0; i < this.keyUniverse.length; i++) {
            if (!Double.isNaN(this.values[i])) {
                h += this.entryHashCode(i);
            }
        }

        return h;
    }

    private int entryHashCode(int index) {
        return (this.keyUniverse[index].hashCode() ^ Double.hashCode(this.values[index]));
    }

    private void typeCheck(K key) {
        Class<?> keyClass = key.getClass();
        if (keyClass != this.keyType && keyClass.getSuperclass() != this.keyType)
            throw new ClassCastException(keyClass + " != " + this.keyType);
    }

    private static <K extends Enum<K>> K[] getKeyUniverse(Class<K> keyType) {
        return keyType.getEnumConstants();
    }

    /**
     * Returns a copy of this enum map.
     *
     * @return a copy of this enum map
     */
    public Enum2DoubleMap<K> clone() {
        return new Enum2DoubleMap<>(this);
    }
}
