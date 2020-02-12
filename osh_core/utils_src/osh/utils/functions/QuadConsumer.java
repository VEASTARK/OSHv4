package osh.utils.functions;

import java.io.Serializable;

/**
 * Represents a function that accepts four arguments and returns no result.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 * @param <W> the type of the forth argument to the function
 */
@FunctionalInterface
public interface QuadConsumer<T, U, V, W> extends Serializable {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     * @param w the forth input argument
     */
    void accept(T t, U u, V v, W w);
}
