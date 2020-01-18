//  ArrayReal.java
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

import jmetal.core.Problem;
import jmetal.core.Variable;
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;


/**
 * Class implementing a decision encodings.variable representing an array of real values.
 * The real values of the array have their own bounds.
 */
public class ArrayReal extends Variable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Stores an array of real values
     */
    public final Double[] array_;
    /**
     * Problem using the type
     */
    private final Problem problem_;
    /**
     * Stores the length of the array
     */
    private final int size_;

    /**
     * Constructor
     */
    public ArrayReal() {
        this.problem_ = null;
        this.size_ = 0;
        this.array_ = null;
    } // Constructor

    /**
     * Constructor
     *
     * @param size Size of the array
     */
    public ArrayReal(int size, Problem problem, PseudoRandom pseudoRandom) {
        this.problem_ = problem;
        this.size_ = size;
        this.array_ = new Double[this.size_];

        for (int i = 0; i < this.size_; i++) {
            this.array_[i] = pseudoRandom.randDouble() * (this.problem_.getUpperLimit(i) -
                    this.problem_.getLowerLimit(i)) +
                    this.problem_.getLowerLimit(i);
        } // for
    } // Constructor

    /**
     * Copy Constructor
     *
     * @param arrayReal The arrayReal to copy
     */
    private ArrayReal(ArrayReal arrayReal) {
        this.problem_ = arrayReal.problem_;
        this.size_ = arrayReal.size_;
        this.array_ = new Double[this.size_];

        System.arraycopy(arrayReal.array_, 0, this.array_, 0, this.size_);
    } // Copy Constructor

    @Override
    public Variable deepCopy() {
        return new ArrayReal(this);
    } // deepCopy

    /**
     * Returns the length of the arrayReal.
     *
     * @return The length
     */
    public int getLength() {
        return this.size_;
    } // getLength

    /**
     * getValue
     *
     * @param index Index of value to be returned
     * @return the value in position index
     */
    public double getValue(int index) throws JMException {
        if ((index >= 0) && (index < this.size_))
            return this.array_[index];
        else {
            Configuration.logger_.severe(jmetal.encodings.variable.ArrayReal.class + ".getValue(): index value (" + index + ") invalid");
            throw new JMException(jmetal.encodings.variable.ArrayReal.class + ".ArrayReal: index value (" + index + ") invalid");
        } // if
    } // getValue

    /**
     * setValue
     *
     * @param index Index of value to be returned
     * @param value The value to be set in position index
     */
    public void setValue(int index, double value) throws JMException {
        if ((index >= 0) && (index < this.size_))
            this.array_[index] = value;
        else {
            Configuration.logger_.severe(jmetal.encodings.variable.ArrayReal.class + ".setValue(): index value (" + index + ") invalid");
            throw new JMException(jmetal.encodings.variable.ArrayReal.class + ": index value (" + index + ") invalid");
        } // else
    } // setValue

    /**
     * Get the lower bound of a value
     *
     * @param index The index of the value
     * @return the lower bound
     */
    public double getLowerBound(int index) throws JMException {
        if ((index >= 0) && (index < this.size_))
            return this.problem_.getLowerLimit(index);
        else {
            Configuration.logger_.severe(jmetal.encodings.variable.ArrayReal.class + ".getLowerBound(): index value (" + index + ") invalid");
            throw new JMException(jmetal.encodings.variable.ArrayReal.class + ".getLowerBound: index value (" + index + ") invalid");
        } // else
    } // getLowerBound

    /**
     * Get the upper bound of a value
     *
     * @param index The index of the value
     * @return the upper bound
     */
    public double getUpperBound(int index) throws JMException {
        if ((index >= 0) && (index < this.size_))
            return this.problem_.getUpperLimit(index);
        else {
            Configuration.logger_.severe(jmetal.encodings.variable.ArrayReal.class + ".getUpperBound(): index value (" + index + ") invalid");
            throw new JMException(jmetal.encodings.variable.ArrayReal.class + ".getUpperBound: index value (" + index + ") invalid");
        } // else
    } // getLowerBound

    /**
     * Returns a string representing the object
     *
     * @return The string
     */
    @Override
    public String toString() {
        StringBuilder string;

        string = new StringBuilder();
        for (int i = 0; i < (this.size_ - 1); i++)
            string.append(this.array_[i]).append(" ");

        string.append(this.array_[this.size_ - 1]);
        return string.toString();
    } // toString
} // ArrayReal
