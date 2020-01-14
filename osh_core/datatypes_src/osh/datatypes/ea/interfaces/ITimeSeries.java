package osh.datatypes.ea.interfaces;

/**
 * Represents a discrete function mapping from time domain to real values.
 * The domain of the function ranges from 0 to length().
 * Function value outside the domain is implicitly considered as 0.0.
 */
public interface ITimeSeries {
    /**
     * returns function value at specified time
     *
     * @param time
     * @return
     */
    double get(long time);

    /**
     * finds the next time the value changes
     *
     * @param time
     * @return
     */
    long getNextChange(long time);

    /**
     * sets one function value at the specified time
     */
    void set(long time, double value);

    /**
     * @return domain of function / time series
     */
    long length();

    /**
     * Augments the length of the time series (crops or fill series with 0.0)
     */
    void setLength(long newLength);

    /**
     * Function addition of two time series.
     * Result stored in this object.
     * Operand is shifted by offset first before operation.
     *
     * @param operand
     * @param offset
     */
    void add(ITimeSeries operand, long offset);

    /**
     * Function subtraction of two time series.
     * Result stored in this object.
     * Operand is shifted by offset first before operation.
     *
     * @param operand
     * @param offset
     */
    void sub(ITimeSeries operand, long offset);

    /**
     * Function multiplication of two time series.
     * Result stored in this object
     * Operand is shifted by offset first before operation.
     *
     * @param operand
     * @param offset
     */
    void multiply(ITimeSeries operand, long offset);

    /**
     * calculates discrete integral (sum) between two times
     *
     * @param from
     * @param to
     */
    double sum(long from, long to);

    /**
     * calculates discrete integral (sum) between two times, but only for positive values
     *
     * @param from
     * @param to
     * @return
     */
    double sumPositive(long from, long to);

    /**
     * calculates discrete integral (sum) between two times, but only for negative values
     *
     * @param from
     * @return
     */
    double sumNegative(long from, long to);

    ITimeSeries cloneMe();
}
