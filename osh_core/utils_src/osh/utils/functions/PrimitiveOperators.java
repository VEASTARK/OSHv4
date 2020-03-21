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

    @FunctionalInterface
    interface HexDoubleLongOperator extends Serializable {
        double apply(double s, double t, double u, double v, double w, double x, long y);
    }

    @FunctionalInterface
    interface QuintDoubleLongOperator extends Serializable {
        double apply(double s, double t, double u, double v, double w, long x);
    }

    @FunctionalInterface
    interface QuadDoubleLongOperator extends Serializable {
        double apply(double s, double t, double u, double v, long w);
    }

    @FunctionalInterface
    interface TriDoubleLongOperator extends Serializable {
        double apply(double s, double t, double u, long v);
    }

    @FunctionalInterface
    interface BiDoubleLongOperator extends Serializable {
        double apply(double s, double t, long u);
    }

    @FunctionalInterface
    interface DoubleLongOperator extends Serializable {
        double apply(double s, long t);
    }

    @FunctionalInterface
    interface DoubleDoubleOperator<O> extends Serializable {
        double apply(double s);
    }
}
