package osh.mgmt.globalcontroller.jmetal.builder;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.singleobjective.differentialevolution.DifferentialEvolution;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.OSHLegacyGenerationalGeneticAlgorithm;
import org.uma.jmetal.algorithm.singleobjective.geneticalgorithm.SteadyStateGeneticAlgorithm;
import org.uma.jmetal.algorithm.singleobjective.particleswarmoptimization.StandardPSO2011;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRuleFactory;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.operator.SelectionOperator;
import org.uma.jmetal.operator.impl.crossover.CrossoverFactory;
import org.uma.jmetal.operator.impl.crossover.CrossoverType;
import org.uma.jmetal.operator.impl.crossover.DifferentialEvolutionCrossover;
import org.uma.jmetal.operator.impl.mutation.MutationFactory;
import org.uma.jmetal.operator.impl.mutation.MutationType;
import org.uma.jmetal.operator.impl.selection.DifferentialEvolutionSelection;
import org.uma.jmetal.operator.impl.selection.SelectionFactory;
import org.uma.jmetal.operator.impl.selection.SelectionType;
import org.uma.jmetal.problem.DoubleProblem;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.BinarySolution;
import org.uma.jmetal.solution.DoubleSolution;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.evaluator.SolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.MultithreadedStealingSolutionListEvaluator;
import org.uma.jmetal.util.evaluator.impl.SequentialSolutionListEvaluator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.pseudorandom.impl.OSHPseudoRandom;
import org.uma.jmetal.util.pseudorandom.impl.ParallelOSHPseudoRandom;
import osh.configuration.oc.*;
import osh.core.OSHRandom;
import osh.core.exceptions.OCManagerException;
import osh.core.logging.IGlobalLogger;
import osh.mgmt.globalcontroller.jmetal.esc.BinaryEnergyManagementProblem;
import osh.mgmt.globalcontroller.jmetal.esc.EMProblemEvaluator;
import osh.mgmt.globalcontroller.jmetal.esc.RealEnergyManagementProblem;
import osh.mgmt.globalcontroller.jmetal.esc.SolutionDistributor;
import osh.mgmt.globalcontroller.jmetal.logging.EALogger;
import osh.mgmt.globalcontroller.jmetal.logging.IEALogger;
import osh.mgmt.globalcontroller.jmetal.logging.ParallelEALogger;
import osh.mgmt.globalcontroller.jmetal.solution.AlgorithmSolutionCollection;
import osh.utils.map.MapUtils;
import osh.utils.string.ParameterConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Represents an executor class that runs all configured algorithms on the optimization problem and returns their
 * results bundled in a specific wrapper.
 *
 * @author Sebastian Kramer
 */
public class AlgorithmExecutor {

    private final IGlobalLogger logger;
    private IEALogger eaLogger;
    private final List<EAObjectives> objectives;

    private final List<AlgorithmConfigurationWrapper> algorithms = new ArrayList<>();
    private final boolean useParallelAlgorithms;

    /**
     * Constructs this executor around the given ea configuration and a logger.
     *
     * @param eaConfiguration the configuration of the algoriothms
     * @param globalLogger the global logger
     */
    public AlgorithmExecutor(EAConfiguration eaConfiguration, IGlobalLogger globalLogger) {
        this.objectives = eaConfiguration.getEaObjectives();
        this.logger = globalLogger;

        eaConfiguration.getAlgorithms().forEach(a -> this.algorithms.add(new AlgorithmConfigurationWrapper(a)));

        this.useParallelAlgorithms = eaConfiguration.isExecuteAlgorithmsParallel() && (eaConfiguration.getAlgorithms().size()
                > 1);

        //just to be sure, if a valid randomGen isn't set this will explode
        JMetalRandom.getInstance().setRandomGenerator(null);
        this.buildLogger(globalLogger);
    }

    private void buildLogger(IGlobalLogger globalLogger) {
        if (!this.useParallelAlgorithms) {
            this.eaLogger = new EALogger(
                    globalLogger,
                    true,
                    true,
                    10,
                    20,
                    true,
                    this.objectives);
        } else {
            this.eaLogger = new ParallelEALogger(
                    globalLogger,
                    true,
                    true,
                    10,
                    20,
                    true,
                    this.objectives);
        }
    }

    /**
     * Returns the specific ea-logger.
     *
     * @return the ea-logger
     */
    public IEALogger getEaLogger() {
        return this.eaLogger;
    }

