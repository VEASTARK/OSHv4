//  XInt.java
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

package jmetal.util.wrapper;

import jmetal.core.Solution;
import jmetal.core.SolutionType;
import jmetal.encodings.solutionType.ArrayIntSolutionType;
import jmetal.encodings.solutionType.IntSolutionType;
import jmetal.encodings.variable.ArrayInt;
import jmetal.util.Configuration;
import jmetal.util.JMException;

/**
 * Wrapper class for accessing integer-coded solutions
 */
public class XInt {
    private Solution solution_;
    private SolutionType type_;

    /**
     * Constructor
     */
    private XInt() {
    } // Constructor

    /**
     * Constructor
     *
     * @param solution
     */
    public XInt(Solution solution) {
        this();
        this.type_ = solution.getType();
        this.solution_ = solution;
    }

    /**
     * Gets value of a encodings.variable
     *
     * @param index Index of the encodings.variable
     * @return The value of the encodings.variable
     * @throws JMException
     */
    public int getValue(int index) throws JMException {
        if (this.type_.getClass() == IntSolutionType.class) {
            return (int) this.solution_.getDecisionVariables()[index].getValue();
        } else if (this.type_.getClass() == ArrayIntSolutionType.class) {
            return ((ArrayInt) (this.solution_.getDecisionVariables()[0])).array_[index];
        } else {
            Configuration.logger_.severe("jmetal.util.wrapper.XInt.getValue, solution type " +
                    this.type_ + "+ invalid");
        }
        return 0;
    } // Get value

    /**
     * Sets the value of a encodings.variable
     *
     * @param index Index of the encodings.variable
     * @param value Value to be assigned
     * @throws JMException
     */
    public void setValue(int index, int value) throws JMException {
        if (this.type_.getClass() == IntSolutionType.class)
            this.solution_.getDecisionVariables()[index].setValue(value);
        else if (this.type_.getClass() == ArrayIntSolutionType.class)
            ((ArrayInt) (this.solution_.getDecisionVariables()[0])).array_[index] = value;
        else
            Configuration.logger_.severe("jmetal.util.wrapper.XInt.setValue, solution type " +
                    this.type_ + "+ invalid");
    } // setValue

    /**
     * Gets the lower bound of a encodings.variable
     *
     * @param index Index of the encodings.variable
     * @return The lower bound of the encodings.variable
     * @throws JMException
     */
    public int getLowerBound(int index) throws JMException {
        if (this.type_.getClass() == IntSolutionType.class)
            return (int) this.solution_.getDecisionVariables()[index].getLowerBound();
        else if (this.type_.getClass() == ArrayIntSolutionType.class)
            return (int) ((ArrayInt) (this.solution_.getDecisionVariables()[0])).getLowerBound(index);
        else {
            Configuration.logger_.severe("jmetal.util.wrapper.XInt.getLowerBound, solution type " +
                    this.type_ + "+ invalid");
        }
        return 0;
    } // getLowerBound

    /**
     * Gets the upper bound of a encodings.variable
     *
     * @param index Index of the encodings.variable
     * @return The upper bound of the encodings.variable
     * @throws JMException
     */
    public int getUpperBound(int index) throws JMException {
        if (this.type_.getClass() == IntSolutionType.class)
            return (int) this.solution_.getDecisionVariables()[index].getUpperBound();
        else if (this.type_.getClass() == ArrayIntSolutionType.class)
            return (int) ((ArrayInt) (this.solution_.getDecisionVariables()[0])).getUpperBound(index);
        else
            Configuration.logger_.severe("jmetal.util.wrapper.XInt.getUpperBound, solution type " +
                    this.type_ + "+ invalid");

        return 0;
    } // getUpperBound

    /**
     * Returns the number of variables of the solution
     *
     * @return
     */
    public int getNumberOfDecisionVariables() {
        if (this.type_.getClass() == IntSolutionType.class)
            return this.solution_.getDecisionVariables().length;
        else if (this.type_.getClass() == ArrayIntSolutionType.class)
            return ((ArrayInt) (this.solution_.getDecisionVariables()[0])).getLength();
        else
            Configuration.logger_.severe("jmetal.util.wrapper.XInt.size, solution type " +
                    this.type_ + "+ invalid");
        return 0;
    } // size
} // XInt