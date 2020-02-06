package osh.utils.functions;

import java.io.Serializable;

/**
 * Represents a function that accepts no input and produces no result.
 *
 * @author Sebastian Kramer
 */
@FunctionalInterface
public interface SerializableOperator extends Serializable {

    /**
     *
     */
    void accept();
}