    /**
     * Updates the random generator used by the jMetal algorithms.
     *
     * @param randomGenerator the new random generator to update on
     */
    public void updateRandomGenerator(OSHRandom randomGenerator) {
        if (this.useParallelAlgorithms) {
            OSHRandom[] randomGenerators = new OSHRandom[this.algorithms.size()];
            for (int i = 0; i < this.algorithms.size(); i++) {
                randomGenerators[i] = new OSHRandom(randomGenerator.getNextLong());
            }

            if (JMetalRandom.getInstance().getRandomGenerator() == null || !JMetalRandom.getInstance()
                    .getRandomGenerator().getClass().equals(ParallelOSHPseudoRandom.class)) {
                JMetalRandom.getInstance().setRandomGenerator(new ParallelOSHPseudoRandom(randomGenerators));
            }
            ((ParallelOSHPseudoRandom) JMetalRandom.getInstance().getRandomGenerator()).setRandomGenerators(randomGenerators);
        } else {
            JMetalRandom.getInstance().setRandomGenerator(new OSHPseudoRandom(randomGenerator));
        }
    }

    /**
     * Executes all configured algorithms on the given problem and returns the bundled results.
     *
     * @param distributor the solution distributor for the problem
     * @param problemEvaluator the evaluator for the problem
     *
     * @return the bundled results of all algorithm executions
     *
     * @throws OCManagerException if there was an error during the construction of the jMetal elements
     * @throws InterruptedException if there was an error during the multi-threaded execution of the algorithms
     */
    @SuppressWarnings("unchecked")
    public AlgorithmSolutionCollection runAlgorithms(SolutionDistributor distributor,
                                                     EMProblemEvaluator problemEvaluator) throws OCManagerException, InterruptedException {

        AlgorithmSolutionCollection resultCollection = new AlgorithmSolutionCollection();
        Problem<BinarySolution> binaryProblem;
        Problem<DoubleSolution> doubleProblem;

        binaryProblem = new BinaryEnergyManagementProblem(
                problemEvaluator,
                this.objectives,
                distributor);
        doubleProblem = new RealEnergyManagementProblem(
                problemEvaluator,
                this.objectives,
                distributor);

        CountDownLatch latch = new CountDownLatch(this.algorithms.size());

        for (int i = 0; i < this.algorithms.size(); i++) {

            AlgorithmConfigurationWrapper algorithmConfig = this.algorithms.get(i);

            Problem<? extends Solution<?>> problem =
                    algorithmConfig.getVariableEncoding() == VariableEncoding.BINARY ? binaryProblem : doubleProblem;

            if (distributor.getVariableInformation(algorithmConfig.getVariableEncoding()).needsNoVariables()) {
                resultCollection.addSolution(algorithmConfig.getAlgorithm(), problem.createSolution());

                if (this.useParallelAlgorithms) {
                    latch.countDown();
                }

                continue;
            }

            Algorithm<?> algorithm = this.buildAlgorithm(problem, algorithmConfig, problemEvaluator);

            if (this.useParallelAlgorithms) {
                Thread runner = new ParallelAlgorithmRunner(algorithm, (ParallelEALogger) this.eaLogger,
                        resultCollection, this.logger, algorithmConfig.getAlgorithm(), latch, i);
                runner.start();
            } else {
                algorithm.run();

                Object o = algorithm.getResult();

                if (o instanceof List) {
                    resultCollection.addSolutionCollection(algorithmConfig.getAlgorithm(), (List<Solution<?>>) o);
                } else {
                    resultCollection.addSolution(algorithmConfig.getAlgorithm(), (Solution<?>) o);
                }
            }
        }

        if (this.useParallelAlgorithms) {
            latch.await();
            ((ParallelEALogger) this.eaLogger).flush();
        }

        return resultCollection;
    }

