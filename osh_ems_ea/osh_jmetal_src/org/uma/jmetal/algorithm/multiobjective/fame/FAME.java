package org.uma.jmetal.algorithm.multiobjective.fame;

import com.fuzzylite.Engine;
import com.fuzzylite.rule.Rule;
import com.fuzzylite.rule.RuleBlock;
import com.fuzzylite.term.Triangle;
import com.fuzzylite.variable.InputVariable;
import com.fuzzylite.variable.OutputVariable;
import org.uma.jmetal.algorithm.multiobjective.nsgaii.SteadyStateNSGAII;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.operator.Operator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.crossover.SBXCrossover;
import org.uma.jmetal.operator.impl.mutation.PolynomialMutation;
import org.uma.jmetal.operator.impl.mutation.UniformMutation;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.SolutionListUtils;
import org.uma.jmetal.util.archive.impl.SpatialSpreadDeviationArchive;
import org.uma.jmetal.util.comparator.DominanceComparator;
import org.uma.jmetal.util.comparator.SpatialSpreadDeviationComparator;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;
import org.uma.jmetal.util.solutionattribute.impl.SpatialSpreadDeviation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * This class implements the FAME algorithm described in: A. Santiago, B. Dorronsoro, A.J. Nebro,
 * J.J. Durillo, O. Castillo, H.J. Fraire A Novel Multi-Objective Evolutionary Algorithm with Fuzzy
 * Logic Based Adaptive Selection of Operators: FAME. Information Sciences. Volume 471, January
 * 2019, Pages 233-251. DOI: https://doi.org/10.1016/j.ins.2018.09.005
 *
 * @author Alejandro Santiago <aurelio.santiago@upalt.edu.mx>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class FAME<S extends Solution<?>> extends SteadyStateNSGAII<S> {

    private static final long serialVersionUID = -1698229077365963205L;
    private final double[] Utilization;
    private final double[] OpProb;
    private final int operators = 4;
    private final int windowSize;
    private final SpatialSpreadDeviationArchive archiveSSD;
    Engine engine;
    InputVariable operatoruse, stagnation;
    OutputVariable probability;
    private int window;
    private double Stagnation;

    /**
     * Constructor
     */
    public FAME(
            Problem<S> problem,
            int populationSize,
            int archiveSize,
            SelectionOperator<List<S>, S> selectionOperator,
            SolutionListEvaluator<S> evaluator) {
        super(
                problem,
                populationSize,
                null,
                null,
                selectionOperator,
                new DominanceComparator<>(),
                evaluator);
        this.archiveSSD = new SpatialSpreadDeviationArchive(archiveSize);
        this.OpProb = new double[this.operators];
        this.Utilization = new double[this.operators];
        this.windowSize = (int) Math.ceil(3.33333 * this.operators);
        for (int x = 0; x < this.operators; x++) {
            this.OpProb[x] = (1.0);
            this.Utilization[x] = 0.0;
        }
        this.loadFIS();
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
    }

    private void loadFIS() {
        this.engine = new Engine();
        this.engine.setName("Probabilides-operadores");
        this.stagnation = new InputVariable();
        this.stagnation.setName("Stagnation");
        this.stagnation.setRange(0.0, 1.0);
        this.stagnation.addTerm(new Triangle("LOW", -0.4, 0.0, 0.4));
        this.stagnation.addTerm(new Triangle("MID", 0.1, 0.5, 0.9));
        this.stagnation.addTerm(new Triangle("HIGH", 0.6, 1.0, 1.4));
        this.engine.addInputVariable(this.stagnation);

        this.operatoruse = new InputVariable();
        this.operatoruse.setName("OperatorUse");
        this.operatoruse.setRange(0.0, 1.0);
        this.operatoruse.addTerm(new Triangle("LOW", -0.4, 0.0, 0.4));
        this.operatoruse.addTerm(new Triangle("MID", 0.1, 0.5, 0.9));
        this.operatoruse.addTerm(new Triangle("HIGH", 0.6, 1.0, 1.4));
        this.engine.addInputVariable(this.operatoruse);

        this.probability = new OutputVariable();
        this.probability.setName("Probability");
        this.probability.setRange(0.0, 1.0);
        this.probability.addTerm(
                new Triangle(
                        "LOW", -0.4, 0.0,
                        0.4)); // probability.addTerm(new Triangle("LOW", 0.000, 0.250, 0.500));
        this.probability.addTerm(
                new Triangle(
                        "MID", 0.1, 0.5,
                        0.9)); // probability.addTerm(new Triangle("MID", 0.250, 0.500, 0.750));
        this.probability.addTerm(
                new Triangle(
                        "HIGH", 0.6, 1.0,
                        1.4)); // probability.addTerm(new Triangle("HIGH", 0.500, 0.750, 1.000));
        this.engine.addOutputVariable(this.probability);

        RuleBlock ruleBlock = new RuleBlock();
        ruleBlock.addRule(
                Rule.parse(
                        "if Stagnation is HIGH and OperatorUse is HIGH then Probability is MID", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is HIGH and OperatorUse is LOW then Probability is MID", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is HIGH and OperatorUse is MID then Probability is LOW", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is MID and OperatorUse is HIGH then Probability is MID", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is MID and OperatorUse is LOW then Probability is MID", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is MID and OperatorUse is MID then Probability is LOW", this.engine));
        ruleBlock.addRule(
                Rule.parse(
                        "if Stagnation is LOW and OperatorUse is HIGH then Probability is HIGH", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is LOW and OperatorUse is LOW then Probability is LOW", this.engine));
        ruleBlock.addRule(
                Rule.parse("if Stagnation is LOW and OperatorUse is MID then Probability is MID", this.engine));
        this.engine.addRuleBlock(ruleBlock);

        this.engine.configure("Minimum", "Maximum", "Minimum", "Maximum", "Centroid");

        StringBuilder status = new StringBuilder();
        if (!this.engine.isReady(status)) {
            throw new RuntimeException(
                    "Engine not ready. " + "The following errors were encountered:\n" + status);
        }
    }

    @Override
    protected void updateProgress() {
        this.evaluations++;
        if (this.window == this.windowSize) {
            for (int x = 0; x < this.operators; x++) {
                this.engine.setInputValue("Stagnation", this.Stagnation);
                this.engine.setInputValue("OperatorUse", this.Utilization[x]);
                this.engine.process();

                this.OpProb[x] = this.probability.getOutputValue();
                this.Utilization[x] = 0.0;
            }
            this.window = 0;
            this.Stagnation = 0.0;
        }
    }

    @Override
    protected boolean isStoppingConditionReached() {
        for (StoppingRule sr : this.getStoppingRules()) {
            if (sr.checkIfStop(this.problem, -1, this.evaluations, this.archiveSSD.getSolutionList())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<S> selection(List<S> population) {
        List<S> matingPopulation = new ArrayList<>(3);
        for (int x = 0; x < 3; x++) {
            double aleat = Math.random();
            if (aleat <= 0.1) // 0.1 n_n wiiii (0.1 lo hace parecido a measo1 (0.05 asco))
                matingPopulation.add(this.selectionOperator.execute(population));
            else {
                matingPopulation.add((S) this.selectionOperator.execute(this.archiveSSD.getSolutionList()));
            }
        }

        return matingPopulation;
    }

    @Override
    protected List<S> reproduction(List<S> population) {
        List<S> offspringPopulation = new ArrayList<>(1);
        List<S> parents;

        double probabilityPolynomial, DristributionIndex;
        probabilityPolynomial = 0.30;
        DristributionIndex = 20;
        Operator mutationPolynomial = new PolynomialMutation(probabilityPolynomial, DristributionIndex);

        double probabilityUniform, perturbation;
        probabilityUniform = 0.30;
        perturbation = 0.1;
        Operator mutationUniform = new UniformMutation(probabilityUniform, perturbation);

        double CR, F;
        CR = 1.0;
        F = 0.5;
        DifferentialEvolutionCrossover crossoverOperator_DE =
                new DifferentialEvolutionCrossover(CR, F, "rand/1/bin");

        double crossoverProbability, crossoverDistributionIndex;
        crossoverProbability = 1.0;
        crossoverDistributionIndex = 20.0;
        Operator crossoverOperator_SBX =
                new SBXCrossover(crossoverProbability, crossoverDistributionIndex);

        Random rnd = new Random();
        int operator = rnd.nextInt(this.operators);
        List<S> offspring = new ArrayList<>(1);
        // ROULETTE
        double cont = 0;
        for (int x = 0; x < this.operators; x++) {
            cont += this.OpProb[x];
        }
        while (cont > 0) {
            cont -= this.OpProb[operator];
            if (cont <= 0) {
                break;
            } else {
                operator++;
                if (operator == this.OpProb.length) operator = 0;
            }
        }
        // seleccion del operator
        // operador=2;
        boolean flag_dep;
        switch (operator) {
            // DIFERENTIAL EVOLUTION
            case 0:
                parents = new ArrayList<>(3);
                parents.add(population.get(0));
                parents.add(population.get(1));
                parents.add(population.get(2));
                DoubleSolution solution = (DoubleSolution) population.get(2).copy();
                crossoverOperator_DE.setCurrentSolution(solution);
                offspring = (List<S>) crossoverOperator_DE.execute((List<DoubleSolution>) parents);
                this.evaluator.evaluate(offspring, this.getProblem());
                offspringPopulation.add(offspring.get(0));
                this.Utilization[0] += 1.0 / this.windowSize;
                flag_dep = this.archiveSSD.add(offspring.get(0));
                if (!flag_dep) {
                    this.Stagnation += 1.0 / this.windowSize;
                }
                break;
            // SBX Crossover
            case 1:
                parents = new ArrayList<>(2);
                parents.add(population.get(0));
                parents.add(population.get(1));
                offspring = (List<S>) crossoverOperator_SBX.execute(parents);
                this.evaluator.evaluate(offspring, this.getProblem());
                offspringPopulation.add(offspring.get(0));
                this.Utilization[1] += 1.0 / this.windowSize;
                flag_dep = this.archiveSSD.add(offspring.get(0));
                if (!flag_dep) {
                    this.Stagnation += 1.0 / this.windowSize;
                }
                break;
            // Polynomial mutation
            case 2:
                parents = new ArrayList<>(1);
                parents.add(population.get(0));
                offspring.add((S) population.get(0).copy());
                mutationPolynomial.execute(offspring.get(0));
                this.evaluator.evaluate(offspring, this.getProblem());
                offspringPopulation.add(offspring.get(0));
                this.Utilization[2] += 1.0 / this.windowSize;
                flag_dep = this.archiveSSD.add(offspring.get(0));
                if (!flag_dep) {
                    this.Stagnation += 1.0 / this.windowSize;
                }
                break;
            // UNIFORM MUTATION
            case 3:
                parents = new ArrayList<>(1);
                parents.add(population.get(0));
                offspring.add((S) population.get(0).copy());
                mutationUniform.execute(offspring.get(0));
                this.evaluator.evaluate(offspring, this.getProblem());
                offspringPopulation.add(offspring.get(0));
                this.Utilization[3] += 1.0 / this.windowSize;
                flag_dep = this.archiveSSD.add(offspring.get(0));
                if (!flag_dep) {
                    this.Stagnation += 1.0 / this.windowSize;
                }
                break;
            default:
                break;
        }
        this.window++;
        return offspringPopulation;
    }

    @Override
    public String getName() {
        return "FAME";
    }

    @Override
    public String getDescription() {
        return "FAME ultima version";
    }

    @Override
    protected List<S> createInitialPopulation() {
        SpatialSpreadDeviation distancia = new SpatialSpreadDeviation();
        List<S> population = new ArrayList<>(this.getMaxPopulationSize());
        for (int i = 0; i < this.getMaxPopulationSize(); i++) {
            S newIndividual = this.getProblem().createSolution();
            distancia.setAttribute(newIndividual, 0.0);
            population.add(newIndividual);
        }
        this.evaluator.evaluate(population, this.getProblem());
        for (int i = 0; i < this.getMaxPopulationSize(); i++) {
            this.archiveSSD.add(population.get(i));
        }
        return population;
    }

    @Override
    public List<S> getResult() {
        return SolutionListUtils.getNondominatedSolutions(this.archiveSSD.getSolutionList());
    }

    @Override
    protected List<S> replacement(List<S> population, List<S> offspringPopulation) {
        List<S> jointPopulation = new ArrayList<>();
        jointPopulation.addAll(population);
        jointPopulation.addAll(offspringPopulation);
        Ranking<S> ranking = new DominanceRanking<>();
        ranking.computeRanking(jointPopulation);

        return this.fast_nondonimated_sort(ranking);
    }

    protected List<S> fast_nondonimated_sort(Ranking<S> ranking) {
        SpatialSpreadDeviation<S> SSD = new SpatialSpreadDeviation<>();
        List<S> population = new ArrayList<>(this.getMaxPopulationSize());
        int rankingIndex = 0;
        while (this.populationIsNotFull(population)) {
            if (this.subfrontFillsIntoThePopulation(ranking, rankingIndex, population)) {
                this.addRankedSolutionsToPopulation(ranking, rankingIndex, population);
                rankingIndex++;
            } else {
                SSD.computeDensityEstimator(ranking.getSubfront(rankingIndex));
                this.addLastRankedSolutionsToPopulation(ranking, rankingIndex, population);
            }
        }

        return population;
    }

    protected boolean populationIsNotFull(List<S> population) {
        return population.size() < this.getMaxPopulationSize();
    }

    protected boolean subfrontFillsIntoThePopulation(
            Ranking<S> ranking, int rank, List<S> population) {
        return ranking.getSubfront(rank).size() < (this.getMaxPopulationSize() - population.size());
    }

    protected void addRankedSolutionsToPopulation(Ranking<S> ranking, int rank, List<S> population) {
        List<S> front;

        front = ranking.getSubfront(rank);

        population.addAll(front);
    }

    protected void addLastRankedSolutionsToPopulation(
            Ranking<S> ranking, int rank, List<S> population) {
        List<S> currentRankedFront = ranking.getSubfront(rank);

        currentRankedFront.sort(new SpatialSpreadDeviationComparator<>());

        int i = 0;
        while (population.size() < this.getMaxPopulationSize()) {
            population.add(currentRankedFront.get(i));
            i++;
        }
    }
}
