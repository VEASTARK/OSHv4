//  Solution.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Description: 
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

package jmetal.core;

import jmetal.encodings.variable.Binary;

import java.io.Serializable;

/**
 * Class representing a solution for a problem.
 */
public class Solution implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    /**
     * Stores the objectives values of the solution.
     */
    private final double[] objective_;
    /**
     * Stores the problem
     */
    private Problem problem_;
    /**
     * Stores the type of the encodings.variable
     */
    private SolutionType type_;
    /**
     * Stores the decision variables of the solution.
     */
    private Variable[] variable_;
    /**
     * Stores the number of objective values of the solution
     */
    private int numberOfObjectives_;

    /**
     * Stores the so called fitness value. Used in some metaheuristics
     */
    private double fitness_;

    /**
     * Used in algorithm AbYSS, this field is intended to be used to know
     * when a <code>Solution</code> is marked.
     */
    private boolean marked_;

    /**
     * Stores the so called rank of the solution. Used in NSGA-II
     */
    private int rank_;

    /**
     * Stores the overall constraint violation of the solution.
     */
    private double overallConstraintViolation_;

    /**
     * Stores the number of constraints violated by the solution.
     */
    private int numberOfViolatedConstraints_;

    /**
     * This field is intended to be used to know the location of
     * a solution into a <code>SolutionSet</code>. Used in MOCell
     */
    private int location_;

    /**
     * Stores the distance to his k-nearest neighbor into a
     * <code>SolutionSet</code>. Used in SPEA2.
     */
    private double kDistance_;

    /**
     * Stores the crowding distance of the the solution in a
     * <code>SolutionSet</code>. Used in NSGA-II.
     */
    private double crowdingDistance_;

    /**
     * Stores the distance between this solution and a <code>SolutionSet</code>.
     * Used in AbySS.
     */
    private double distanceToSolutionSet_;

    /**
     * Constructor.
     */
    public Solution() {
        this.problem_ = null;
        this.marked_ = false;
        this.overallConstraintViolation_ = 0.0;
        this.numberOfViolatedConstraints_ = 0;
        this.type_ = null;
        this.variable_ = null;
        this.objective_ = null;
    } // Solution

    /**
     * Constructor
     *
     * @param numberOfObjectives Number of objectives of the solution
     *                           <p>
     *                           This constructor is used mainly to read objective values from a file to
     *                           variables of a SolutionSet to apply quality indicators
     */
    public Solution(int numberOfObjectives) {
        this.numberOfObjectives_ = numberOfObjectives;
        this.objective_ = new double[numberOfObjectives];
    }

    /**
     * Constructor.
     *
     * @param problem The problem to solve
     */
    public Solution(Problem problem) {
        this.problem_ = problem;
        this.type_ = problem.getSolutionType();
        this.numberOfObjectives_ = problem.getNumberOfObjectives();
        this.objective_ = new double[this.numberOfObjectives_];

        // Setting initial values
        this.fitness_ = 0.0;
        this.kDistance_ = 0.0;
        this.crowdingDistance_ = 0.0;
        this.distanceToSolutionSet_ = Double.POSITIVE_INFINITY;
        //<-

        //variable_ = problem.solutionType_.createVariables() ;
        this.variable_ = this.type_.createVariables();
    } // Solution

    /**
     * Constructor
     *
     * @param problem The problem to solve
     */
    public Solution(Problem problem, Variable[] variables) {
        this.problem_ = problem;
        this.type_ = problem.getSolutionType();
        this.numberOfObjectives_ = problem.getNumberOfObjectives();
        this.objective_ = new double[this.numberOfObjectives_];

        // Setting initial values
        this.fitness_ = 0.0;
        this.kDistance_ = 0.0;
        this.crowdingDistance_ = 0.0;
        this.distanceToSolutionSet_ = Double.POSITIVE_INFINITY;
        //<-

        this.variable_ = variables;
    } // Constructor

    /**
     * Copy constructor.
     *
     * @param solution Solution to copy.
     */
    public Solution(Solution solution) {
        this.problem_ = solution.problem_;
        this.type_ = solution.type_;

        this.numberOfObjectives_ = solution.getNumberOfObjectives();
        this.objective_ = new double[this.numberOfObjectives_];
        for (int i = 0; i < this.objective_.length; i++) {
            this.objective_[i] = solution.getObjective(i);
        } // for
        //<-

        this.variable_ = this.type_.copyVariables(solution.variable_);
        this.overallConstraintViolation_ = solution.overallConstraintViolation_;
        this.numberOfViolatedConstraints_ = solution.numberOfViolatedConstraints_;
        this.distanceToSolutionSet_ = solution.distanceToSolutionSet_;
        this.crowdingDistance_ = solution.crowdingDistance_;
        this.kDistance_ = solution.kDistance_;
        this.fitness_ = solution.fitness_;
        this.marked_ = solution.marked_;
        this.rank_ = solution.rank_;
        this.location_ = solution.location_;
    } // Solution

    static public Solution getNewSolution(Problem problem) {
        return new Solution(problem);
    }

    /**
     * Gets the distance from the solution to a <code>SolutionSet</code>.
     * <b> REQUIRE </b>: this method has to be invoked after calling
     * <code>setDistanceToPopulation</code>.
     *
     * @return the distance to a specific solutionSet.
     */
    public double getDistanceToSolutionSet() {
        return this.distanceToSolutionSet_;
    } // getDistanceToSolutionSet

    /**
     * Sets the distance between this solution and a <code>SolutionSet</code>.
     * The value is stored in <code>distanceToSolutionSet_</code>.
     *
     * @param distance The distance to a solutionSet.
     */
    public void setDistanceToSolutionSet(double distance) {
        this.distanceToSolutionSet_ = distance;
    } // SetDistanceToSolutionSet

    /**
     * Gets the distance from the solution to his k-nearest nighbor in a
     * <code>SolutionSet</code>. Returns the value stored in
     * <code>kDistance_</code>. <b> REQUIRE </b>: this method has to be invoked
     * after calling <code>setKDistance</code>.
     *
     * @return the distance to k-nearest neighbor.
     */
    public double getKDistance() {
        return this.kDistance_;
    } // getKDistance

    /**
     * Sets the distance between the solution and its k-nearest neighbor in
     * a <code>SolutionSet</code>. The value is stored in <code>kDistance_</code>.
     *
     * @param distance The distance to the k-nearest neighbor.
     */
    public void setKDistance(double distance) {
        this.kDistance_ = distance;
    } // setKDistance

    /**
     * Gets the crowding distance of the solution into a <code>SolutionSet</code>.
     * Returns the value stored in <code>crowdingDistance_</code>.
     * <b> REQUIRE </b>: this method has to be invoked after calling
     * <code>setCrowdingDistance</code>.
     *
     * @return the distance crowding distance of the solution.
     */
    public double getCrowdingDistance() {
        return this.crowdingDistance_;
    } // getCrowdingDistance

    /**
     * Sets the crowding distance of a solution in a <code>SolutionSet</code>.
     * The value is stored in <code>crowdingDistance_</code>.
     *
     * @param distance The crowding distance of the solution.
     */
    public void setCrowdingDistance(double distance) {
        this.crowdingDistance_ = distance;
    } // setCrowdingDistance

    /**
     * Gets the fitness of the solution.
     * Returns the value of stored in the encodings.variable <code>fitness_</code>.
     * <b> REQUIRE </b>: This method has to be invoked after calling
     * <code>setFitness()</code>.
     *
     * @return the fitness.
     */
    public double getFitness() {
        return this.fitness_;
    } // getFitness

    /**
     * Sets the fitness of a solution.
     * The value is stored in <code>fitness_</code>.
     *
     * @param fitness The fitness of the solution.
     */
    public void setFitness(double fitness) {
        this.fitness_ = fitness;
    } // setFitness

    /**
     * Sets the value of the i-th objective.
     *
     * @param i     The number identifying the objective.
     * @param value The value to be stored.
     */
    public void setObjective(int i, double value) {
        this.objective_[i] = value;
    } // setObjective

    /**
     * Returns the value of the i-th objective.
     *
     * @param i The value of the objective.
     */
    public double getObjective(int i) {
        return this.objective_[i];
    } // getObjective

    /**
     * Returns the number of objectives.
     *
     * @return The number of objectives.
     */
    public int getNumberOfObjectives() {
        if (this.objective_ == null)
            return 0;
        else
            return this.numberOfObjectives_;
    } // getNumberOfObjectives

    /**
     * Returns the number of decision variables of the solution.
     *
     * @return The number of decision variables.
     */
    public int numberOfVariables() {
        return this.problem_.getNumberOfVariables();
    } // numberOfVariables

    /**
     * Returns a string representing the solution.
     *
     * @return The string.
     */
    public String toString() {
        StringBuilder aux = new StringBuilder();
        for (int i = 0; i < this.numberOfObjectives_; i++)
            aux.append(this.getObjective(i)).append(" ");

        return aux.toString();
    } // toString

    /**
     * Returns the decision variables of the solution.
     *
     * @return the <code>DecisionVariables</code> object representing the decision
     * variables of the solution.
     */
    public Variable[] getDecisionVariables() {
        return this.variable_;
    } // getDecisionVariables

    /**
     * Sets the decision variables for the solution.
     *
     * @param variables The <code>DecisionVariables</code> object
     *                  representing the decision variables of the solution.
     */
    public void setDecisionVariables(Variable[] variables) {
        this.variable_ = variables;
    } // setDecisionVariables

    public Problem getProblem() {
        return this.problem_;
    }

    /**
     * Indicates if the solution is marked.
     *
     * @return true if the method <code>marked</code> has been called and, after
     * that, the method <code>unmarked</code> hasn't been called. False in other
     * case.
     */
    public boolean isMarked() {
        return this.marked_;
    } // isMarked

    /**
     * Establishes the solution as marked.
     */
    public void marked() {
        this.marked_ = true;
    } // marked

    /**
     * Established the solution as unmarked.
     */
    public void unMarked() {
        this.marked_ = false;
    } // unMarked

    /**
     * Gets the rank of the solution.
     * <b> REQUIRE </b>: This method has to be invoked after calling
     * <code>setRank()</code>.
     *
     * @return the rank of the solution.
     */
    public int getRank() {
        return this.rank_;
    } // getRank

    /**
     * Sets the rank of a solution.
     *
     * @param value The rank of the solution.
     */
    public void setRank(int value) {
        this.rank_ = value;
    } // setRank

    /**
     * Gets the overall constraint violated by the solution.
     * <b> REQUIRE </b>: This method has to be invoked after calling
     * <code>overallConstraintViolation</code>.
     *
     * @return the overall constraint violation by the solution.
     */
    public double getOverallConstraintViolation() {
        return this.overallConstraintViolation_;
    }  //getOverallConstraintViolation

    /**
     * Sets the overall constraints violated by the solution.
     *
     * @param value The overall constraints violated by the solution.
     */
    public void setOverallConstraintViolation(double value) {
        this.overallConstraintViolation_ = value;
    } // setOverallConstraintViolation

    /**
     * Gets the number of constraint violated by the solution.
     * <b> REQUIRE </b>: This method has to be invoked after calling
     * <code>setNumberOfViolatedConstraint</code>.
     *
     * @return the number of constraints violated by the solution.
     */
    public int getNumberOfViolatedConstraint() {
        return this.numberOfViolatedConstraints_;
    } // getNumberOfViolatedConstraint

    /**
     * Sets the number of constraints violated by the solution.
     *
     * @param value The number of constraints violated by the solution.
     */
    public void setNumberOfViolatedConstraint(int value) {
        this.numberOfViolatedConstraints_ = value;
    } //setNumberOfViolatedConstraint

    /**
     * Gets the location of this solution in a <code>SolutionSet</code>.
     * <b> REQUIRE </b>: This method has to be invoked after calling
     * <code>setLocation</code>.
     *
     * @return the location of the solution into a solutionSet
     */
    public int getLocation() {
        return this.location_;
    } // getLocation

    /**
     * Sets the location of the solution into a solutionSet.
     *
     * @param location The location of the solution.
     */
    public void setLocation(int location) {
        this.location_ = location;
    } // setLocation

    /**
     * Sets the type of the encodings.variable.
     * @param type The type of the encodings.variable.
     */
    //public void setType(String type) {
    // type_ = Class.forName("") ;
    //} // setType

    /**
     * Gets the type of the encodings.variable
     *
     * @return the type of the encodings.variable
     */
    public SolutionType getType() {
        return this.type_;
    } // getType

    /**
     * Sets the type of the encodings.variable.
     *
     * @param type The type of the encodings.variable.
     */
    public void setType(SolutionType type) {
        this.type_ = type;
    } // setType

    /**
     * Returns the aggregative value of the solution
     *
     * @return The aggregative value.
     */
    public double getAggregateValue() {
        double value = 0.0;
        for (int i = 0; i < this.getNumberOfObjectives(); i++) {
            value += this.getObjective(i);
        }
        return value;
    } // getAggregativeValue

    /**
     * Returns the number of bits of the chromosome in case of using a binary
     * representation
     *
     * @return The number of bits if the case of binary variables, 0 otherwise
     * This method had a bug which was fixed by Rafael Olaechea
     */
    public int getNumberOfBits() {
        int bits = 0;

        for (Variable variable : this.variable_)
            if ((variable.getVariableType() == Binary.class) ||
                    (variable.getVariableType() == jmetal.encodings.variable.BinaryReal.class))

                bits += ((Binary) variable).getNumberOfBits();

        return bits;
    } // getNumberOfBits
} // Solution
