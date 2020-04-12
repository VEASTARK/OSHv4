package osh.utils.functions;

import java.io.Serializable;

/**
 * Represents a function that accepts three arguments and returns a forth.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the return type of the function
 */
@FunctionalInterface
public interface TriFunction<T, U, V, W> extends Serializable {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     * @return the result
     */
    W apply(T t, U u, V v);
}
