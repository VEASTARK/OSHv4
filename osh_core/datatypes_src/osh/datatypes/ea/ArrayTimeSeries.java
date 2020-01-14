package osh.datatypes.ea;

import osh.datatypes.ea.interfaces.ITimeSeries;

import java.util.Arrays;

/**
 * Dense time series - use it for device profiles
 */
public class ArrayTimeSeries implements ITimeSeries {

    protected double[] values;

    public ArrayTimeSeries() {
        this.values = new double[0];
    }

    @Override
    public double get(long time) {
        if (time < this.length())
            return this.values[(int) time];
        else
            throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public void add(ITimeSeries operand, long offset) {
        long newLength = Math.max(this.length(), offset + operand.length());
        double[] newArray = Arrays.copyOf(this.values, (int) newLength);

        long maxIdx = Math.min(this.length(), offset + operand.length());
        for (long i = Math.max(0, offset); i < maxIdx; i++) {
            newArray[(int) i] += operand.get((int) (i - offset));
        }

        this.values = newArray;
    }

    @Override
    public void sub(ITimeSeries operand, long offset) {
        long newLength = Math.max(this.length(), offset + operand.length());
        double[] newArray = Arrays.copyOf(this.values, (int) newLength);

        long maxIdx = Math.min(this.length(), offset + operand.length());
        for (long i = Math.max(0, offset); i < maxIdx; i++) {
            newArray[(int) i] -= operand.get((int) (i - offset));
        }

        this.values = newArray;
    }

    @Override
    public void multiply(ITimeSeries operand, long offset) {
        long newLength = Math.max(this.length(), offset + operand.length());
        double[] newArray = Arrays.copyOf(this.values, (int) newLength);

        for (long i = 0; i < newLength; i++) {
            if (i < this.length()) {
                if (i - offset >= 0 && i - offset < operand.length()) {
                    newArray[(int) i] = this.get(i) * operand.get(i - offset);
                } else {
                    newArray[(int) i] = 0.0;
                }
            } else {
                newArray[(int) i] = 0.0;
            }
        }

        this.values = newArray;
    }

    @Override
    public double sum(long from, long to) {
        double s = 0.0;

        for (long i = from; i < to; i++) {
            s += this.values[(int) i];
        }

        return s;
    }

    @Override
    public long length() {
        return this.values.length;
    }

    @Override
    public void set(long time, double value) {
        if (time < this.length())
            this.values[(int) time] = value;
        else
            throw new ArrayIndexOutOfBoundsException();
    }

    @Override
    public void setLength(long newLength) {
        this.values = Arrays.copyOf(this.values, (int) newLength);
    }

    @Override
    public long getNextChange(long time) {
        double val = this.get(time);

        for (long i = time + 1; i < this.length(); i++) {
            if (this.get(i) != val)
                return i;
        }

        return -1;
    }

    @Override
    public double sumPositive(long from, long to) {
        double s = 0.0;

        for (long i = from; i < to; i++) {
            double v = this.values[(int) i];
            if (v > 0.0)
                s += v;
        }

        return s;
    }

    @Override
    public double sumNegative(long from, long to) {
        double s = 0.0;

        for (long i = from; i < to; i++) {
            double v = this.values[(int) i];
            if (v < 0.0)
                s += v;
        }

        return s;
    }

    @Override
    public ITimeSeries cloneMe() {
        ArrayTimeSeries ats = new ArrayTimeSeries();

        ats.values = Arrays.copyOf(this.values, this.values.length);

        return ats;
    }
}
