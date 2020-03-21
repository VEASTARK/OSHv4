package osh.utils.dataStructures;

import java.io.Serializable;
import java.util.Arrays;
import java.util.function.Function;

public class Enum2DoubleMap<K extends Enum<K>> implements Serializable, Cloneable {

    private static final long serialVersionUID = -2804162059596633009L;

    private final Class<K> keyType;
    private final K[] keyUniverse;
    private final double[] values;

    public Enum2DoubleMap(Class<K> keyType) {
        this.keyType = keyType;
        this.keyUniverse = getKeyUniverse(keyType);
        this.values = new double[this.keyUniverse.length];
        Arrays.fill(this.values, 0.0);
    }

    public Enum2DoubleMap(Enum2DoubleMap<K> m) {
        this.keyType = m.keyType;
        this.keyUniverse = m.keyUniverse;
        this.values = new double[this.keyUniverse.length];
        System.arraycopy(m.values, 0, this.values, 0, this.keyUniverse.length);
    }

    public boolean containsValue(double value) {
        for (double val : this.values)
            if (value == val)
                return true;

        return false;
    }

    public double get(Object key) {
        return (this.isValidKey(key) ? this.values[((Enum<?>)key).ordinal()] : 0.0);
    }

    public double put(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value)) return Double.NaN;

        int index = key.ordinal();
        double oldValue = this.values[index];
        this.values[index] = value;
        return oldValue;
    }

    public void add(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value)) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    public void addIfPositive(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value) || value < 0) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    public void addIfNegative(K key, double value) {
        this.typeCheck(key);
        if (Double.isNaN(value) || value > 0) return;

        int index = key.ordinal();
        this.values[index] += value;
    }

    public void addAll(Enum2DoubleMap<K> other) {
        for (K key : this.keyUniverse) {
            this.add(key, other.values[key.ordinal()]);
        }
    }

    public void compute(K key, Function<Double, Double> calculation) {
        this.typeCheck(key);
        int index = key.ordinal();

        Double result = calculation.apply(this.values[index]);
        if (Double.isNaN(result)) return;
        this.values[index] = result;
    }

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
     * Returns a shallow copy of this enum map.  (The values themselves
     * are not cloned.
     *
     * @return a shallow copy of this enum map
     */
    public Enum2DoubleMap<K> clone() {
        return new Enum2DoubleMap<>(this);
    }
}
