package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.BinaryProblem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.HashMap;
import java.util.Map;

/**
 * Defines an implementation of a binary solution
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultBinarySolution
        extends AbstractGenericSolution<BinarySet, BinaryProblem>
        implements BinarySolution {

    /**
     * Constructor
     */
    public DefaultBinarySolution(BinaryProblem problem) {
        super(problem);

        this.initializeBinaryVariables(JMetalRandom.getInstance());
        this.initializeObjectiveValues();
    }

    /**
     * Copy constructor
     */
    public DefaultBinarySolution(DefaultBinarySolution solution) {
        super(solution.problem);

        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.setVariableValue(i, (BinarySet) solution.getVariableValue(i).clone());
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        this.attributes = new HashMap<>(solution.attributes);
    }

    private static BinarySet createNewBitSet(int numberOfBits, JMetalRandom randomGenerator) {
        BinarySet bitSet = new BinarySet(numberOfBits);

        for (int i = 0; i < numberOfBits; i++) {
            double rnd = randomGenerator.nextDouble();
            if (rnd < 0.5) {
                bitSet.set(i);
            } else {
                bitSet.clear(i);
            }
        }
        return bitSet;
    }

    @Override
    public int getNumberOfBits(int index) {
        return this.getVariableValue(index).getBinarySetLength();
    }

    @Override
    public DefaultBinarySolution copy() {
        return new DefaultBinarySolution(this);
    }

    @Override
    public int getTotalNumberOfBits() {
        int sum = 0;
        for (int i = 0; i < this.getNumberOfVariables(); i++) {
            sum += this.getVariableValue(i).getBinarySetLength();
        }

        return sum;
    }

    @Override
    public String getVariableValueString(int index) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.getVariableValue(index).getBinarySetLength(); i++) {
            if (this.getVariableValue(index).get(i)) {
                result.append("1");
            } else {
                result.append("0");
            }
        }
        return result.toString();
    }

    private void initializeBinaryVariables(JMetalRandom randomGenerator) {
        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.setVariableValue(i, createNewBitSet(this.problem.getNumberOfBits(i), randomGenerator));
        }
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
