package osh.utils;

import java.io.Serializable;
import java.util.Objects;

/**
 * A generic tuple of elements
 *
 * @param <T> the type of the first element
 * @param <U> the type of the second element
 */
public class Tuple<T, U> implements Serializable {

    private static final long serialVersionUID = -6742311460616489454L;
    private T t;
    private U u;

    /**
     * Generates a tuple of the given elements.
     *
     * @param t the first element of this tuple
     * @param u the second element of this tuple
     */
    public Tuple(T t, U u) {
        this.t = t;
        this.u = u;
    }

    /**
     * Returns the first element of this tuple.
     * @return the first element of the tuple
     */
    public T getFirst() {
        return this.t;
    }

    /**
     * Sets the first element of this tuple to the given value.
     * @param t the new first element
     */
    public void setFirst(T t) {
        this.t = t;
    }

    /**
     * Returns the second element of this tuple.
     * @return the second element of the tuple
     */
    public U getSecond() {
        return this.u;
    }

    /**
     * Sets the second element of this tuple to the given value.
     * @param u the new second element
     */
    public void setSecond(U u) {
        this.u = u;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        Tuple<?, ?> tuple = (Tuple<?, ?>) o;
        return Objects.equals(this.t, tuple.t) &&
                Objects.equals(this.u, tuple.u);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.t, this.u);
    }
}