    @SuppressWarnings("unchecked")
    private <S extends Solution<?>> Algorithm<?> buildAlgorithm(Problem<S> problem,
                                                                AlgorithmConfigurationWrapper algorithmConfig,
                                                                EMProblemEvaluator problemEvaluator) throws OCManagerException {

        SelectionOperator<?, ?> selectionOperator = null;
        CrossoverOperator<S> crossoverOperator = null;
        MutationOperator<S> mutationOperator = null;

        try {
            if (algorithmConfig.getOperatorMap().containsKey(OperatorType.SELECTION)) {
                selectionOperator = SelectionFactory.<S>getSelectionOperator(
                        SelectionType.fromName(algorithmConfig.getOperatorMap().get(OperatorType.SELECTION).getName()),
                        MapUtils.mapFromCPCollectionUnpacked(algorithmConfig.getOperatorMap().get(OperatorType.SELECTION).getOperatorParameters()));
            }
            if (algorithmConfig.getOperatorMap().containsKey(OperatorType.RECOMBINATION)) {
                crossoverOperator = CrossoverFactory.getCrossoverOperator(
                        CrossoverType.fromName(algorithmConfig.getOperatorMap().get(OperatorType.RECOMBINATION).getName()),
                        MapUtils.mapFromCPCollectionUnpacked(algorithmConfig.getOperatorMap().get(OperatorType.RECOMBINATION).getOperatorParameters()));
            }
            if (algorithmConfig.getOperatorMap().containsKey(OperatorType.MUTATION)) {
                mutationOperator = MutationFactory.getMutationOperator(
                        MutationType.fromName(algorithmConfig.getOperatorMap().get(OperatorType.MUTATION).getName()),
                        MapUtils.mapFromCPCollectionUnpacked(algorithmConfig.getOperatorMap().get(OperatorType.MUTATION).getOperatorParameters()));
            }
        } catch (JMetalException e) {
            throw new OCManagerException(e);
        }

        SolutionListEvaluator<S> evaluator;
        if (!algorithmConfig.getAlgorithmParameterMap().containsKey(ParameterConstants.EA_ALGORITHM.singleThreaded)) {
            evaluator = new MultithreadedStealingSolutionListEvaluator<>();
            problemEvaluator.initializeMultithreading();
        } else {
            evaluator = new SequentialSolutionListEvaluator<>();
        }

        int populationSize = (int) algorithmConfig.getAlgorithmParameterMap().get(ParameterConstants.EA.populationSize);

        Algorithm<?> algorithm;


        //SO
        if (algorithmConfig.getAlgorithm() == AlgorithmType.G_GA) {
            algorithm = new OSHLegacyGenerationalGeneticAlgorithm<>(problem, populationSize, crossoverOperator,
                    mutationOperator, (SelectionOperator<List<S>, S>) selectionOperator, evaluator, this.eaLogger);
        } else if (algorithmConfig.getAlgorithm() == AlgorithmType.SS_GA) {
            algorithm = new SteadyStateGeneticAlgorithm<>(problem, populationSize, crossoverOperator,
                    mutationOperator, (SelectionOperator<List<S>, S>) selectionOperator, this.eaLogger);
        } else if (algorithmConfig.getAlgorithm() == AlgorithmType.DE) {
            algorithm = new DifferentialEvolution((DoubleProblem) problem, populationSize, (DifferentialEvolutionCrossover) crossoverOperator,
                    (DifferentialEvolutionSelection) selectionOperator, (SolutionListEvaluator<DoubleSolution>) evaluator, this.eaLogger);
        } else if (algorithmConfig.getAlgorithm() == AlgorithmType.PSO) {
            //particlesToInform must be smaller then the populationSize
            int particlesToInform = populationSize / 2;
            if (algorithmConfig.getAlgorithmParameterMap().containsKey(ParameterConstants.EA_ALGORITHM.particlesToInform)) {
                particlesToInform = (int) algorithmConfig.getAlgorithmParameterMap().get(ParameterConstants.EA_ALGORITHM.particlesToInform);
            } else {
                this.logger.logDebug("Parameter particlesToInform not given for PSO algorithm, using default: " + particlesToInform);
            }

            algorithm = new StandardPSO2011((DoubleProblem) problem, populationSize, particlesToInform,
                    (SolutionListEvaluator<DoubleSolution>) evaluator, this.eaLogger);
        } else {
            throw new IllegalArgumentException("Algorithm not implemented");
        }

        for (Map.Entry<StoppingRuleType, StoppingRuleConfiguration> en : algorithmConfig.getStoppingRuleMap().entrySet()) {
            Map<String, Object> params = MapUtils.mapFromCPCollectionUnpacked(en.getValue().getRuleParameters());

            StoppingRule sr = StoppingRuleFactory.getStoppingRule(en.getKey(), params);
            algorithm.addStoppingRule(sr);
        }

        return algorithm;
    }
}
