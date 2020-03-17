package org.uma.jmetal.algorithm.multiobjective.abyss;

import org.uma.jmetal.algorithm.impl.AbstractScatterSearch;
import org.uma.jmetal.algorithm.multiobjective.abyss.util.MarkAttribute;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.LocalSearchOperator;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.SolutionUtils;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.archive.impl.CrowdingDistanceArchive;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.EqualSolutionsComparator;
import org.uma.jmetal.util.comparator.StrengthFitnessComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.impl.DistanceToSolutionListAttribute;
import org.uma.jmetal.util.solutionattribute.impl.StrengthRawFitness;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import javax.management.JMException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * This class implements the AbYSS algorithm, a multiobjective scatter search metaheuristics,
 * which is described in:
 * A.J. Nebro, F. Luna, E. Alba, B. Dorronsoro, J.J. Durillo, A. Beham
 * "AbYSS: Adapting Scatter Search to Multiobjective Optimization." IEEE Transactions on
 * Evolutionary Computation. Vol. 12, No. 4 (August 2008), pp. 439-457
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 * @author Cristobal Barba
 */
@SuppressWarnings("serial")
public class ABYSS extends AbstractScatterSearch<DoubleSolution, List<DoubleSolution>> {
    protected final Problem<DoubleSolution> problem;

    protected final int referenceSet1Size;
    protected final int referenceSet2Size;
    protected final List<DoubleSolution> referenceSet1;
    protected final List<DoubleSolution> referenceSet2;

    protected final int archiveSize;
    protected final Archive<DoubleSolution> archive;

    protected final LocalSearchOperator<DoubleSolution> localSearch;
    protected final CrossoverOperator<DoubleSolution> crossover;
    protected final JMetalRandom randomGenerator;
    /**
     * These variables are used in the diversification method.
     */
    protected final int numberOfSubRanges;
    protected final int[] sumOfFrequencyValues;
    protected final int[] sumOfReverseFrequencyValues;
    protected final int[][] frequency;
    protected final int[][] reverseFrequency;
    protected final StrengthRawFitness<DoubleSolution> strengthRawFitness; //TODO: invert this dependency
    protected final Comparator<DoubleSolution> fitnessComparator; //TODO: invert this dependency
    protected final MarkAttribute marked;
    protected final DistanceToSolutionListAttribute distanceToSolutionListAttribute;
    protected final Comparator<DoubleSolution> dominanceComparator;
    protected final Comparator<DoubleSolution> equalComparator;
    protected final Comparator<DoubleSolution> crowdingDistanceComparator;
    protected int evaluations;

    public ABYSS(DoubleProblem problem, int populationSize, int referenceSet1Size,
                 int referenceSet2Size, int archiveSize, Archive<DoubleSolution> archive,
                 LocalSearchOperator<DoubleSolution> localSearch,
                 CrossoverOperator<DoubleSolution> crossoverOperator,
                 int numberOfSubRanges, IEALogger eaLogger) {

        this.setPopulationSize(populationSize);

        this.problem = problem;
        this.referenceSet1Size = referenceSet1Size;
        this.referenceSet2Size = referenceSet2Size;
        this.archiveSize = archiveSize;
        this.archive = archive;
        this.localSearch = localSearch;
        this.crossover = crossoverOperator;

        this.referenceSet1 = new ArrayList<>(referenceSet1Size);
        this.referenceSet2 = new ArrayList<>(referenceSet2Size);

        this.numberOfSubRanges = numberOfSubRanges;

        this.randomGenerator = JMetalRandom.getInstance();

        this.sumOfFrequencyValues = new int[problem.getNumberOfVariables()];
        this.sumOfReverseFrequencyValues = new int[problem.getNumberOfVariables()];
        this.frequency = new int[numberOfSubRanges][problem.getNumberOfVariables()];
        this.reverseFrequency = new int[numberOfSubRanges][problem.getNumberOfVariables()];

        this.strengthRawFitness = new StrengthRawFitness<>();
        this.fitnessComparator = new StrengthFitnessComparator<>();
        this.marked = new MarkAttribute();
        this.distanceToSolutionListAttribute = new DistanceToSolutionListAttribute();
        this.crowdingDistanceComparator = new CrowdingDistanceComparator<>();

        this.dominanceComparator = new DominanceComparator<>();
        this.equalComparator = new EqualSolutionsComparator<>();

        this.evaluations = 0;

        this.setEALogger(eaLogger);
        this.getEALogger().logStart(this);
    }

