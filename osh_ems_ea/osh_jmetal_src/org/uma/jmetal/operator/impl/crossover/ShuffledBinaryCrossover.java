package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * This class allows to apply a shuffled crossover operator on binary solutions using two parent
 * solutions.
 *
 * @author Sebastian Kramer
 */
public class ShuffledBinaryCrossover implements CrossoverOperator<BinarySolution> {

    private static final long serialVersionUID = 2984671631565011924L;

    private final double crossoverProbability;
    private final JMetalRandom randomGenerator;

    /**
     * Constructor
     */
    public ShuffledBinaryCrossover(double crossoverProbability) {
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

        //TODO: make this operator work for multiple variables
        assert(parent1.getNumberOfVariables() == 1 && parent2.getNumberOfVariables() == 1);

        if (this.randomGenerator.nextDouble() < probability) {
            //Step 1: Generate the permutation array
            int numberOfBits = parent1.getTotalNumberOfBits();

            //aborting for 0 bit solutions
            if (numberOfBits == 0) {
                return offspring;
            }

            int[] permutation = new int[numberOfBits];

            for (int i = 1; i < numberOfBits; i++) {
                permutation[i] = i;
            }

            //knuth shuffle
            for (int i = 0; i < numberOfBits; i++) {
                // choose index uniformly in [i, N-1]
                int r = this.randomGenerator.nextInt(i, numberOfBits - 1);
                int swap = permutation[r];
                permutation[r] = permutation[i];
                permutation[i] = swap;
            }

            //Step 2: calculate a single crossover point and based on the permutation compute which bit in the resulting
            //		  offspring comes from which parent

            int crossoverPoint = this.randomGenerator.nextInt(0, numberOfBits - 1);

            BitSet ownBitsToKeep = new BitSet(numberOfBits);
            BitSet otherBitsToGet = new BitSet(numberOfBits);

            for (int i = 0; i < numberOfBits; i++) {
                if (permutation[i] < crossoverPoint) {
                    ownBitsToKeep.set(i);
                } else {
                    otherBitsToGet.set(i);
                }
            }

            BitSet secondBitForFirstBitset = new BitSet(numberOfBits);
            BitSet firstBitsForSecondBitset = new BitSet(numberOfBits);

            firstBitsForSecondBitset.or(offspring.get(0).getVariableValue(0));
            secondBitForFirstBitset.or(offspring.get(1).getVariableValue(0));

            secondBitForFirstBitset.and(otherBitsToGet);
            firstBitsForSecondBitset.and(otherBitsToGet);

            offspring.get(0).getVariableValue(0).and(ownBitsToKeep);
            offspring.get(1).getVariableValue(0).and(ownBitsToKeep);

            offspring.get(0).getVariableValue(0).or(secondBitForFirstBitset);
            offspring.get(1).getVariableValue(0).or(firstBitsForSecondBitset);

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
