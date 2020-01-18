package osh.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * represents a generic tuple of objects
 *
 * @author Sebastian Kramer
 */
public class Tuple<K, V> implements Serializable {

    private static final long serialVersionUID = -520076694436184937L;

    private K k;
    private V v;

    public Tuple(K k, V v) {
        this.k = k;
        this.v = v;
    }

    public K getFirst() {
        return this.k;
    }

    public void setFirst(K k) {
        this.k = k;
    }

    public V getSecond() {
        return this.v;
    }

    public void setSecond(V v) {
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(this.k, tuple.k) &&
                Objects.equals(this.v, tuple.v);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.k, this.v);
    }
}
