package org.uma.jmetal.solution.impl;

import org.uma.jmetal.problem.PermutationProblem;
import org.uma.jmetal.solution.PermutationSolution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines an implementation of solution composed of a permuation of integers
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class DefaultIntegerPermutationSolution
        extends AbstractGenericSolution<Integer, PermutationProblem<?>>
        implements PermutationSolution<Integer> {

    /**
     * Constructor
     */
    public DefaultIntegerPermutationSolution(PermutationProblem<?> problem) {
        super(problem);

        List<Integer> randomSequence = new ArrayList<>(problem.getPermutationLength());

        for (int j = 0; j < problem.getPermutationLength(); j++) {
            randomSequence.add(j);
        }

        java.util.Collections.shuffle(randomSequence);

        for (int i = 0; i < this.getNumberOfVariables(); i++) {
            this.setVariableValue(i, randomSequence.get(i));
        }
    }

    /**
     * Copy Constructor
     */
    public DefaultIntegerPermutationSolution(DefaultIntegerPermutationSolution solution) {
        super(solution.problem);
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.setObjective(i, solution.getObjective(i));
        }

        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.setVariableValue(i, solution.getVariableValue(i));
        }

        this.attributes = new HashMap<>(solution.attributes);
    }

    @Override
    public String getVariableValueString(int index) {
        return this.getVariableValue(index).toString();
    }

    @Override
    public DefaultIntegerPermutationSolution copy() {
        return new DefaultIntegerPermutationSolution(this);
    }

    @Override
    public Map<Object, Object> getAttributes() {
        return this.attributes;
    }
}
