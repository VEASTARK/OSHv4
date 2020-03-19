package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.DoubleBinaryProblem;
import org.uma.jmetal.solution.DoubleBinarySolution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import osh.utils.DeepCopy;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * Description:
 * - this solution contains an array of double value + a binary string
 * - getNumberOfVariables() returns the number of double values + 1 (the string)
 * - getNumberOfDoubleVariables() returns the number of double values
 * - getNumberOfVariables() = getNumberOfDoubleVariables() + 1
 * - the bitset is the last variable
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultDoubleBinarySolution
        extends AbstractGenericSolution<Object, DoubleBinaryProblem<?>>
        implements DoubleBinarySolution {
    private final int numberOfDoubleVariables;

    /**
     * Constructor
     */
    public DefaultDoubleBinarySolution(DoubleBinaryProblem<?> problem) {
        super(problem);

        this.numberOfDoubleVariables = problem.getNumberOfDoubleVariables();

        this.initializeDoubleVariables(JMetalRandom.getInstance());
        this.initializeBitSet(JMetalRandom.getInstance());
        this.initializeObjectiveValues();
    }

    /**
     * Copy constructor
     */
    @SuppressWarnings("unchecked")
    public DefaultDoubleBinarySolution(DefaultDoubleBinarySolution solution) {
        super(solution.problem);
        this.numberOfDoubleVariables = solution.problem.getNumberOfDoubleVariables();

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        this.copyDoubleVariables(solution);
        this.copyBitSet(solution);

        this.attributes = (HashMap<Object, Object>) DeepCopy.copy(solution.attributes);
    }

    private void initializeDoubleVariables(JMetalRandom randomGenerator) {
        for (int i = 0; i < this.numberOfDoubleVariables; i++) {
            Double value = randomGenerator.nextDouble(this.getLowerBound(i), this.getUpperBound(i));
            //variables.add(value) ;
            this.setVariableValue(i, value);
        }
    }

    private void initializeBitSet(JMetalRandom randomGenerator) {
        BitSet bitset = this.createNewBitSet(this.problem.getNumberOfBits(), randomGenerator);
        this.setVariableValue(this.numberOfDoubleVariables, bitset);
    }

    private void copyDoubleVariables(DefaultDoubleBinarySolution solution) {
        for (int i = 0; i < this.numberOfDoubleVariables; i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }
    }

    private void copyBitSet(DefaultDoubleBinarySolution solution) {
        BitSet bitset = (BitSet) solution.getVariableValue(solution.getNumberOfVariables() - 1);
        this.setVariableValue(this.numberOfDoubleVariables, bitset);
    }

    @Override
    public int getNumberOfDoubleVariables() {
        return this.numberOfDoubleVariables;
    }

    @Override
    public Double getUpperBound(int index) {
        return (Double) this.problem.getUpperBound(index);
    }

    @Override
    public int getNumberOfBits() {
        return this.problem.getNumberOfBits();
    }

    @Override
    public Double getLowerBound(int index) {
        return (Double) this.problem.getLowerBound(index);
    }

    @Override
    public DefaultDoubleBinarySolution copy() {
        return new DefaultDoubleBinarySolution(this);
    }

    @Override
    public String getVariableValueString(int index) {
        return this.getVariableValue(index).toString();
    }

    private BitSet createNewBitSet(int numberOfBits, JMetalRandom randomGenerator) {
        BitSet bitSet = new BitSet(numberOfBits);

        for (int i = 0; i < numberOfBits; i++) {
            bitSet.set(i, randomGenerator.nextDouble() < 0.5);
        }
        return bitSet;
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