    @Override
    public boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.archive.getSolutionList())) {
                this.getEALogger().logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }

    @Override
    public DoubleSolution improvement(DoubleSolution solution) {
        DoubleSolution improvedSolution = this.localSearch.execute(solution);
        this.evaluations += this.localSearch.getEvaluations();

        return improvedSolution;
    }

    @Override
    public List<DoubleSolution> getResult() {
        return this.archive.getSolutionList();
    }

    @Override
    public DoubleSolution diversificationGeneration() {
        DoubleSolution solution = this.problem.createSolution();

        double value;
        int range;

        for (int i = 0; i < this.problem.getNumberOfVariables(); i++) {
            this.sumOfReverseFrequencyValues[i] = 0;
            for (int j = 0; j < this.numberOfSubRanges; j++) {
                this.reverseFrequency[j][i] = this.sumOfFrequencyValues[i] - this.frequency[j][i];
                this.sumOfReverseFrequencyValues[i] += this.reverseFrequency[j][i];
            }

            if (this.sumOfReverseFrequencyValues[i] == 0) {
                range = this.randomGenerator.nextInt(0, this.numberOfSubRanges - 1);
            } else {
                value = this.randomGenerator.nextInt(0, this.sumOfReverseFrequencyValues[i] - 1);
                range = 0;
                while (value > this.reverseFrequency[range][i]) {
                    value -= this.reverseFrequency[range][i];
                    range++;
                }
            }

            this.frequency[range][i]++;
            this.sumOfFrequencyValues[i]++;

            double low = ((DoubleProblem) this.problem).getUnboxedLowerBound(i) + range *
                    (((DoubleProblem) this.problem).getUnboxedUpperBound(i) -
                            ((DoubleProblem) this.problem).getUnboxedLowerBound(i)) / this.numberOfSubRanges;
            double high = low + (((DoubleProblem) this.problem).getUnboxedUpperBound(i) -
                    ((DoubleProblem) this.problem).getUnboxedLowerBound(i)) / this.numberOfSubRanges;

            value = this.randomGenerator.nextDouble(low, high);
            solution.setUnboxedVariableValue(i, value);
        }

        this.problem.evaluate(solution);
        this.evaluations++;
        return solution;
    }

    /**
     * Build the reference set after the initialization phase
     */
    @Override
    public void referenceSetUpdate() {
        this.buildNewReferenceSet1();
        this.buildNewReferenceSet2();
        this.getEALogger().logPopulation(this.archive.getSolutionList(), this.evaluations / this.getPopulationSize());
    }

    /**
     * Update the reference set with a new solution
     *
     * @param solution
     */
    @Override
    public void referenceSetUpdate(DoubleSolution solution) {
        if (this.refSet1Test(solution)) {
            for (DoubleSolution solutionInRefSet2 : this.referenceSet2) {
                double aux = SolutionUtils.distanceBetweenSolutionsInObjectiveSpace(solution, solutionInRefSet2);
                if (aux < this.distanceToSolutionListAttribute.getAttribute(solutionInRefSet2)) {
                    this.distanceToSolutionListAttribute.setAttribute(solutionInRefSet2, aux);
                }
            }
        } else {
            this.refSet2Test(solution);
        }
        this.getEALogger().logPopulation(this.archive.getSolutionList(), this.evaluations / this.getPopulationSize());
    }

    /**
     * Build the referenceSet1 by moving the best referenceSet1Size individuals, according to
     * a fitness comparator, from the population to the referenceSet1
     */
    public void buildNewReferenceSet1() {
        DoubleSolution individual;
        this.strengthRawFitness.computeDensityEstimator(this.getPopulation());
        this.getPopulation().sort(this.fitnessComparator);

        for (int i = 0; i < this.referenceSet1Size; i++) {
            individual = this.getPopulation().get(0);
            this.getPopulation().remove(0);
            this.marked.setAttribute(individual, false);
            this.referenceSet1.add(individual);
        }
    }

    /**
     * Build the referenceSet2 by moving to it the most diverse referenceSet2Size individuals from the
     * population in respect to the referenceSet1.
     * <p>
     * The size of the referenceSet2 can be lower than referenceSet2Size depending on the current size
     * of the population
     */
    public void buildNewReferenceSet2() {
        for (int i = 0; i < this.getPopulation().size(); i++) {
            DoubleSolution individual = this.getPopulation().get(i);
            double distanceAux = SolutionUtils
                    .distanceToSolutionListInSolutionSpace(individual, this.referenceSet1);
            this.distanceToSolutionListAttribute.setAttribute(individual, distanceAux);
        }

        int size = this.referenceSet2Size;
        if (this.getPopulation().size() < this.referenceSet2Size) {
            size = this.getPopulation().size();
        }

        for (int i = 0; i < size; i++) {
            // Find the maximumMinimumDistanceToPopulation
            double maxMinimum = 0.0;
            int index = 0;
            for (int j = 0; j < this.getPopulation().size(); j++) {

                DoubleSolution auxSolution = this.getPopulation().get(j);
                if (this.distanceToSolutionListAttribute.getAttribute(auxSolution) > maxMinimum) {
                    maxMinimum = this.distanceToSolutionListAttribute.getAttribute(auxSolution);
                    index = j;
                }
            }
            DoubleSolution individual = this.getPopulation().get(index);
            this.getPopulation().remove(index);

            // Update distances to REFSET in population
            for (int j = 0; j < this.getPopulation().size(); j++) {
                double aux = SolutionUtils.distanceBetweenSolutionsInObjectiveSpace(this.getPopulation().get(j), individual);

                if (aux < this.distanceToSolutionListAttribute.getAttribute(individual)) {
                    DoubleSolution auxSolution = this.getPopulation().get(j);
                    this.distanceToSolutionListAttribute.setAttribute(auxSolution, aux);
                }
            }

            // Insert the individual into REFSET2
            this.marked.setAttribute(individual, false);
            this.referenceSet2.add(individual);

            // Update distances in REFSET2
            for (int j = 0; j < this.referenceSet2.size(); j++) {
                for (DoubleSolution doubleSolution : this.referenceSet2) {
                    if (i != j) {
                        double aux = SolutionUtils.distanceBetweenSolutionsInObjectiveSpace(this.referenceSet2.get(j), doubleSolution);
                        DoubleSolution auxSolution = this.referenceSet2.get(j);
                        if (aux < this.distanceToSolutionListAttribute.getAttribute(auxSolution)) {
                            this.distanceToSolutionListAttribute.setAttribute(auxSolution, aux);
                        }
                    }
                }
            }
        }
    }

    /**
     * Tries to update the reference set one with a solution
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     */
    public boolean refSet1Test(DoubleSolution solution) {
        boolean dominated = false;
        int flag;
        int i = 0;
        while (i < this.referenceSet1.size()) {
            flag = this.dominanceComparator.compare(solution, this.referenceSet1.get(i));
            if (flag == -1) { //This is: solution dominates
                this.referenceSet1.remove(i);
            } else if (flag == 1) {
                dominated = true;
                i++;
            } else {
                flag = this.equalComparator.compare(solution, this.referenceSet1.get(i));
                if (flag == 0) {
                    return true;
                } // if
                i++;
            } // if
        } // while

        if (!dominated) {
            this.marked.setAttribute(solution, false);
            if (this.referenceSet1.size() < this.referenceSet1Size) { //refSet1 isn't full
                this.referenceSet1.add(solution);
            } else {
                this.archive.add(solution);
            } // if
        } else {
            return false;
        } // if
        return true;
    }

    /**
     * Try to update the reference set 2 with a <code>Solution</code>
     *
     * @param solution The <code>Solution</code>
     * @return true if the <code>Solution</code> has been inserted, false
     * otherwise.
     * @throws JMException
     */
    public boolean refSet2Test(DoubleSolution solution) {
        if (this.referenceSet2.size() < this.referenceSet2Size) {
            double solutionAux = SolutionUtils.distanceToSolutionListInSolutionSpace(solution, this.referenceSet1);
            this.distanceToSolutionListAttribute.setAttribute(solution, solutionAux);
            double aux = SolutionUtils.distanceToSolutionListInSolutionSpace(solution, this.referenceSet2);
            if (aux < this.distanceToSolutionListAttribute.getAttribute(solution)) {
                this.distanceToSolutionListAttribute.setAttribute(solution, aux);
            }
            this.referenceSet2.add(solution);
            return true;
        }
        double auxDistance = SolutionUtils.distanceToSolutionListInSolutionSpace(solution, this.referenceSet1);
        this.distanceToSolutionListAttribute.setAttribute(solution, auxDistance);
        double aux = SolutionUtils.distanceToSolutionListInSolutionSpace(solution, this.referenceSet2);
        if (aux < this.distanceToSolutionListAttribute.getAttribute(solution)) {
            this.distanceToSolutionListAttribute.setAttribute(solution, aux);
        }
        double worst = 0.0;
        int index = 0;
        for (int i = 0; i < this.referenceSet2.size(); i++) {
            DoubleSolution auxSolution = this.referenceSet2.get(i);
            aux = this.distanceToSolutionListAttribute.getAttribute(auxSolution);
            if (aux > worst) {
                worst = aux;
                index = i;
            }
        }

        double auxDist = this.distanceToSolutionListAttribute.getAttribute(solution);
        if (auxDist < worst) {
            this.referenceSet2.remove(index);
            //Update distances in REFSET2
            for (DoubleSolution doubleSolution : this.referenceSet2) {
                aux = SolutionUtils.distanceBetweenSolutionsInObjectiveSpace(doubleSolution, solution);
                if (aux < this.distanceToSolutionListAttribute.getAttribute(doubleSolution)) {
                    this.distanceToSolutionListAttribute.setAttribute(doubleSolution, aux);
                }
            }
            this.marked.setAttribute(solution, false);
            this.referenceSet2.add(solution);
            return true;
        }
        return false;
    }

    @Override
    public boolean restartConditionIsFulfilled(List<DoubleSolution> combinedSolutions) {
        return combinedSolutions.isEmpty();
    }

    /**
     * Subset generation method
     *
     * @return
     */
    @Override
    public List<List<DoubleSolution>> subsetGeneration() {
        List<List<DoubleSolution>> solutionGroupsList;

        solutionGroupsList = this.generatePairsFromSolutionList(this.referenceSet1);

        solutionGroupsList.addAll(this.generatePairsFromSolutionList(this.referenceSet2));

        return solutionGroupsList;
    }

    /**
     * Generate all pair combinations of the referenceSet1
     */
    public List<List<DoubleSolution>> generatePairsFromSolutionList(List<DoubleSolution> solutionList) {
        List<List<DoubleSolution>> subset = new ArrayList<>();
        for (int i = 0; i < solutionList.size(); i++) {
            DoubleSolution solution1 = solutionList.get(i);
            for (int j = i + 1; j < solutionList.size(); j++) {
                DoubleSolution solution2 = solutionList.get(j);

                if (!this.marked.getAttribute(solution1) ||
                        !this.marked.getAttribute(solution2)) {
                    List<DoubleSolution> pair = new ArrayList<>(2);
                    pair.add(solution1);
                    pair.add(solution2);
                    subset.add(pair);

                    this.marked.setAttribute(solutionList.get(i), true);
                    this.marked.setAttribute(solutionList.get(j), true);
                }
            }
        }

        return subset;
    }

    @Override
    public List<DoubleSolution> solutionCombination(List<List<DoubleSolution>> solutionList) {
        List<DoubleSolution> resultList = new ArrayList<>();
        for (List<DoubleSolution> pair : solutionList) {
            List<DoubleSolution> offspring = this.crossover.execute(pair);

            this.problem.evaluate(offspring.get(0));
            this.problem.evaluate(offspring.get(1));
            this.evaluations += 2;
            resultList.add(offspring.get(0));
            resultList.add(offspring.get(1));
        }

        return resultList;
    }

    @Override
    public void restart() {
        this.getPopulation().clear();
        this.addReferenceSet1ToPopulation();
        this.updatePopulationWithArchive();
        this.fillPopulationWithRandomSolutions();
    }

    private void addReferenceSet1ToPopulation() {
        for (DoubleSolution solution : this.referenceSet1) {
            DoubleSolution improvement = this.improvement(solution);
            this.marked.setAttribute(improvement, false);

            this.getPopulation().add(improvement);
        }
        this.referenceSet1.clear();
        this.referenceSet2.clear();
    }

    private void updatePopulationWithArchive() {
        CrowdingDistanceArchive<DoubleSolution> crowdingArchive;
        crowdingArchive = (CrowdingDistanceArchive<DoubleSolution>) this.archive;
        crowdingArchive.computeDensityEstimator();

        crowdingArchive.getSolutionList().sort(this.crowdingDistanceComparator);

        int insert = this.getPopulationSize() / 2;

        if (insert > crowdingArchive.getSolutionList().size())
            insert = crowdingArchive.getSolutionList().size();

        if (insert > (this.getPopulationSize() - this.getPopulation().size()))
            insert = this.getPopulationSize() - this.getPopulation().size();

        for (int i = 0; i < insert; i++) {
            DoubleSolution solution = (DoubleSolution) crowdingArchive.getSolutionList().get(i).copy();
            this.marked.setAttribute(solution, false);
            this.getPopulation().add(solution);
        }
    }

    private void fillPopulationWithRandomSolutions() {
        while (this.getPopulation().size() < this.getPopulationSize()) {
            DoubleSolution solution = this.diversificationGeneration();

            this.problem.evaluate(solution);
            this.evaluations++;
            solution = this.improvement(solution);

            this.marked.setAttribute(solution, false);
            this.getPopulation().add(solution);
        }
    }

    @Override
    public String getName() {
        return "AbYSS";
    }

    @Override
    public String getDescription() {
        return "Archived based hYbrid Scatter Search Algorithm";
    }
}

