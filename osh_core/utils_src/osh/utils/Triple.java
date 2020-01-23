package osh.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * A generic triple of elements
 *
 * @param <T> the type of the first element
 * @param <U> the type of the second element
 * @param <V> the type of the third element
 */
public class Triple<T, U, V> implements Serializable {

    private static final long serialVersionUID = 7422358370416324145L;
    private T t;
    private U u;
    private V v;

    /**
     * Generates a triple of the given elements.
     *
     * @param t the first element of this triple
     * @param u the second element of this triple
     * @param v the third element of this triple
     */
    public Triple(T t, U u, V v) {
        this.t = t;
        this.u = u;
        this.v = v;
    }

    /**
     * Returns the first element of this triple.
     * @return the first element of the triple
     */
    public T getFirst() {
        return this.t;
    }

    /**
     * Sets the first element of this triple to the given value.
     * @param t the new first element
     */
    public void setFirst(T t) {
        this.t = t;
    }

    /**
     * Returns the second element of this triple.
     * @return the second element of the triple
     */
    public U getSecond() {
        return this.u;
    }

    /**
     * Sets the second element of this triple to the given value.
     * @param u the new second element
     */
    public void setSecond(U u) {
        this.u = u;
    }

    /**
     * Returns the third element of this triple.
     * @return the third element of the triple
     */
    public V getThird() {
        return this.v;
    }

    /**
     * Sets the third element of this triple to the given value.
     * @param v the new third element
     */
    public void setThird(V v) {
        this.v = v;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
        return Objects.equals(this.t, triple.t) &&
                Objects.equals(this.u, triple.u) &&
                Objects.equals(this.v, triple.v);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.t, this.u, this.v);
    }
}
