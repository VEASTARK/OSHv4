package org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization;

import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.Operator;
import org.uma.jmetal.operator.impl.selection.BestSolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.neighborhood.impl.AdaptiveRandomNeighborhood;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Class implementing a Standard PSO 2007 algorithm.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class StandardPSO2007 extends AbstractParticleSwarmOptimization<DoubleSolution, DoubleSolution> {
    private final DoubleProblem problem;
    private final SolutionListEvaluator<DoubleSolution> evaluator;

    private final Operator<List<DoubleSolution>, DoubleSolution> findBestSolution;
    private final Comparator<DoubleSolution> fitnessComparator;
    private final int swarmSize;
    private final int numberOfParticlesToInform;
    private final DoubleSolution[] localBest;
    private final DoubleSolution[] neighborhoodBest;
    private final double[][] speed;
    private final AdaptiveRandomNeighborhood<DoubleSolution> neighborhood;
    private final GenericSolutionAttribute<DoubleSolution, Integer> positionInSwarm;
    private final double weight;
    private final double c;
    private final JMetalRandom randomGenerator = JMetalRandom.getInstance();
    private final int objectiveId;
    private int iterations;
    private DoubleSolution bestFoundParticle;

    /**
     * Constructor
     *
     * @param problem
     * @param objectiveId               This field indicates which objective, in the case of a multi-objective problem,
     *                                  is selected to be optimized.
     * @param swarmSize
     * @param numberOfParticlesToInform
     * @param evaluator
     */
    public StandardPSO2007(DoubleProblem problem, int objectiveId, int swarmSize,
                           int numberOfParticlesToInform, SolutionListEvaluator<DoubleSolution> evaluator) {
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.numberOfParticlesToInform = numberOfParticlesToInform;
        this.evaluator = evaluator;
        this.objectiveId = objectiveId;

        this.weight = 1.0 / (2.0 * Math.log(2));
        this.c = 1.0 / 2.0 + Math.log(2);

        this.fitnessComparator = new ObjectiveComparator<>(objectiveId);
        this.findBestSolution = new BestSolutionSelection<>(this.fitnessComparator);

        this.localBest = new DoubleSolution[swarmSize];
        this.neighborhoodBest = new DoubleSolution[swarmSize];
        this.speed = new double[swarmSize][problem.getNumberOfVariables()];

        this.positionInSwarm = new GenericSolutionAttribute<>();

        this.bestFoundParticle = null;
        this.neighborhood = new AdaptiveRandomNeighborhood<>(swarmSize, this.numberOfParticlesToInform);
    }

    /**
     * Constructor
     *
     * @param problem
     * @param swarmSize
     * @param numberOfParticlesToInform
     * @param evaluator
     */
    public StandardPSO2007(DoubleProblem problem, int swarmSize, int numberOfParticlesToInform,
                           SolutionListEvaluator<DoubleSolution> evaluator) {
        this(problem, 0, swarmSize, numberOfParticlesToInform, evaluator);
    }

    @Override
    public void initProgress() {
        this.iterations = 1;
    }

    @Override
    public void updateProgress() {
        this.iterations += 1;
    }

    @Override
    public boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations, -1, Arrays.asList(this.localBest))) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<DoubleSolution> createInitialSwarm() {
        List<DoubleSolution> swarm = new ArrayList<>(this.swarmSize);

        DoubleSolution newSolution;
        for (int i = 0; i < this.swarmSize; i++) {
            newSolution = this.problem.createSolution();
            this.positionInSwarm.setAttribute(newSolution, i);
            swarm.add(newSolution);
        }

        return swarm;
    }

    @Override
    public List<DoubleSolution> evaluateSwarm(List<DoubleSolution> swarm) {
        return this.evaluator.evaluate(swarm, this.problem);
    }

    @Override
    public void initializeLeader(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            this.neighborhoodBest[i] = this.getNeighborBest(i);
        }
    }

    @Override
    public void initializeParticlesMemory(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            this.localBest[i] = (DoubleSolution) swarm.get(i).copy();
        }
    }

    @Override
    public void initializeVelocity(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            DoubleSolution particle = swarm.get(i);
            for (int j = 0; j < this.problem.getNumberOfVariables(); j++) {
                this.speed[i][j] =
                        (this.randomGenerator.nextDouble(particle.getUnboxedLowerBound(j), particle.getUnboxedUpperBound(j))
                                - particle.getUnboxedVariableValue(j)) / 2.0;
            }
        }
    }

    @Override
    public void updateVelocity(List<DoubleSolution> swarm) {
        double r1, r2;

        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);

            r1 = this.randomGenerator.nextDouble(0, this.c);
            r2 = this.randomGenerator.nextDouble(0, this.c);

            if (this.localBest[i] != this.neighborhoodBest[i]) {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    this.speed[i][var] = this.weight * this.speed[i][var] +
                            r1 * (this.localBest[i].getUnboxedVariableValue(var) - particle.getUnboxedVariableValue(var)) +
                            r2 * (this.neighborhoodBest[i].getUnboxedVariableValue(var) - particle.getUnboxedVariableValue
                                    (var));
                }
            } else {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    this.speed[i][var] = this.weight * this.speed[i][var] +
                            r1 * (this.localBest[i].getUnboxedVariableValue(var) -
                                    particle.getUnboxedVariableValue(var));
                }
            }
        }
    }

    @Override
    public void updatePosition(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                particle.setUnboxedVariableValue(var, particle.getUnboxedVariableValue(var) + this.speed[i][var]);

                if (particle.getUnboxedVariableValue(var) < this.problem.getUnboxedLowerBound(var)) {
                    particle.setUnboxedVariableValue(var, this.problem.getUnboxedLowerBound(var));
                    this.speed[i][var] = 0;
                }
                if (particle.getUnboxedVariableValue(var) > this.problem.getUnboxedUpperBound(var)) {
                    particle.setUnboxedVariableValue(var, this.problem.getUnboxedUpperBound(var));
                    this.speed[i][var] = 0;
                }
            }
        }
    }

    @Override
    public void perturbation(List<DoubleSolution> swarm) {
    /*
    MutationOperator<DoubleSolution> mutation =
            new PolynomialMutation(1.0/problem.getNumberOfVariables(), 20.0) ;
    for (DoubleSolution particle : swarm) {
      mutation.execute(particle) ;
    }
    */
    }

    @Override
    public void updateLeaders(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            this.neighborhoodBest[i] = this.getNeighborBest(i);
        }

        DoubleSolution bestSolution = this.findBestSolution.execute(swarm);

        if (this.bestFoundParticle == null) {
            this.bestFoundParticle = (DoubleSolution) bestSolution.copy();
        } else {
            if (bestSolution.getObjective(this.objectiveId) == this.bestFoundParticle.getObjective(0)) {
                this.neighborhood.recompute();
            }
            if (bestSolution.getObjective(this.objectiveId) < this.bestFoundParticle.getObjective(0)) {
                this.bestFoundParticle = (DoubleSolution) bestSolution.copy();
            }
        }
    }

    @Override
    public void updateParticlesMemory(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            if ((swarm.get(i).getObjective(this.objectiveId) < this.localBest[i].getObjective(0))) {
                this.localBest[i] = (DoubleSolution) swarm.get(i).copy();
            }
        }
    }

    @Override
    public DoubleSolution getResult() {
        return this.bestFoundParticle;
    }

    private DoubleSolution getNeighborBest(int i) {
        DoubleSolution bestLocalBestSolution = null;

        for (DoubleSolution solution : this.neighborhood.getNeighbors(this.getSwarm(), i)) {
            int solutionPositionInSwarm = this.positionInSwarm.getAttribute(solution);
            if ((bestLocalBestSolution == null) || (bestLocalBestSolution.getObjective(0)
                    > this.localBest[solutionPositionInSwarm].getObjective(0))) {
                bestLocalBestSolution = this.localBest[solutionPositionInSwarm];
            }
        }

        return bestLocalBestSolution;
    }

    /* Getters */
    public double[][] getSwarmSpeedMatrix() {
        return this.speed;
    }

    public DoubleSolution[] getLocalBest() {
        return this.localBest;
    }

    @Override
    public String getName() {
        return "SPSO07";
    }

    @Override
    public String getDescription() {
        return "Standard PSO 2007";
    }
}