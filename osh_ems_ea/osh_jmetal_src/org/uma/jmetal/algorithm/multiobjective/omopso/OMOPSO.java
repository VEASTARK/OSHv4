package org.uma.jmetal.algorithm.multiobjective.omopso;

import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.operator.impl.mutation.NonUniformMutation;
import org.uma.jmetal.operator.impl.mutation.UniformMutation;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.archive.impl.NonDominatedSolutionListArchive;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Class implementing the OMOPSO algorithm
 */
@SuppressWarnings("serial")
public class OMOPSO extends AbstractParticleSwarmOptimization<DoubleSolution, List<DoubleSolution>> {

    final SolutionListEvaluator<DoubleSolution> evaluator;
    private final DoubleProblem problem;
    private final int swarmSize;
    private final int archiveSize;
    private final int maxIterations;
    private final DoubleSolution[] localBest;
    private final CrowdingDistanceArchive<DoubleSolution> leaderArchive;
    private final NonDominatedSolutionListArchive<DoubleSolution> epsilonArchive;
    private final double[][] speed;
    private final Comparator<DoubleSolution> dominanceComparator;
    private final Comparator<DoubleSolution> crowdingDistanceComparator;
    private final UniformMutation uniformMutation;
    private final NonUniformMutation nonUniformMutation;
    private final double eta = 0.0075;
    private final JMetalRandom randomGenerator;
    private final CrowdingDistance<DoubleSolution> crowdingDistance;
    private int currentIteration;

    /**
     * Constructor
     */
    public OMOPSO(DoubleProblem problem, SolutionListEvaluator<DoubleSolution> evaluator,
                  int swarmSize, int maxIterations, int archiveSize, UniformMutation uniformMutation,
                  NonUniformMutation nonUniformMutation) {
        this.problem = problem;
        this.evaluator = evaluator;

        this.swarmSize = swarmSize;
        this.maxIterations = maxIterations;
        this.archiveSize = archiveSize;

        this.uniformMutation = uniformMutation;
        this.nonUniformMutation = nonUniformMutation;

        this.localBest = new DoubleSolution[swarmSize];
        this.leaderArchive = new CrowdingDistanceArchive<>(this.archiveSize);
        this.epsilonArchive = new NonDominatedSolutionListArchive<>(new DominanceComparator<>(this.eta));

        this.dominanceComparator = new DominanceComparator<>();
        this.crowdingDistanceComparator = new CrowdingDistanceComparator<>();

        this.speed = new double[swarmSize][problem.getNumberOfVariables()];

        this.randomGenerator = JMetalRandom.getInstance();
        this.crowdingDistance = new CrowdingDistance<>();
    }


    @Override
    protected void initProgress() {
        this.currentIteration = 1;
        this.crowdingDistance.computeDensityEstimator(this.leaderArchive.getSolutionList());
    }

    @Override
    protected void updateProgress() {
        this.currentIteration += 1;
        this.crowdingDistance.computeDensityEstimator(this.leaderArchive.getSolutionList());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.currentIteration >= this.maxIterations;
    }

    @Override
    protected List<DoubleSolution> createInitialSwarm() {
        List<DoubleSolution> swarm = new ArrayList<>(this.swarmSize);

        DoubleSolution newSolution;
        for (int i = 0; i < this.swarmSize; i++) {
            newSolution = this.problem.createSolution();
            swarm.add(newSolution);
        }

        return swarm;
    }

    @Override
    protected List<DoubleSolution> evaluateSwarm(List<DoubleSolution> swarm) {
        swarm = this.evaluator.evaluate(swarm, this.problem);
        return swarm;
    }

    @Override
    public List<DoubleSolution> getResult() {
        //return this.leaderArchive.getSolutionList();
        return this.epsilonArchive.getSolutionList();
    }

    @Override
    protected void initializeLeader(List<DoubleSolution> swarm) {
        for (DoubleSolution solution : swarm) {
            DoubleSolution particle = (DoubleSolution) solution.copy();
            if (this.leaderArchive.add(particle)) {
                this.epsilonArchive.add((DoubleSolution) particle.copy());
            }
        }
    }

