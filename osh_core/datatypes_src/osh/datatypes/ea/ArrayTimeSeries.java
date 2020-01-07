package osh.datatypes.ea;

import osh.datatypes.ea.interfaces.ITimeSeries;

import java.util.Arrays;

/**
 * Dense time series - use it for device profiles
 * 
 */
public class ArrayTimeSeries implements ITimeSeries {

	protected double[] values;

	public ArrayTimeSeries() {
		values = new double[0];
	}
	
	@Override
	public double get(long time) {
		if( time < length() )
			return values[(int) time];
		else
			throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public void add(ITimeSeries operand, long offset) {
		long newLength = Math.max(this.length(), offset+operand.length());
		double[] newArray = Arrays.copyOf(values, (int) newLength);

		long maxIdx = Math.min(this.length(), offset+operand.length());
		for( long i = Math.max(0, offset); i < maxIdx; i++ ) {
			newArray[(int) i] += operand.get((int) (i-offset));
		}
		
		values = newArray;
	}

	@Override
	public void sub(ITimeSeries operand, long offset) {
		long newLength = Math.max(this.length(), offset+operand.length());
		double[] newArray = Arrays.copyOf(values, (int) newLength);

		long maxIdx = Math.min(this.length(), offset+operand.length());
		for( long i = Math.max(0, offset); i < maxIdx; i++ ) {
			newArray[(int) i] -= operand.get((int) (i-offset));
		}
		
		values = newArray;
	}
	
	@Override
	public void multiply(ITimeSeries operand, long offset) {
		long newLength = Math.max(this.length(), offset+operand.length());
		double[] newArray = Arrays.copyOf(values, (int) newLength);

		for( long i = 0; i < newLength; i++ ) {
			if( i < this.length() ) {
				if( i-offset >= 0 && i-offset < operand.length() ) {
					newArray[(int) i] = this.get(i) * operand.get(i-offset);
				} else {
					newArray[(int) i] = 0.0;
				}
			} else {
				newArray[(int) i] = 0.0;
			}
		}
		
		values = newArray;
	}

	@Override
	public double sum(long from, long to) {
		double s = 0.0;
		
		for( long i = from; i < to; i++ ) {
			s += values[(int) i];
		}
		
		return s;
	}

	@Override
	public long length() {
		return values.length;
	}

	@Override
	public void set(long time, double value) {
		if( time < length() )
			values[(int) time] = value;
		else
			throw new ArrayIndexOutOfBoundsException();
	}

	@Override
	public void setLength(long newLength) {
        values = Arrays.copyOf(values, (int) newLength);
	}

	@Override
	public long getNextChange(long time) {
		double val = get(time);
		
		for( long i = time+1; i < length(); i++ ) {
			if( get(i) != val )
				return i;
		}
			
		return -1;
	}

	@Override
	public double sumPositive(long from, long to) {
		double s = 0.0;
		
		for( long i = from; i < to; i++ ) {
			double v = values[(int) i]; 
			if( v > 0.0 )
				s += v;
		}
		
		return s;
	}

	@Override
	public double sumNegative(long from, long to) {
		double s = 0.0;
		
		for( long i = from; i < to; i++ ) {
			double v = values[(int) i]; 
			if( v < 0.0 )
				s += v;
		}
		
		return s;
	}

	@Override
	public ITimeSeries cloneMe() {
		ArrayTimeSeries ats = new ArrayTimeSeries();
		
		ats.values = Arrays.copyOf(values, values.length);
		
		return ats;
	}
}
