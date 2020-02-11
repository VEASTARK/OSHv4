package osh.utils.functions;

import java.io.Serializable;
import java.util.function.ToDoubleFunction;

/**
 * Collections of extension to operators on primitive datatypes that are also serializable.
 */
public interface PrimitiveOperators {

    /**
     *  Represents a function that accepts an argument and produces a double-valued result.
     *
     * @param <T> the type of the first argument to the function
     */
    @FunctionalInterface
    interface SerializableToDoubleFunction<T> extends ToDoubleFunction<T>, Serializable { }
}
