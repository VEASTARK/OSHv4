package osh.utils.functions;

import java.io.Serializable;
import java.util.function.Function;

/**
 * Extension of the function interface that is serializable.
 *
 * @author Sebastian Kramer
 */
@FunctionalInterface
public interface SerializableFunction<T, R> extends Function<T, R>, Serializable {
}
