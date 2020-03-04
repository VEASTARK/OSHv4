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

package org.uma.jmetal.algorithm.multiobjective.smpso;

import org.uma.jmetal.algorithm.impl.AbstractParticleSwarmOptimization;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.archivewithreferencepoint.ArchiveWithReferencePoint;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.measure.Measurable;
import org.uma.jmetal.util.measure.MeasureManager;
import org.uma.jmetal.util.measure.impl.BasicMeasure;
import org.uma.jmetal.util.measure.impl.CountingMeasure;
import org.uma.jmetal.util.measure.impl.SimpleMeasureManager;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.GenericSolutionAttribute;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the SMPSORP algorithm described in:
 * "Extending the Speed-constrained Multi-Objective PSO (SMPSO) With Reference Point Based Preference
 * Articulation. Antonio J. Nebro, Juan J. Durillo, José García-Nieto, Cristóbal Barba-González,
 * Javier Del Ser, Carlos A. Coello Coello, Antonio Benítez-Hidalgo, José F. Aldana-Montes.
 * Parallel Problem Solving from Nature -- PPSN XV. Lecture Notes In Computer Science, Vol. 11101,
 * pp. 298-310. 2018".
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class SMPSORP
        extends AbstractParticleSwarmOptimization<DoubleSolution, List<DoubleSolution>>
        implements Measurable {
    public final List<ArchiveWithReferencePoint<DoubleSolution>> leaders;
    protected final int swarmSize;
    protected final int maxIterations;
    protected final double[] deltaMax;
    protected final double[] deltaMin;
    protected final SolutionListEvaluator<DoubleSolution> evaluator;
    protected final List<List<Double>> referencePoints;
    protected final CountingMeasure currentIteration;
    protected final SimpleMeasureManager measureManager;
    protected final BasicMeasure<List<DoubleSolution>> solutionListMeasure;
    private final DoubleProblem problem;
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
    private final GenericSolutionAttribute<DoubleSolution, DoubleSolution> localBest;
    private final double[][] speed;
    private final JMetalRandom randomGenerator;
    private final Comparator<DoubleSolution> dominanceComparator;
    private final MutationOperator<DoubleSolution> mutation;
    protected int iterations;
    private List<DoubleSolution> referencePointSolutions;

    /**
     * Constructor
     */
    public SMPSORP(DoubleProblem problem, int swarmSize,
                   List<ArchiveWithReferencePoint<DoubleSolution>> leaders,
                   List<List<Double>> referencePoints,
                   MutationOperator<DoubleSolution> mutationOperator, int maxIterations, double r1Min, double r1Max,
                   double r2Min, double r2Max, double c1Min, double c1Max, double c2Min, double c2Max,
                   double weightMin, double weightMax, double changeVelocity1, double changeVelocity2,
                   SolutionListEvaluator<DoubleSolution> evaluator) {
        this.problem = problem;
        this.swarmSize = swarmSize;
        this.leaders = leaders;
        this.mutation = mutationOperator;
        this.maxIterations = maxIterations;
        this.referencePoints = referencePoints;

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

        this.currentIteration = new CountingMeasure(0);
        this.solutionListMeasure = new BasicMeasure<>();

        this.measureManager = new SimpleMeasureManager();
        this.measureManager.setPushMeasure("currentPopulation", this.solutionListMeasure);
        this.measureManager.setPushMeasure("currentIteration", this.currentIteration);


        this.referencePointSolutions = new ArrayList<>();
        for (List<Double> referencePoint : referencePoints) {
            DoubleSolution refPoint = problem.createSolution();
            for (int j = 0; j < referencePoints.get(0).size(); j++) {
                refPoint.setObjective(j, referencePoint.get(j));
            }

            this.referencePointSolutions.add(refPoint);
        }
    }

    protected void updateLeadersDensityEstimator() {
        for (BoundedArchive<DoubleSolution> leader : this.leaders) {
            leader.computeDensityEstimator();
        }
    }

    @Override
    protected void initProgress() {
        this.iterations = 1;
        this.currentIteration.reset(1);
        this.updateLeadersDensityEstimator();
    }

    @Override
    protected void updateProgress() {
        this.iterations += 1;
        this.currentIteration.increment(1);
        this.updateLeadersDensityEstimator();

        this.solutionListMeasure.push(this.getResult());
    }

    @Override
    protected boolean isStoppingConditionReached() {
        return this.iterations >= this.maxIterations;
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
            for (BoundedArchive<DoubleSolution> leader : this.leaders) {
                leader.add((DoubleSolution) particle.copy());
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
                                        c1 * r1 * (bestParticle.getVariableValue(var) - particle.getVariableValue(var)) +
                                        c2 * r2 * (bestGlobal.getVariableValue(var) - particle.getVariableValue(var))),
                        this.deltaMax, this.deltaMin, var);
            }
        }
    }

    @Override
    protected void updatePosition(List<DoubleSolution> swarm) {
        for (int i = 0; i < this.swarmSize; i++) {
            DoubleSolution particle = swarm.get(i);
            for (int j = 0; j < particle.getNumberOfVariables(); j++) {
                particle.setVariableValue(j, particle.getVariableValue(j) + this.speed[i][j]);

                if (particle.getVariableValue(j) < this.problem.getLowerBound(j)) {
                    particle.setVariableValue(j, this.problem.getLowerBound(j));
                    this.speed[i][j] *= this.changeVelocity1;
                }
                if (particle.getVariableValue(j) > this.problem.getUpperBound(j)) {
                    particle.setVariableValue(j, this.problem.getUpperBound(j));
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
            for (BoundedArchive<DoubleSolution> leader : this.leaders) {
                leader.add((DoubleSolution) particle.copy());
            }
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
        List<DoubleSolution> resultList = new ArrayList<>();
        for (BoundedArchive<DoubleSolution> leader : this.leaders) {
            resultList.addAll(leader.getSolutionList());
        }

        return resultList;
    }

    protected DoubleSolution selectGlobalBest() {
        int selectedSwarmIndex;

        selectedSwarmIndex = this.randomGenerator.nextInt(0, this.leaders.size() - 1);
        BoundedArchive<DoubleSolution> selectedSwarm = this.leaders.get(selectedSwarmIndex);

        DoubleSolution one, two;
        DoubleSolution bestGlobal;
        int pos1 = this.randomGenerator.nextInt(0, selectedSwarm.getSolutionList().size() - 1);
        int pos2 = this.randomGenerator.nextInt(0, selectedSwarm.getSolutionList().size() - 1);

        one = selectedSwarm.getSolutionList().get(pos1);
        two = selectedSwarm.getSolutionList().get(pos2);

        if (selectedSwarm.getComparator().compare(one, two) < 1) {
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

    private double constrictionCoefficient(double c1, double c2) {
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
        return "SMPSO/RP";
    }

    @Override
    public String getDescription() {
        return "Speed contrained Multiobjective PSO";
    }

    @Override
    public MeasureManager getMeasureManager() {
        return this.measureManager;
    }

    public void removeDominatedSolutionsInArchives() {
        for (ArchiveWithReferencePoint<DoubleSolution> archive : this.leaders) {
            int i = 0;
            while (i < archive.getSolutionList().size()) {
                boolean dominated = false;
                for (DoubleSolution referencePoint : this.referencePointSolutions) {
                    if (this.dominanceComparator.compare(archive.getSolutionList().get(i), referencePoint) == 0) {
                        dominated = true;
                    }
                }

                if (dominated) {
                    archive.getSolutionList().remove(i);
                } else {
                    i++;
                }
            }
        }
    }

    public synchronized void changeReferencePoints(List<List<Double>> referencePoints) {
        for (int i = 0; i < this.leaders.size(); i++) {
            this.leaders.get(i).changeReferencePoint(referencePoints.get(i));
        }
    }

    public List<DoubleSolution> getReferencePointSolutions() {
        return this.referencePointSolutions;
    }

    public void setReferencePointSolutions(List<DoubleSolution> referencePointSolutions) {
        this.referencePointSolutions = referencePointSolutions;
    }
}
