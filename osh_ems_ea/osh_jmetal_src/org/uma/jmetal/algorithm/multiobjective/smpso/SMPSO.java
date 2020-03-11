package org.uma.jmetal.algorithm.multiobjective.smpso;

import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the SMPSO algorithm described in:
 * SMPSO: A new PSO-based metaheuristic for multi-objective optimization
 * MCDM 2009. DOI: http://dx.doi.org/10.1109/MCDM.2009.4938830
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMPSO extends AbstractParticleSwarmOptimization<DoubleSolution, List<DoubleSolution>> {
    protected final DoubleProblem problem;

    private final double c1Max;
    private final double c1Min;
    private final double c2Max;
    private final double c2Min;
    private final double r1Max;
    private final double r1Min;
    private final double r2Max;
    private final double r2Min;
    private final double weightMax;
    private final double weightMin;
    private final double changeVelocity1;
    private final double changeVelocity2;

    private final int swarmSize;
    private final int maxIterations;
    private final GenericSolutionAttribute<DoubleSolution, DoubleSolution> localBest;
    private final double[][] speed;
    private final JMetalRandom randomGenerator;
    private final BoundedArchive<DoubleSolution> leaders;
    private final Comparator<DoubleSolution> dominanceComparator;
    private final MutationOperator<DoubleSolution> mutation;
    private final double[] deltaMax;
    private final double[] deltaMin;
    private final SolutionListEvaluator<DoubleSolution> evaluator;
    private int iterations;

    /**
     * Constructor
     */
    public SMPSO(DoubleProblem problem, int swarmSize, BoundedArchive<DoubleSolution> leaders,
                 MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min, double r1Max,
                 double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
                 double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                 SolutionListEvaluator<DoubleSolution> evaluator) {
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.leaders = leaders;
        this.mutation = mutationOperator;
        this.maxIterations = maxIterations;

        this.r1Max = r1Max;
        this.r1Min = r1Min;
        this.r2Max = r2Max;
        this.r2Min = r2Min;
        this.c1Max = c1Max;
        this.c1Min = c1Min;
        this.c2Max = c2Max;
        this.c2Min = c2Min;
        this.weightMax = weightMax;
        this.weightMin = weightMin;
        this.changeVelocity1 = changeVelocity1;
        this.changeVelocity2 = changeVelocity2;

        this.randomGenerator = JMetalRandom.getInstance();
        this.evaluator = evaluator;

        this.dominanceComparator = new DominanceComparator<>();
        this.localBest = new GenericSolutionAttribute<>();
        this.speed = new double[swarmSize][problem.getNumberOfVariables()];

        this.deltaMax = new double[problem.getNumberOfVariables()];
        this.deltaMin = new double[problem.getNumberOfVariables()];
        for (int i = 0; i < problem.getNumberOfVariables(); i++) {
            this.deltaMax[i] = (problem.getUpperBound(i) - problem.getLowerBound(i)) / 2.0;
            this.deltaMin[i] = -this.deltaMax[i];
        }
    }

    protected void updateLeadersDensityEstimator() {
        this.leaders.computeDensityEstimator();
    }

    @Override
    protected void initProgress() {
        this.iterations = 1;
        this.updateLeadersDensityEstimator();
    }

    @Override
    protected void updateProgress() {
        this.iterations += 1;
        this.updateLeadersDensityEstimator();
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, this.iterations, -1, this.leaders.getSolutionList())) {
                return true;
            }
        }
        return false;
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
    protected void initializeLeader(List<DoubleSolution> swarm) {
        for (DoubleSolution particle : swarm) {
            this.leaders.add(particle);
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

    @Override
    protected void initializeParticlesMemory(List<DoubleSolution> swarm) {
        for (DoubleSolution particle : swarm) {
            this.localBest.setAttribute(particle, (DoubleSolution) particle.copy());
        }
    }

    @Override
    protected void updateVelocity(List<DoubleSolution> swarm) {
        double r1, r2, c1, c2;
        double wmax, wmin;
        DoubleSolution bestGlobal;

        for (int i = 0; i < swarm.size(); i++) {
            DoubleSolution particle = (DoubleSolution) swarm.get(i).copy();
            DoubleSolution bestParticle = (DoubleSolution) this.localBest.getAttribute(swarm.get(i)).copy();

            bestGlobal = this.selectGlobalBest();

            r1 = this.randomGenerator.nextDouble(this.r1Min, this.r1Max);
            r2 = this.randomGenerator.nextDouble(this.r2Min, this.r2Max);
            c1 = this.randomGenerator.nextDouble(this.c1Min, this.c1Max);
            c2 = this.randomGenerator.nextDouble(this.c2Min, this.c2Max);
            wmax = this.weightMax;
            wmin = this.weightMin;

            for (int var = 0; var < particle.getNumberOfVariables(); var++) {
                this.speed[i][var] = this.velocityConstriction(this.constrictionCoefficient(c1, c2) * (
                                this.inertiaWeight(this.iterations, this.maxIterations, wmax, wmin) * this.speed[i][var] +
                                        c1 * r1 * (bestParticle.getUnboxedVariableValue(var) - particle.getUnboxedVariableValue(var)) +
                                        c2 * r2 * (bestGlobal.getUnboxedVariableValue(var) - particle.getUnboxedVariableValue(var))),
                        this.deltaMax, this.deltaMin, var);
            }
        }
    }

    @Override
    protected void updatePosition(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int j = 0; j < particle.getNumberOfVariables(); j++) {
                particle.setUnboxedVariableValue(j, particle.getUnboxedVariableValue(j) + this.speed[i][j]);

                if (particle.getUnboxedVariableValue(j) < this.problem.getLowerBound(j)) {
                    particle.setUnboxedVariableValue(j, this.problem.getLowerBound(j));
                    this.speed[i][j] *= this.changeVelocity1;
                }
                if (particle.getUnboxedVariableValue(j) > this.problem.getUpperBound(j)) {
                    particle.setUnboxedVariableValue(j, this.problem.getUpperBound(j));
                    this.speed[i][j] *= this.changeVelocity2;
                }
            }
        }
    }

    @Override
    protected void perturbation(List<DoubleSolution> swarm) {
        for (int i = 0; i < swarm.size(); i++) {
            if ((i % 6) == 0) {
                this.mutation.execute(swarm.get(i));
            }
        }
    }

    @Override
    protected void updateLeaders(List<DoubleSolution> swarm) {
        for (DoubleSolution particle : swarm) {
            this.leaders.add((DoubleSolution) particle.copy());
        }
    }

    @Override
    protected void updateParticlesMemory(List<DoubleSolution> swarm) {
        for (DoubleSolution doubleSolution : swarm) {
            int flag = this.dominanceComparator.compare(doubleSolution, this.localBest.getAttribute(doubleSolution));
            if (flag <= 0) {
                DoubleSolution particle = (DoubleSolution) doubleSolution.copy();
                this.localBest.setAttribute(doubleSolution, particle);
            }
        }
    }

    @Override
    public List<DoubleSolution> getResult() {
        return this.leaders.getSolutionList();
    }

    protected DoubleSolution selectGlobalBest() {
        DoubleSolution one, two;
        DoubleSolution bestGlobal;
        int pos1 = this.randomGenerator.nextInt(0, this.leaders.getSolutionList().size() - 1);
        int pos2 = this.randomGenerator.nextInt(0, this.leaders.getSolutionList().size() - 1);
        one = this.leaders.getSolutionList().get(pos1);
        two = this.leaders.getSolutionList().get(pos2);

        if (this.leaders.getComparator().compare(one, two) < 1) {
            bestGlobal = (DoubleSolution) one.copy();
        } else {
            bestGlobal = (DoubleSolution) two.copy();
        }

        return bestGlobal;
    }

    private double velocityConstriction(double v, double[] deltaMax, double[] deltaMin,
                                        int variableIndex) {
        double result;

        double dmax = deltaMax[variableIndex];
        double dmin = deltaMin[variableIndex];

        result = Math.min(v, dmax);

        if (v < dmin) {
            result = dmin;
        }

        return result;
    }

    protected double constrictionCoefficient(double c1, double c2) {
        double rho = c1 + c2;
        if (rho <= 4) {
            return 1.0;
        } else {
            return 2 / (2 - rho - Math.sqrt(Math.pow(rho, 2.0) - 4.0 * rho));
        }
    }

    private double inertiaWeight(int iter, int miter, double wma, double wmin) {
        return wma;
    }

    @Override
    public String getName() {
        return "SMPSO";
    }

    @Override
    public String getDescription() {
        return "Speed contrained Multiobjective PSO";
    }

    /* Getters */
    public int getSwarmSize() {
        return this.swarmSize;
    }

    public int getMaxIterations() {
        return this.maxIterations;
    }

    public int getIterations() {
        return this.iterations;
    }

    /* Setters */
    public void setIterations(int iterations) {
        this.iterations = iterations;
    }
}
