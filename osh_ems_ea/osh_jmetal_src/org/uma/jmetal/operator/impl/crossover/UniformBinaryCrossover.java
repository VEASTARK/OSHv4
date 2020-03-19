package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.List;

/**
 * This class allows to apply a uniform crossover operator on binary solutions using two parent
 * solutions.
 *
 * @author Sebastian Kramer
 */
public class UniformBinaryCrossover implements CrossoverOperator<BinarySolution> {

    private static final long serialVersionUID = 4628005367254756495L;

    private final double crossoverProbability;
    private final JMetalRandom randomGenerator;

    /**
     * Constructor
     */
    public UniformBinaryCrossover(double crossoverProbability) {
        if (crossoverProbability < 0) {
            throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
        }
        this.crossoverProbability = crossoverProbability;
        this.randomGenerator = JMetalRandom.getInstance();
    }

    @Override
    public List<BinarySolution> execute(List<BinarySolution> solutions) {
        if (solutions == null) {
            throw new JMetalException("Null parameter");
        } else if (solutions.size() != 2) {
            throw new JMetalException("There must be two parents instead of " + solutions.size());
        }

        return this.doCrossover(this.crossoverProbability, solutions.get(0), solutions.get(1));
    }

    /**
     * Perform the crossover operation.
     *
     * @param probability Crossover setProbability
     * @param parent1     The first parent
     * @param parent2     The second parent
     * @return An array containing the two offspring
     */
    public List<BinarySolution> doCrossover(double probability, BinarySolution parent1, BinarySolution parent2) {
        List<BinarySolution> offspring = new ArrayList<>(2);
        offspring.add((BinarySolution) parent1.copy());
        offspring.add((BinarySolution) parent2.copy());

        assert(parent1.getNumberOfVariables() == parent2.getNumberOfVariables());

        if (this.randomGenerator.nextDouble() < probability) {

            for (int i = 0; i < parent1.getNumberOfVariables(); i++) {
                int numberOfBits = parent1.getNumberOfBits(i);

                //aborting for 0 bit solutions
                if (numberOfBits == 0) {
                    continue;
                }

                //Step 1: Generate the crossover mask
                int[] crossoverMask = new int[numberOfBits];

                for (int k = 0; k < crossoverMask.length; k++) {
                    crossoverMask[k] = this.randomGenerator.nextInt(0, 2);
                }

                //Step 2: Do the crossover
                for (int k = 1; k < numberOfBits; k++) {
                    if (crossoverMask[k] == 0) {
                        boolean swap = offspring.get(0).getVariableValue(0).get(k);
                        offspring.get(0).getVariableValue(0).set(k, offspring.get(1).getVariableValue(0).get(k));
                        offspring.get(1).getVariableValue(0).set(k, swap);
                    }
                }
            }
        }
        return offspring;
    }

    @Override
    public int getNumberOfRequiredParents() {
        return 2;
    }

    @Override
    public int getNumberOfGeneratedChildren() {
        return 2;
    }
}
