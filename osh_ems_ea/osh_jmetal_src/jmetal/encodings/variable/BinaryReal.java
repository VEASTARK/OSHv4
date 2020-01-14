//  BinaryReal.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
// 
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.encodings.variable;

import jmetal.core.Variable;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

import java.util.BitSet;

/**
 * This class extends the Binary class to represent a Real encodings.variable encoded by
 * a binary string
 */
public class BinaryReal extends Binary {

    /**
     * Defines the default number of bits used for binary coded variables.
     */
    public static final int DEFAULT_PRECISION = 30;
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Stores the real value of the encodings.variable
     */
    private double value_;

    /**
     * Stores the lower limit for the encodings.variable
     */
    private double lowerBound_;

    /**
     * Stores the upper limit for the encodings.variable
     */
    private double upperBound_;

    /**
     * Constructor.
     */
    public BinaryReal() {
        super();
    } //BinaryReal

    /**
     * Constructor
     *
     * @param numberOfBits Length of the binary string.
     * @param lowerBound   The lower limit for the encodings.variable
     * @param upperBound   The upper limit for the encodings.variable.
     */
    public BinaryReal(
            int numberOfBits,
            double lowerBound,
            double upperBound,
            PseudoRandom pseudoRandom) {
        super(numberOfBits, pseudoRandom);
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;

        this.decode();
    } //BinaryReal

    /**
     * @param bits       BitSet
     * @param nbBits     Number of bits
     * @param lowerBound Lower bound
     * @param upperBound Upper bound
     */
    public BinaryReal(BitSet bits,
                      int nbBits,
                      double lowerBound,
                      double upperBound,
                      PseudoRandom pseudoRandom) {
        super(nbBits, pseudoRandom);
        this.bits_ = bits;
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;
        this.decode();
    } // BinaryReal

    /**
     * Copy constructor
     *
     * @param variable The encodings.variable to copy
     */
    public BinaryReal(BinaryReal variable) {
        super(variable);

        this.lowerBound_ = variable.lowerBound_;
        this.upperBound_ = variable.upperBound_;
    /*
    numberOfBits_ = encodings.variable.numberOfBits_;
     
    bits_ = new BitSet(numberOfBits_);
    for (int i = 0; i < numberOfBits_; i++)
      bits_.set(i,encodings.variable.bits_.get(i));
    */
        this.value_ = variable.value_;
    } //BinaryReal


    /**
     * Decodes the real value encoded in the binary string represented
     * by the <code>BinaryReal</code> object. The decoded value is stores in the
     * <code>value_</code> field and can be accessed by the method
     * <code>getValue</code>.
     */
    @Override
    public void decode() {
        double value = 0.0;
        for (int i = 0; i < this.numberOfBits_; i++) {
            if (this.bits_.get(i)) {
                value += Math.pow(2.0, i);
            }
        }

        this.value_ = value * (this.upperBound_ - this.lowerBound_) /
                (Math.pow(2.0, this.numberOfBits_) - 1.0);
        this.value_ += this.lowerBound_;
    } //decode

    /**
     * Returns the double value of the encodings.variable.
     *
     * @return the double value.
     */
    @Override
    public double getValue() {
        return this.value_;
    } //getValue

    /**
     * This implementation is efficient for binary string of length up to 24
     * bits, and for positive intervals.
     *
     * @see jmetal.core.Variable#setValue(double)
     * <p>
     * Contributor: jl hippolyte
     */
    @Override
    public void setValue(double value) throws JMException {
        if (this.numberOfBits_ <= 24 && this.lowerBound_ >= 0) {
            BitSet bitSet;
            if (value <= this.lowerBound_) {
                bitSet = new BitSet(this.numberOfBits_);
                bitSet.clear();
            } else if (value >= this.upperBound_) {
                bitSet = new BitSet(this.numberOfBits_);
                bitSet.set(0, this.numberOfBits_);
            } else {
                bitSet = new BitSet(this.numberOfBits_);
                bitSet.clear();
                // value is the integerToCode-th possible value, what is integerToCode?
                int integerToCode = 0;
                double tmp = this.lowerBound_;
                double path = (this.upperBound_ - this.lowerBound_) / (Math.pow(2.0, this.numberOfBits_) - 1);
                while (tmp < value) {
                    tmp += path;
                    integerToCode++;
                }
                int remain = integerToCode;
                for (int i = this.numberOfBits_ - 1; i >= 0; i--) {
                    //System.out.println("i= " + i);
                    int ithPowerOf2 = (int) Math.pow(2, i);

                    if (ithPowerOf2 <= remain) {
                        //System.out
                        //			.println(ithPowerOf2thValue + " <= " + remain);
                        bitSet.set(i);
                        remain -= ithPowerOf2;
                    } else {
                        bitSet.clear(i);
                    }
                }
            }
            this.bits_ = bitSet;
            this.decode();

        } else {
            if (this.lowerBound_ < 0)
                throw new JMException("Unsupported lowerbound: " + this.lowerBound_
                        + " > 0");
            if (this.numberOfBits_ >= 24)
                throw new JMException("Unsupported bit string length"
                        + this.numberOfBits_ + " is > 24 bits");
        }
    }// setValue

    /**
     * Creates an exact copy of a <code>BinaryReal</code> object.
     *
     * @return The copy of the object
     */
    @Override
    public Variable deepCopy() {
        return new BinaryReal(this);
    } //deepCopy

    /**
     * Returns the lower bound of the encodings.variable.
     *
     * @return the lower bound.
     */
    @Override
    public double getLowerBound() {
        return this.lowerBound_;
    } // getLowerBound

    /**
     * Sets the lower bound of the encodings.variable.
     *
     * @param lowerBound the lower bound.
     */
    @Override
    public void setLowerBound(double lowerBound) {
        this.lowerBound_ = lowerBound;
    } // setLowerBound

    /**
     * Returns the upper bound of the encodings.variable.
     *
     * @return the upper bound.
     */
    @Override
    public double getUpperBound() {
        return this.upperBound_;
    } // getUpperBound

    /**
     * Sets the upper bound of the encodings.variable.
     *
     * @param upperBound the upper bound.
     */
    @Override
    public void setUpperBound(double upperBound) {
        this.upperBound_ = upperBound;
    } // setUpperBound

    /**
     * Returns a string representing the object.
     *
     * @return the string.
     */
    @Override
    public String toString() {
        return this.value_ + "";
    } // toString
} // BinaryReal