    @Override
    protected void initializeParticlesMemory(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
            this.localBest[i] = particle;
        }
    }

    @Override
    protected void updateVelocity(List<DoubleSolution> swarm) {
        double r1, r2, W, C1, C2;
        DoubleSolution bestGlobal;

        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            DoubleSolution bestParticle = this.localBest[i];

            //Select a global localBest for calculate the speed of particle i, bestGlobal
            DoubleSolution one;
            DoubleSolution two;
            int pos1 = this.randomGenerator.nextInt(0, this.leaderArchive.getSolutionList().size() - 1);
            int pos2 = this.randomGenerator.nextInt(0, this.leaderArchive.getSolutionList().size() - 1);
            one = this.leaderArchive.getSolutionList().get(pos1);
            two = this.leaderArchive.getSolutionList().get(pos2);

            if (this.crowdingDistanceComparator.compare(one, two) < 1) {
                bestGlobal = one;
            } else {
                bestGlobal = two;
            }

            //Parameters for velocity equation
            r1 = this.randomGenerator.nextDouble();
            r2 = this.randomGenerator.nextDouble();
            C1 = this.randomGenerator.nextDouble(1.5, 2.0);
            C2 = this.randomGenerator.nextDouble(1.5, 2.0);
            W = this.randomGenerator.nextDouble(0.1, 0.5);
            //

            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                //Computing the velocity of this particle
                this.speed[i][var] = W * this.speed[i][var] + C1 * r1 * (bestParticle.getVariableValue(var) -
                        particle.getVariableValue(var)) +
                        C2 * r2 * (bestGlobal.getVariableValue(var) - particle.getVariableValue(var));
            }
        }
    }

    /**
     * Update the position of each particle
     */
    @Override
    protected void updatePosition(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                particle.setVariableValue(var, particle.getVariableValue(var) + this.speed[i][var]);
                if (particle.getVariableValue(var) < this.problem.getLowerBound(var)) {
                    particle.setVariableValue(var, this.problem.getLowerBound(var));
                    this.speed[i][var] *= -1.0;
                }
                if (particle.getVariableValue(var) > this.problem.getUpperBound(var)) {
                    particle.setVariableValue(var, this.problem.getUpperBound(var));
                    this.speed[i][var] *= -1.0;
                }
            }
        }
    }

    @Override
    protected void updateParticlesMemory(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            int flag = this.dominanceComparator.compare(swarm.get(i), this.localBest[i]);
            if (flag != 1) {
                DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
                this.localBest[i] = particle;
            }
        }
    }

    @Override
    protected void initializeVelocity(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            for (int j = 0; j < this.problem.getNumberOfVariables(); j++) {
                this.speed[i][j] = 0.0;
            }
        }
    }

    /**
     * Apply a mutation operator to all particles in the swarm (perturbation)
     */
    @Override
    protected void perturbation(List<DoubleSolution> swarm) {
        this.nonUniformMutation.setCurrentIteration(this.currentIteration);

        for (int i = 0; i < swarm.size(); i++) {
            if (i % 3 == 0) {
                this.nonUniformMutation.execute(swarm.get(i));
            } else if (i % 3 == 1) {
                this.uniformMutation.execute(swarm.get(i));
            }
        }
    }

    /**
     * Update leaders method
     *
     * @param swarm List of solutions (swarm)
     */
    @Override
    protected void updateLeaders(List<DoubleSolution> swarm) {
        for (DoubleSolution solution : swarm) {
            DoubleSolution particle = (DoubleSolution) solution.copy();
            if (this.leaderArchive.add(particle)) {
                this.epsilonArchive.add((DoubleSolution) particle.copy());
            }
        }
    }

    protected void tearDown() {
        this.evaluator.shutdown();
    }

    @Override
    public String getName() {
        return "OMOPSO";
    }

    @Override
    public String getDescription() {
        return "Optimized MOPSO";
    }

}
