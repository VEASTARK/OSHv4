package org.uma.jmetal.algorithm.multiobjective.cellde;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.util.archive.BoundedArchive;
import org.uma.jmetal.util.comparator.CrowdingDistanceComparator;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.neighborhood.Neighborhood;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.CrowdingDistance;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.util.solutionattribute.impl.LocationAttribute;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public class CellDE45 implements Algorithm<List<DoubleSolution>> {
    private final Problem<DoubleSolution> problem;
    private final int populationSize;
    private final Neighborhood<DoubleSolution> neighborhood;
    private final SelectionOperator<List<DoubleSolution>, DoubleSolution> selection;
    private final DifferentialEvolutionCrossover crossover;
    private final BoundedArchive<DoubleSolution> archive;
    private final Comparator<DoubleSolution> dominanceComparator;
    private final SolutionListEvaluator<DoubleSolution> evaluator;
    private final double feedback;
    private final CrowdingDistanceComparator<DoubleSolution> comparator = new CrowdingDistanceComparator<>();
    private final CrowdingDistance<DoubleSolution> distance = new CrowdingDistance<>();
    protected int evaluations;
    private List<DoubleSolution> population;
    private int currentIndividual;
    private List<DoubleSolution> currentNeighbors;
    private LocationAttribute<DoubleSolution> location;

    private final List<StoppingRule> stoppingRules = new ArrayList<>();
    private IEALogger eaLogger;

    public CellDE45(Problem<DoubleSolution> problem,
                    int populationSize,
                    BoundedArchive<DoubleSolution> archive,
                    Neighborhood<DoubleSolution> neighborhood,
                    SelectionOperator<List<DoubleSolution>, DoubleSolution> selection,
                    DifferentialEvolutionCrossover crossover,
                    double feedback,
                    SolutionListEvaluator<DoubleSolution> evaluator,
                    IEALogger eaLogger) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.archive = archive;
        this.neighborhood = neighborhood;
        this.selection = selection;
        this.crossover = crossover;
        this.dominanceComparator = new DominanceComparator<>();
        this.feedback = feedback;

        this.evaluator = evaluator;

        this.eaLogger = eaLogger;
        this.eaLogger.logStart(this);
    }

    @Override
    public void run() {
        this.population = this.createInitialPopulation();
        this.population = this.evaluatePopulation(this.population);
        this.initProgress();

        while (!this.isStoppingConditionReached()) {
            for (int i = 0; i < this.populationSize; i++) {
                DoubleSolution solution = (DoubleSolution) this.population.get(i).copy();

                this.currentNeighbors = this.neighborhood.getNeighbors(this.population, i);
                this.currentNeighbors.add(this.population.get(i));

                List<DoubleSolution> parents = new ArrayList<>();
                parents.add(this.selection.execute(this.currentNeighbors));
                parents.add(this.selection.execute(this.currentNeighbors));
                parents.add(solution);

                this.crossover.setCurrentSolution(this.population.get(i));
                List<DoubleSolution> children = this.crossover.execute(parents);

                DoubleSolution offspring = children.get(0);
                this.problem.evaluate(offspring);
                this.updateProgress();

                int result = this.dominanceComparator.compare(this.population.get(i), offspring);
                if (result > 0) {
                    this.location.setAttribute(offspring, this.location.getAttribute(this.population.get(i)));
                    this.population.set(i, (DoubleSolution) offspring.copy());
                    this.archive.add((DoubleSolution) offspring.copy());
                } else if (result == 0) {
                    Ranking<DoubleSolution> ranking = this.computeRanking(this.currentNeighbors);

                    this.distance.computeDensityEstimator(ranking.getSubfront(0));
                    boolean deleteMutant = true;
                    int compareResult = this.comparator.compare(solution, offspring);

                    if (compareResult > 0) {
                        deleteMutant = false;
                    }

                    if (!deleteMutant) {
                        this.location.setAttribute(offspring, this.location.getAttribute(solution));
                        this.population.set(this.location.getAttribute(offspring), offspring);
                    }
                    this.archive.add((DoubleSolution) offspring.copy());
                }
            }

            for (int i = 0; i < this.feedback; i++) {
                if (this.archive.size() > i) {
                    int random = JMetalRandom.getInstance().nextInt(0, this.population.size() - 1);
                    if (random < this.population.size()) {
                        DoubleSolution solution = this.archive.get(i);
                        this.location.setAttribute(solution, random);
                        this.population.set(random, (DoubleSolution) solution.copy());
                    }
                }
            }
        }

    }

    protected List<DoubleSolution> createInitialPopulation() {
        List<DoubleSolution> population = new ArrayList<>(this.populationSize);
        for (int i = 0; i < this.populationSize; i++) {
            DoubleSolution newIndividual = this.problem.createSolution();
            population.add(newIndividual);
        }
        this.location = new LocationAttribute<>(population);
        return population;
    }

    protected List<DoubleSolution> evaluatePopulation(List<DoubleSolution> population) {
        return this.evaluator.evaluate(population, this.problem);
    }

    protected void initProgress() {
        this.evaluations = this.populationSize;
        this.currentIndividual = 0;
        this.eaLogger.logPopulation(this.archive.getSolutionList(), this.evaluations / this.populationSize);
    }

    protected void updateProgress() {
        this.evaluations++;
        this.currentIndividual = (this.currentIndividual + 1) % this.populationSize;
        this.eaLogger.logPopulation(this.archive.getSolutionList(), this.evaluations / this.populationSize);
    }

    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.stoppingRules) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.archive.getSolutionList())) {
                this.eaLogger.logAdditional(sr.getMsg());
                return true;
            }
        }
        return false;
    }


    @Override
    public String getName() {
        return "CellDE";
    }

    @Override
    public String getDescription() {
        return "Multi-Objective Differential Evolution Cellular evolutionary algorithm";
    }

    @Override
    public List<DoubleSolution> getResult() {
        return this.archive.getSolutionList();
    }

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    @Override
    public void setEALogger(IEALogger eaLogger) {
        this.eaLogger = eaLogger;
    }

    @Override
    public IEALogger getEALogger() {
        return this.eaLogger;
    }

    protected Ranking<DoubleSolution> computeRanking(List<DoubleSolution> solutionList) {
        Ranking<DoubleSolution> ranking = new DominanceRanking<>();
        ranking.computeRanking(solutionList);

        return ranking;
    }
}
