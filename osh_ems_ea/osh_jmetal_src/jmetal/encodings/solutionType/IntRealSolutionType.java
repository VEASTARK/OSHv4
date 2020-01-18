//  IntRealSolutionType.java
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

package jmetal.encodings.solutionType;

import jmetal.core.Problem;
import jmetal.core.SolutionType;
import jmetal.core.Variable;
import jmetal.encodings.variable.Int;
import jmetal.encodings.variable.Real;
import jmetal.util.PseudoRandom;

/**
 * Class representing  a solution type including two variables: an integer
 * and a real.
 */
public class IntRealSolutionType extends SolutionType {
    private final int intVariables_;
    private final int realVariables_;

    /**
     * Constructor
     *
     * @param problem       Problem to solve
     * @param intVariables  Number of integer variables
     * @param realVariables Number of real variables
     */
    public IntRealSolutionType(
            Problem problem,
            int intVariables,
            int realVariables,
            PseudoRandom pseudoRandom) {
        super(problem);
        this.intVariables_ = intVariables;
        this.realVariables_ = realVariables;
    } // Constructor

    /**
     * Creates the variables of the solution
     *
     */
    @Override
    public Variable[] createVariables() {
        Variable[] variables = new Variable[this.problem_.getNumberOfVariables()];

        for (int var = 0; var < this.intVariables_; var++)
            variables[var] = new Int(
                    (int) this.problem_.getLowerLimit(var),
                    (int) this.problem_.getUpperLimit(var),
                    this.pseudoRandom);

        for (int var = this.intVariables_; var < (this.intVariables_ + this.realVariables_); var++)
            variables[var] = new Real(
                    this.problem_.getLowerLimit(var),
                    this.problem_.getUpperLimit(var),
                    this.pseudoRandom);

        return variables;
    } // createVariables
} // IntRealSolutionType
