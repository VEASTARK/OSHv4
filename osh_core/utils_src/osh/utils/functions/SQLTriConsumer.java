package osh.utils.functions;

import java.io.Serializable;
import java.sql.SQLException;

/**
 * Represents a function that accepts three arguments and returns no result.
 *
 * @param <T> the type of the first argument to the function
 * @param <U> the type of the second argument to the function
 * @param <V> the type of the third argument to the function
 */
@FunctionalInterface
public interface SQLTriConsumer<T, U, V> extends Serializable {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @param v the third input argument
     */
    void accept(T t, U u, V v) throws SQLException;
}
