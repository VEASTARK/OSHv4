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

import org.uma.jmetal.algorithm.stoppingrule.EvaluationsStoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.AlgorithmBuilder;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

/**
 * Builder class for algorithm CDG
 *
 * @author Feng Zhang
 * @version 1.0
 */
public class CDGBuilder implements AlgorithmBuilder<AbstractCDG<DoubleSolution>> {

    protected final Problem<DoubleSolution> problem;
    private final IEALogger eaLogger;
    protected final double sigma_;
    /**
     * Delta in Zhang & Li paper
     */
    protected double neighborhoodSelectionProbability;
    protected CrossoverOperator<DoubleSolution> crossover;
    protected int populationSize;
    protected int resultPopulationSize;
    protected int numberOfThreads;
    protected int maxEvaluations;

    protected int k_;

    protected int t_;

    protected int subproblemNum_;

    protected int childGrid_;

    protected int childGridNum_;

    /**
     * Constructor
     */
    public CDGBuilder(Problem<DoubleSolution> problem, IEALogger eaLogger) {

        this.problem = problem;
        this.eaLogger = eaLogger;
        this.populationSize = 300;
        this.resultPopulationSize = 300;
        this.maxEvaluations = 300000;
        this.crossover = new DifferentialEvolutionCrossover();
        this.neighborhoodSelectionProbability = 0.9;
        this.numberOfThreads = 1;
        this.sigma_ = 10.0e-6;

        if (problem.getNumberOfObjectives() == 2) {
            this.k_ = 180;
            this.t_ = 1;
            this.childGrid_ = 60;
        } else if (problem.getNumberOfObjectives() == 3) {
            this.k_ = 25;
            this.t_ = 1;
            this.k_++;
            this.childGrid_ = 20;
        } else {
            this.k_ = 180;
            this.t_ = 5;
        }
        this.childGridNum_ = (int) Math.pow(this.childGrid_, problem.getNumberOfObjectives());
        this.childGridNum_++;
        this.subproblemNum_ = (int) Math.pow(this.k_, problem.getNumberOfObjectives() - 1);
        this.subproblemNum_ *= problem.getNumberOfObjectives();
    }

    /* Getters/Setters */
    public int getPopulationSize() {
        return this.populationSize;
    }

    public CDGBuilder setPopulationSize(int populationSize) {
        this.populationSize = populationSize;

        return this;
    }

    public int getMaxEvaluations() {
        return this.maxEvaluations;
    }

    public CDGBuilder setMaxEvaluations(int maxEvaluations) {
        this.maxEvaluations = maxEvaluations;

        return this;
    }

    public int getResultPopulationSize() {
        return this.resultPopulationSize;
    }

    public CDGBuilder setResultPopulationSize(int resultPopulationSize) {
        this.resultPopulationSize = resultPopulationSize;

        return this;
    }

    public CrossoverOperator<DoubleSolution> getCrossover() {
        return this.crossover;
    }

    public CDGBuilder setCrossover(CrossoverOperator<DoubleSolution> crossover) {
        this.crossover = crossover;

        return this;
    }

    public double getNeighborhoodSelectionProbability() {
        return this.neighborhoodSelectionProbability;
    }

    public CDGBuilder setNeighborhoodSelectionProbability(double neighborhoodSelectionProbability) {
        this.neighborhoodSelectionProbability = neighborhoodSelectionProbability;

        return this;
    }

    public int getNumberOfThreads() {
        return this.numberOfThreads;
    }

    public CDGBuilder setNumberOfThreads(int numberOfThreads) {
        this.numberOfThreads = numberOfThreads;

        return this;
    }

    public int getK() {
        return this.k_;
    }

    public CDGBuilder setK(int k) {
        this.k_ = k;

        return this;
    }

    public double getT() {
        return this.t_;
    }

    public CDGBuilder setT(int t) {
        this.t_ = t;

        return this;
    }

    public int getChildGrid() {
        return this.childGrid_;
    }

    public CDGBuilder setChildGrid(int childGrid) {
        this.childGrid_ = childGrid;

        return this;
    }

    public int getChildGridNum() {
        return this.childGridNum_;
    }

    public CDGBuilder setChildGridNum(int childGridNum) {
        this.childGridNum_ = childGridNum;

        return this;
    }

    public AbstractCDG<DoubleSolution> build() {
        AbstractCDG<DoubleSolution> algorithm;
        algorithm = new CDG(this.problem, this.populationSize, this.resultPopulationSize, this.maxEvaluations,
                this.crossover, this.neighborhoodSelectionProbability, this.sigma_, this.k_, this.t_, this.subproblemNum_,
                this.childGrid_, this.childGridNum_, this.eaLogger);
        algorithm.addStoppingRule(new EvaluationsStoppingRule(this.populationSize, this.maxEvaluations));
        return algorithm;
    }
}
