//  TwoPointsCrossover.java
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

package org.uma.jmetal.operator.impl.crossover;

import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.binarySet.BinarySet;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * This class allows to apply a n points crossover operator using two parent solutions.
 *
 * @author Sebastian Kramer
 */
public class BinaryNPointCrossover implements CrossoverOperator<BinarySolution> {

    private static final long serialVersionUID = 4004036238114613136L;

    private final double crossoverProbability;
    private final int pointsToUse;
    private final JMetalRandom randomGenerator;

    /**
     * Constructor
     * Creates a new instance of the n point crossover operator
     */
    public BinaryNPointCrossover(double crossoverProbability, int pointsToUse) {
        if (crossoverProbability < 0) {
            throw new JMetalException("Crossover probability is negative: " + crossoverProbability);
        }
        this.crossoverProbability = crossoverProbability;

        if (pointsToUse < 1) {
            throw new JMetalException("Crossover points is too small: " + pointsToUse);
        }
        this.pointsToUse = pointsToUse;
        this.randomGenerator = JMetalRandom.getInstance();
    }

    @Override
    public List<BinarySolution> execute(List<BinarySolution> solutions) {
        if (solutions == null) {
            throw new JMetalException("Null parameter");
        } else if (solutions.size() != 2) {
            throw new JMetalException("There must be two parents instead of " + solutions.size());
        }

        return this.doCrossover(this.crossoverProbability, this.pointsToUse, solutions.get(0), solutions.get(1));
    }

    /**
     * Perform the crossover operation.
     *
     * @param probability Crossover setProbability
     * @param parent1     The first parent
     * @param parent2     The second parent
     * @return An array containing the two offspring
     */
    public List<BinarySolution> doCrossover(double probability, int pointsToUse, BinarySolution parent1, BinarySolution parent2) {
        List<BinarySolution> offspring = new ArrayList<>(2);
        offspring.add((BinarySolution) parent1.copy());
        offspring.add((BinarySolution) parent2.copy());

        assert (parent1.getNumberOfVariables() == parent2.getNumberOfVariables());

        if (this.randomGenerator.nextDouble() < probability) {
            if (parent1.getNumberOfVariables() == 1) {
                BinarySet offspring1 = (BinarySet) parent1.getVariableValue(0).clone();
                BinarySet offspring2 = (BinarySet) parent2.getVariableValue(0).clone();
                this.doBinarySetCrossover(pointsToUse, parent1.getVariableValue(0), parent2.getVariableValue(0),
                        offspring1, offspring2);

                offspring.get(0).setVariableValue(0, offspring1);
                offspring.get(1).setVariableValue(0, offspring2);
            } else {
                //binary n-point crossover for multiple variables will be pretty rare so we do a simple and naive
                // solution that is proven to work (by reusing the code from the single variable crossover) but may
                // not be as ideal or performant as a specialized written version

                //we will concatenate together all variables to one giant BinarySet, perform a crossover on that and
                // then split it up at the saved bit-lengths

                int totalBits = parent1.getTotalNumberOfBits();
                int[] aggregateBitSum = new int[parent1.getNumberOfVariables()];
                for (int i = 0; i < parent1.getNumberOfVariables(); i++) {
                    for (int k = i; k < parent1.getNumberOfVariables(); k++) {
                        aggregateBitSum[k] += parent1.getNumberOfBits(k);
                    }
                }

                BinarySet parent1Union, parent2Union, offspring1Union, offspring2Union;

                parent1Union = new BinarySet(totalBits);
                parent2Union = new BinarySet(totalBits);

                parent1Union.or(parent1.getVariableValue(0));
                parent2Union.or(parent2.getVariableValue(0));

                for (int i = 1; i < parent1.getNumberOfVariables(); i++) {
                    BinarySet toOperate = parent1.getVariableValue(i);
                    int index = toOperate.nextSetBit(0);
                    while(index != -1) {
                        parent1Union.set(index + aggregateBitSum[i - 1]);
                        index = toOperate.nextSetBit(index);
                    }

                    toOperate = parent2.getVariableValue(i);
                    index = toOperate.nextSetBit(0);
                    while(index != -1) {
                        parent2Union.set(index + aggregateBitSum[i - 1]);
                        index = toOperate.nextSetBit(index);
                    }
                }

                offspring1Union = (BinarySet) parent1Union.clone();
                offspring2Union = (BinarySet) parent2Union.clone();

                this.doBinarySetCrossover(pointsToUse, parent1Union, parent2Union, offspring1Union, offspring2Union);

                int startIndex = 0;

                for (int i = 0; i < parent1.getNumberOfVariables(); i++) {
                    BinarySet variable1 = new BinarySet(aggregateBitSum[i] - startIndex);
                    BinarySet variable2 = new BinarySet(aggregateBitSum[i] - startIndex);

                    variable1.or(offspring1Union.get(startIndex, aggregateBitSum[i]));
                    variable2.or(offspring2Union.get(startIndex, aggregateBitSum[i]));

                    offspring.get(0).setVariableValue(i, variable1);
                    offspring.get(1).setVariableValue(i, variable2);
                }
            }
        }
        return offspring;
    }

    private void doBinarySetCrossover(int pointsToUse, BinarySet parent1, BinarySet parent2,
                                           BinarySet offspring1, BinarySet offspring2) {
        int points = pointsToUse;
        int numberOfBits = parent1.getBinarySetLength();

        //aborting for 0 bit solutions
        if (numberOfBits == 0) {
            return;
        }
        //adjusting for small bitstrings
        if (numberOfBits < points) {
            points = numberOfBits;
        }

        //Step 1: Generate the random cutting points and sort them
        HashSet<Integer> xoPoints = new HashSet<>(points);
        Integer[] crossoverPoints = new Integer[points + 1];

        while (xoPoints.size() != points) {
            while (!xoPoints.add(this.randomGenerator.nextInt(0, numberOfBits - 1))) ;
        }

        //adding start and finish for easier iterating
        //if start and/or end are selected as crossoverPoints then this will effectively reduce the number of xo-points
        xoPoints.add(0);
        xoPoints.add(numberOfBits);

        crossoverPoints = xoPoints.toArray(crossoverPoints);

        Arrays.sort(crossoverPoints);

        //Step 2: Do the crossover
        //using bitset operations is faster then just setting bits one after another
        BinarySet tmp1 = (BinarySet) parent1.clone();
        BinarySet tmp2 = (BinarySet) parent2.clone();

        for (int i = 1; i < crossoverPoints.length; i++) {
            if (i % 2 == 0) {
                offspring1.clear(crossoverPoints[i - 1], crossoverPoints[i]);
                offspring2.clear(crossoverPoints[i - 1], crossoverPoints[i]);
            } else {
                tmp1.clear(crossoverPoints[i - 1], crossoverPoints[i]);
                tmp2.clear(crossoverPoints[i - 1], crossoverPoints[i]);
            }
        }
        offspring1.or(tmp2);
        offspring2.or(tmp1);
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
