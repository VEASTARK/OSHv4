package osh.utils.functions;

import java.io.Serializable;
import java.util.function.Consumer;

/**
 * Extension of the consumer interface that is serializable.
 *
 * @author Sebastian Kramer
 */
@FunctionalInterface
public interface SerializableConsumer<T> extends Consumer<T>, Serializable {
}
