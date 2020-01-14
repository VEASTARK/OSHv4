//  Int.java
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
import jmetal.util.Configuration;
import jmetal.util.JMException;
import jmetal.util.PseudoRandom;

/**
 * This class implements an integer decision encodings.variable
 */
public class Int extends Variable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int value_;       //Stores the value of the encodings.variable
    private int lowerBound_;  //Stores the lower limit of the encodings.variable
    private int upperBound_;  //Stores the upper limit of the encodings.variable

    /**
     * Constructor
     */
    public Int() {
        this.lowerBound_ = java.lang.Integer.MIN_VALUE;
        this.upperBound_ = java.lang.Integer.MAX_VALUE;
        this.value_ = 0;
    } // Int

    /**
     * Constructor
     *
     * @param lowerBound Variable lower bound
     * @param upperBound Variable upper bound
     */
    public Int(int lowerBound, int upperBound, PseudoRandom pseudoRandom) {
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;
        this.value_ = pseudoRandom.randInt(lowerBound, upperBound);
    } // Int

    /**
     * Constructor
     *
     * @param value      Value of the encodings.variable
     * @param lowerBound Variable lower bound
     * @param upperBound Variable upper bound
     */
    public Int(int value, int lowerBound, int upperBound) {
        super();

        this.value_ = value;
        this.lowerBound_ = lowerBound;
        this.upperBound_ = upperBound;
    } // Int

    /**
     * Copy constructor.
     *
     * @param variable Variable to be copied.
     * @throws JMException
     */
    public Int(Variable variable) throws JMException {
        this.lowerBound_ = (int) variable.getLowerBound();
        this.upperBound_ = (int) variable.getUpperBound();
        this.value_ = (int) variable.getValue();
    } // Int

    /**
     * Returns the value of the encodings.variable.
     *
     * @return the value.
     */
    @Override
    public double getValue() {
        return this.value_;
    } // getValue

    /**
     * Assigns a value to the encodings.variable.
     *
     * @param value The value.
     */
    @Override
    public void setValue(double value) {
        this.value_ = (int) value;
    } // setValue

    /**
     * Creates an exact copy of the <code>Int</code> object.
     *
     * @return the copy.
     */
    @Override
    public Variable deepCopy() {
        try {
            return new Int(this);
        } catch (JMException e) {
            Configuration.logger_.severe("Int.deepCopy.execute: JMException");
            return null;
        }
    } // deepCopy

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
     * @param lowerBound The lower bound value.
     */
    @Override
    public void setLowerBound(double lowerBound) {
        this.lowerBound_ = (int) lowerBound;
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
     * @param upperBound The new upper bound value.
     */
    @Override
    public void setUpperBound(double upperBound) {
        this.upperBound_ = (int) upperBound;
    } // setUpperBound

    /**
     * Returns a string representing the object
     *
     * @return The string
     */
    @Override
    public String toString() {
        return this.value_ + "";
    } // toString
} // Int
