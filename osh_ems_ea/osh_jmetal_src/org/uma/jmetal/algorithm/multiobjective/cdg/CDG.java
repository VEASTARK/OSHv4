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

package org.uma.jmetal.algorithm.multiobjective.cdg;

import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.mutation.CDGMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;

import java.util.List;

/**
 * Xinye Cai, Zhiwei Mei, Zhun Fan, Qingfu Zhang,
 * A Constrained Decomposition Approach with Grids for Evolutionary Multiobjective Optimization,
 * IEEE Transaction on Evolutionary Computation, press online, 2018, DOI: 10.1109/TEVC.2017.2744674
 * The paper and Matlab code can be download at
 * http://xinyecai.github.io/
 *
 * @author Feng Zhang
 * @version 1.0
 */
@SuppressWarnings("serial")
public class CDG extends AbstractCDG<DoubleSolution> {
    private final DifferentialEvolutionCrossover differentialEvolutionCrossover;

    public CDG(Problem<DoubleSolution> problem,
               int populationSize,
               int resultPopulationSize,
               int maxEvaluations,
               CrossoverOperator<DoubleSolution> crossover,
               double neighborhoodSelectionProbability,
               double sigma_,
               int k_,
               int t_,
               int subproblemNum_,
               int childGrid_,
               int childGridNum_) {
        super(problem, populationSize, resultPopulationSize, maxEvaluations, crossover,
                neighborhoodSelectionProbability, sigma_, k_, t_, subproblemNum_, childGrid_, childGridNum_);

        this.differentialEvolutionCrossover = (DifferentialEvolutionCrossover) this.crossoverOperator;
    }

    @Override
    public void run() {
        this.initializePopulation();

        this.initializeIdealPoint();

        this.initializeNadirPoint();

        this.evaluations = this.populationSize;

        int gen = 0;

        double mutationProbability = 1.0 / this.problem.getNumberOfVariables();

        double delta;

        do {

            this.updateNeighborhood();

            delta = Math.pow((1 - this.evaluations / this.maxEvaluations), 0.7);

            int[] permutation = new int[this.populationSize];
            MOEADUtils.randomPermutation(permutation, this.populationSize);

            MutationOperator<DoubleSolution> mutation = new CDGMutation(mutationProbability, delta);

            for (int i = 0; i < this.populationSize; i++) {
                int subProblemId = permutation[i];

                NeighborType neighborType = this.chooseNeighborType(subProblemId);
                List<DoubleSolution> parents = this.parentSelection(subProblemId, neighborType);

                this.differentialEvolutionCrossover.setCurrentSolution(this.population.get(subProblemId));
                List<DoubleSolution> children = this.differentialEvolutionCrossover.execute(parents);

                DoubleSolution child = children.get(0);
                mutation.execute(child);

                this.problem.evaluate(child);

                this.evaluations++;

                this.initialCDGAttributes(child);

                this.population.add(child);
            }

            gen++;

            for (DoubleSolution doubleSolution : this.population) this.updateIdealPoint(doubleSolution);

            if (gen % 20 == 0)
                this.initializeNadirPoint();

            if (this.problem.getNumberOfObjectives() == 3) {
                this.updateBorder();
                this.excludeBadSolution3();
                this.chooseSpecialPopulation();
                this.gridSystemSetup3();
            } else {
                this.excludeBadSolution();
                this.chooseSpecialPopulation();
                this.gridSystemSetup();
            }

            if (this.population.size() < this.populationSize)
                this.supplyBadSolution();
            else
                this.rankBasedSelection();

        } while (!this.isStoppingConditionReached());
    }

    protected void initializePopulation() {
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newSolution = this.problem.createSolution();

            this.problem.evaluate(newSolution);
            this.initialCDGAttributes(newSolution);
            this.population.add(newSolution);

        }
    }

    public boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.population)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String getName() {
        return "CDG";
    }

    @Override
    public String getDescription() {
        return "A Constrained Decomposition Approach with Grids for Evolutionary Multiobjective Optimization";
    }
}
