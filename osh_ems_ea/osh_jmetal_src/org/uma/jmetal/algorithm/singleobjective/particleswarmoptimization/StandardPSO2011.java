package org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization;

import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.operator.Operator;
import org.uma.jmetal.operator.impl.selection.BestSolutionSelection;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.SolutionUtils;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.neighborhood.impl.AdaptiveRandomNeighborhood;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.impl.ExtendedPseudoRandomGenerator;
import org.uma.jmetal.util.pseudorandom.impl.JavaRandomGenerator;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class implementing a Standard PSO 2011 algorithm.
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class StandardPSO2011 extends AbstractParticleSwarmOptimization<DoubleSolution, DoubleSolution> {
    private final DoubleProblem problem;
    private final SolutionListEvaluator<DoubleSolution> evaluator;

    private final Operator<List<DoubleSolution>, DoubleSolution> findBestSolution;
    private final Comparator<DoubleSolution> fitnessComparator;
    private final int swarmSize;
    private final int maxIterations;
    private final int numberOfParticlesToInform;
    private final DoubleSolution[] localBest;
    private final DoubleSolution[] neighborhoodBest;
    private final double[][] speed;
    private final AdaptiveRandomNeighborhood<DoubleSolution> neighborhood;
    private final GenericSolutionAttribute<DoubleSolution, Integer> positionInSwarm;
    private final double weight;
    private final double c;
    private final JMetalRandom randomGenerator;
    private final double changeVelocity;
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
     * @param maxIterations
     * @param numberOfParticlesToInform
     * @param evaluator
     */
    public StandardPSO2011(DoubleProblem problem, int objectiveId, int swarmSize, int maxIterations,
                           int numberOfParticlesToInform, SolutionListEvaluator<DoubleSolution> evaluator) {
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;
        this.numberOfParticlesToInform = numberOfParticlesToInform;
        this.evaluator = evaluator;
        this.objectiveId = objectiveId;

        this.weight = 1.0 / (2.0 * Math.log(2)); //0.721;
        this.c = 1.0 / 2.0 + Math.log(2); //1.193;
        this.changeVelocity = -0.5;

        this.fitnessComparator = new ObjectiveComparator<>(objectiveId);
        this.findBestSolution = new BestSolutionSelection<>(this.fitnessComparator);

        this.localBest = new DoubleSolution[swarmSize];
        this.neighborhoodBest = new DoubleSolution[swarmSize];
        this.speed = new double[swarmSize][problem.getNumberOfVariables()];

        this.positionInSwarm = new GenericSolutionAttribute<>();

        this.randomGenerator = JMetalRandom.getInstance();
        this.randomGenerator.setRandomGenerator(new ExtendedPseudoRandomGenerator(new JavaRandomGenerator()));

        this.bestFoundParticle = null;
        this.neighborhood = new AdaptiveRandomNeighborhood<>(swarmSize, this.numberOfParticlesToInform);
    }

    /**
     * Constructor
     *
     * @param problem
     * @param swarmSize
     * @param maxIterations
     * @param numberOfParticlesToInform
     * @param evaluator
     */
    public StandardPSO2011(DoubleProblem problem, int swarmSize, int maxIterations,
                           int numberOfParticlesToInform, SolutionListEvaluator<DoubleSolution> evaluator) {
        this(problem, 0, swarmSize, maxIterations, numberOfParticlesToInform, evaluator);
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
        return this.iterations >= this.maxIterations;
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
        swarm = this.evaluator.evaluate(swarm, this.problem);

        return swarm;
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
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int j = 0; j < this.problem.getNumberOfVariables(); j++) {
                this.speed[i][j] = (this.randomGenerator.nextDouble(
                        particle.getLowerBound(j) - particle.getVariableValue(0),
                        particle.getUpperBound(j) - particle.getVariableValue(0)));
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

            DoubleSolution gravityCenter = this.problem.createSolution();

            if (this.localBest[i] != this.neighborhoodBest[i]) {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    double G;
                    G = particle.getVariableValue(var) +
                            this.c * (this.localBest[i].getVariableValue(var) +
                                    this.neighborhoodBest[i].getVariableValue(var) - 2 *
                                    particle.getVariableValue(var)) / 3.0;

                    gravityCenter.setVariableValue(var, G);
                }
            } else {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    double g = particle.getVariableValue(var) +
                            this.c * (this.localBest[i].getVariableValue(var) - particle.getVariableValue(var)) / 2.0;

                    gravityCenter.setVariableValue(var, g);
                }
            }

            DoubleSolution randomParticle = this.problem.createSolution();

            double radius = 0;
            radius = SolutionUtils.distanceBetweenSolutionsInObjectiveSpace(gravityCenter, particle);

            double[] random = ((ExtendedPseudoRandomGenerator) this.randomGenerator.getRandomGenerator()).randSphere(this.problem.getNumberOfVariables());

            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                randomParticle.setVariableValue(var, gravityCenter.getVariableValue(var) + radius * random[var]);
            }

            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                this.speed[i][var] =
                        this.weight * this.speed[i][var] + randomParticle.getVariableValue(var) - particle.getVariableValue(var);
            }


            if (this.localBest[i] != this.neighborhoodBest[i]) {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    this.speed[i][var] = this.weight * this.speed[i][var] +
                            r1 * (this.localBest[i].getVariableValue(var) - particle.getVariableValue(var)) +
                            r2 * (this.neighborhoodBest[i].getVariableValue(var) - particle.getVariableValue
                                    (var));
                }
            } else {
                for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                    this.speed[i][var] = this.weight * this.speed[i][var] +
                            r1 * (this.localBest[i].getVariableValue(var) -
                                    particle.getVariableValue(var));
                }
            }
        }
    }

    @Override
    public void updatePosition(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                particle.setVariableValue(var, particle.getVariableValue(var) + this.speed[i][var]);

                if (particle.getVariableValue(var) < this.problem.getLowerBound(var)) {
                    particle.setVariableValue(var, this.problem.getLowerBound(var));
                    this.speed[i][var] = this.changeVelocity * this.speed[i][var];
                }
                if (particle.getVariableValue(var) > this.problem.getUpperBound(var)) {
                    particle.setVariableValue(var, this.problem.getUpperBound(var));
                    this.speed[i][var] = this.changeVelocity * this.speed[i][var];
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
            this.bestFoundParticle = bestSolution;
        } else {
            if (bestSolution.getObjective(this.objectiveId) == this.bestFoundParticle.getObjective(0)) {
                this.neighborhood.recompute();
            }
            if (bestSolution.getObjective(this.objectiveId) < this.bestFoundParticle.getObjective(0)) {
                this.bestFoundParticle = bestSolution;
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
        return "SPSO11";
    }

    @Override
    public String getDescription() {
        return "Standard PSO 2011";
    }
}