package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.Util;
import org.uma.jmetal.operator.impl.crossover.CrossoverType;
import org.uma.jmetal.operator.impl.mutation.MutationType;
import org.uma.jmetal.operator.impl.selection.SelectionType;
import osh.configuration.oc.*;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants;
import osh.utils.string.ParameterConstants.ALPHABET;
import osh.utils.string.ParameterConstants.EA_ALGORITHM;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Generation class for the {@link EAConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class EAConfig {

    //ea logging configuration
    public static boolean logObjectiveBest;
    public static boolean logQualityIndicators;
    public static boolean logParetoFront;
    public static boolean logRanking;

    public static final QualityIndicator[] qualityIndicatorsToLog = {
            QualityIndicator.IGD_PLUS,
            QualityIndicator.HV
    };

    //solution Ranking
    public static final RankingType solutionRanking = RankingType.OBJECTIVE;
    public static final EAObjectives rankingObjective = EAObjectives.MONEY;
    public static final Map<EAObjectives, Double> objectiveWeights = new EnumMap<>(EAObjectives.class);
    static {
        objectiveWeights.put(EAObjectives.MONEY, 5.0);
        objectiveWeights.put(EAObjectives.SELF_SUFFICIENCY_RATIO, 1.0);
        objectiveWeights.put(EAObjectives.SELF_CONSUMPTION_RATIO, 1.0);
    }

    //algorithms
    public static final AlgorithmType[] algorithmsToUse = {
//            AlgorithmType.G_GA,
//            AlgorithmType.SS_GA,
//            AlgorithmType.DE,
//            AlgorithmType.PSO,
            AlgorithmType.ELITIST_ES,
            AlgorithmType.CMAES,
            AlgorithmType.CRO,
    };

    public static final CrossoverType defaultRealCrossoverOperator = CrossoverType.SBX;
    public static final MutationType defaultRealMutationOperator = MutationType.POLYNOMIAL_AUTO;

    public static final CrossoverType defaultBinaryCrossoverOperator = CrossoverType.BINARY_N_POINT;
    public static final MutationType defaultBinaryMutationOperator = MutationType.BIT_FLIP_AUTO;

    public static final CrossoverType deCrossoverOperator = CrossoverType.DE;
    public static final SelectionType deSelectionOperator = SelectionType.DE;

    public static final SelectionType defaultSelectionOperator = SelectionType.BINARY_TOURNAMENT;

    public static final Map<AlgorithmType, List<OperatorConfiguration>> algorithmSpecificOperators =
            new EnumMap<>(AlgorithmType.class);
    public static final Set<AlgorithmType> enforcedRealAlgorithms = EnumSet.noneOf(AlgorithmType.class);
    public static final Set<AlgorithmType> deAlgorithms = EnumSet.noneOf(AlgorithmType.class);
    public static final Collection<AlgorithmType> regularBinaryAlgorithms;
    public static final Collection<AlgorithmType> regularBinaryOperatorAlgorithms;

    static {
        enforcedRealAlgorithms.add(AlgorithmType.PSO);
        enforcedRealAlgorithms.add(AlgorithmType.CMAES);
        deAlgorithms.add(AlgorithmType.DE);

        regularBinaryAlgorithms = Arrays.stream(AlgorithmType.values()).filter(a -> !enforcedRealAlgorithms.contains(a))
                .collect(Collectors.toList());
        regularBinaryOperatorAlgorithms = regularBinaryAlgorithms.stream().filter(a -> !deAlgorithms.contains(a))
                .collect(Collectors.toList());
    }

    static {
        OperatorConfiguration sel = new OperatorConfiguration();
        OperatorConfiguration selDE = new OperatorConfiguration();
        OperatorConfiguration crossBin = new OperatorConfiguration();
        OperatorConfiguration crossReal = new OperatorConfiguration();
        OperatorConfiguration crossDE = new OperatorConfiguration();
        OperatorConfiguration mutBin = new OperatorConfiguration();
        OperatorConfiguration mutReal = new OperatorConfiguration();

        sel.setType(osh.configuration.oc.OperatorType.SELECTION);
        selDE.setType(osh.configuration.oc.OperatorType.SELECTION);
        sel.setName(defaultSelectionOperator.getName());
        selDE.setName(deSelectionOperator.getName());

        crossBin.setType(osh.configuration.oc.OperatorType.RECOMBINATION);
        crossReal.setType(osh.configuration.oc.OperatorType.RECOMBINATION);
        crossDE.setType(osh.configuration.oc.OperatorType.RECOMBINATION);
        crossBin.setName(defaultBinaryCrossoverOperator.getName());
        crossReal.setName(defaultRealCrossoverOperator.getName());
        crossDE.setName(deCrossoverOperator.getName());

        mutBin.setType(osh.configuration.oc.OperatorType.MUTATION);
        mutReal.setType(osh.configuration.oc.OperatorType.MUTATION);
        mutBin.setName(defaultBinaryMutationOperator.getName());
        mutReal.setName(defaultRealMutationOperator.getName());

        regularBinaryOperatorAlgorithms.forEach(algorithm -> {
                    List<OperatorConfiguration> opList = new ArrayList<>();
                    opList.add(sel);
                    opList.add(crossBin);
                    opList.add(mutBin);
                    algorithmSpecificOperators.put(algorithm, opList);
                }
        );

        deAlgorithms.forEach(algorithm -> {
                    List<OperatorConfiguration> opList = new ArrayList<>();
                    opList.add(selDE);
                    opList.add(crossDE);
                    opList.add(mutReal);
                    algorithmSpecificOperators.put(algorithm, opList);
                }
        );

        enforcedRealAlgorithms.forEach(algorithm -> {
            List<OperatorConfiguration> opList = new ArrayList<>();
            opList.add(sel);
            opList.add(crossReal);
            opList.add(mutReal);
            algorithmSpecificOperators.put(algorithm, opList);
        });
    }

    //TODO: Allow deviations from the default encoding type (if possible)
    public static final Map<AlgorithmType, VariableEncoding> encodingMap = new EnumMap<>(AlgorithmType.class);

    static {
        regularBinaryAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.BINARY));
        enforcedRealAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.REAL));
        deAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.REAL));
    }

    // OptimizationAlgorithm specific variables
    public static final int numEvaluations = 20000;
    public static final int popSize = 100;

    public static double crossoverProbability = 0.99;
    public static final int crossoverPoints = 2;
    public static final int crossoverDistributionIndex = 20;


    public static final double mutationProbability = 0.001; // BitFlipMutation
    public static double autoProbMutationFactor = 9.0; // BitFlipAutoProbMutation
    public static final int mutationDistributionIndex = 20;

    //DE
    public static final String deVariant = "rand/1/bin";
    public static final double de_cr = 0.5;
    public static final double de_f = 0.5;
    public static final double de_k = 0.5;

    //CMAES
    public static final double sigma = 0.3;

    //CRO
    public static final double rho = 0.7;
    public static final double fbs = 0.9;
    public static final double fa = 0.1;
    public static final double pd = 0.1;
    public static final int attemptsToSettle = 2;

    //stopping Rules
    //should never be false
    public static final boolean useMaxEvaluations = true;

    //how many generations the minDeltaFitness can be violated before stopping
    public static final int maxGenerationsViolations = 20;

    public static final boolean useMinDeltaFitness = true;
//	static boolean useMinDeltaFitness = false;

    //min. perc. amount of fitness change required
    public static final double minDeltaFitnessPercent = 5.0E-15;

    public static final EAObjectives[] eaObjectives = {
            EAObjectives.MONEY,
//            EAObjectives.SELF_SUFFICIENCY_RATIO,
//            EAObjectives.SELF_CONSUMPTION_RATIO
    };

    public static final boolean executeAlgorithmsParallel = true;

    private static List<ConfigurationParameter> generateOperatorParameters(OperatorType type, String operatorName) {
        List<ConfigurationParameter> list = new ArrayList<>();

        if (type == OperatorType.SELECTION) {
            //Nothing for now
        } else if (type == OperatorType.RECOMBINATION) {
            list.add(Util.generateClassedParameter(ParameterConstants.EA.probability, crossoverProbability));
            if (operatorName.equals(CrossoverType.BINARY_N_POINT.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.points, crossoverPoints));
            } else if (operatorName.equals(CrossoverType.SBX.getName()) || operatorName.equals(CrossoverType.INTEGER_SBX.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA.distributionIndex,
                        crossoverDistributionIndex));
            } else if (operatorName.equals(CrossoverType.DE.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.cr, de_cr));
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.f, de_f));
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.k, de_k));
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.variant, deVariant));
            }
        } else {
            list.add(Util.generateClassedParameter(ParameterConstants.EA.probability, mutationProbability));
            if (operatorName.equals(MutationType.BIT_FLIP_AUTO.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA_MUTATION.autoProbMutationFactor, autoProbMutationFactor));
            } else if (operatorName.equals(MutationType.POLYNOMIAL.getName())
                    || operatorName.equals(MutationType.POLYNOMIAL_APPROX.getName())
                    || operatorName.equals(MutationType.POLYNOMIAL_AUTO.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA.distributionIndex, mutationDistributionIndex));
                if (operatorName.equals(MutationType.POLYNOMIAL_AUTO.getName())) {
                    list.add(Util.generateClassedParameter(ParameterConstants.EA_MUTATION.autoProbMutationFactor, autoProbMutationFactor));
                }
            }
        }

        return list;
    }

    private static List<ConfigurationParameter> generateAlgorithmParameters(AlgorithmType type) {
        List<ConfigurationParameter> list = new ArrayList<>();

        list.add(Util.generateClassedParameter(ParameterConstants.EA.populationSize, popSize));

        if (type == AlgorithmType.DE) {
            list.add(Util.generateClassedParameter(ParameterConstants.EA_ALGORITHM.deF, de_f));
            list.add(Util.generateClassedParameter(ParameterConstants.EA_ALGORITHM.deVariant, deVariant));
        }
        if (type == AlgorithmType.ELITIST_ES) {
            list.add(Util.generateClassedParameter(ALPHABET.lambda, popSize));
        }
        if (type == AlgorithmType.CMAES) {
            list.add(Util.generateClassedParameter(ALPHABET.sigma, sigma));
        }
        if (type == AlgorithmType.CRO) {
            double sqr = Math.sqrt(popSize);
            if (sqr - Math.floor(sqr) == 0) {
                list.add(Util.generateClassedParameter(EA_ALGORITHM.grid_n, (int) sqr));
                list.add(Util.generateClassedParameter(EA_ALGORITHM.grid_m, (int) sqr));
            } else {
                list.add(Util.generateClassedParameter(EA_ALGORITHM.grid_n, 1));
                list.add(Util.generateClassedParameter(EA_ALGORITHM.grid_m, popSize));
            }

            list.add(Util.generateClassedParameter(ALPHABET.rho, rho));
            list.add(Util.generateClassedParameter(EA_ALGORITHM.fbs, fbs));
            list.add(Util.generateClassedParameter(EA_ALGORITHM.fa, fa));
            list.add(Util.generateClassedParameter(EA_ALGORITHM.pd, pd));
            list.add(Util.generateClassedParameter(EA_ALGORITHM.attemptsToSettle, attemptsToSettle));
        }

        return list;
    }


    /**
     * Generates the EA configuration.
     *
     * @return the EA configuration
     */
    public static EAConfiguration generateEAConfig() {
        EAConfiguration eaConfig = new EAConfiguration();

        //logging Configuration
        LoggingConfiguration loggingConfiguration = new LoggingConfiguration();
        loggingConfiguration.setLogObjectiveBest(logObjectiveBest);
        loggingConfiguration.setLogQualityIndicators(logQualityIndicators);
        loggingConfiguration.setLogParetoFront(logParetoFront);
        loggingConfiguration.setLogRanking(logRanking);
        Arrays.stream(qualityIndicatorsToLog).forEach(q -> {
            QualityIndicatorConfiguration qic = new QualityIndicatorConfiguration();
            qic.setType(q);
            loggingConfiguration.getQualityIndicators().add(qic);
        });
        eaConfig.setLoggingConfiguration(loggingConfiguration);


        //solution Ranking
        SolutionRanking solRank = new SolutionRanking();
        solRank.setType(solutionRanking);
        if (solutionRanking == RankingType.WEIGHTED_OBJECTIVE || solutionRanking == RankingType.CHEBYSHEV) {
            solRank.getRankingParameters().add(Util.generateClassedParameter(ParameterConstants.EA_MULTI_OBJECTIVE.objectiveWeights,
                    Arrays.toString(Arrays.stream(eaObjectives).mapToDouble(objectiveWeights::get).toArray())));
        } else if (solutionRanking == RankingType.OBJECTIVE) {
            solRank.getRankingParameters().add(Util.generateClassedParameter(ParameterConstants.EA_MULTI_OBJECTIVE.objective,
                    Arrays.binarySearch(eaObjectives, rankingObjective)));
        }
        eaConfig.setSolutionRanking(solRank);

        eaConfig.getEaObjectives().addAll(Arrays.stream(eaObjectives).collect(Collectors.toList()));
        eaConfig.setExecuteAlgorithmsParallel(executeAlgorithmsParallel);

        List<StoppingRuleConfiguration> stoppingRules = new ArrayList<>();

        if (useMaxEvaluations) {
            StoppingRuleConfiguration src = new StoppingRuleConfiguration();
            src.setStoppingRule(StoppingRuleType.MAX_EVALUATIONS);
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.populationSize, popSize));
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.maxEvaluations, numEvaluations));
            stoppingRules.add(src);
        }

        if (useMinDeltaFitness) {
            StoppingRuleConfiguration src = new StoppingRuleConfiguration();
            src.setStoppingRule(StoppingRuleType.DELTA_FITNESS);
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.minDeltaFitnessPercent,
                    minDeltaFitnessPercent));
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated,
                    maxGenerationsViolations));
            stoppingRules.add(src);
        }

        for (AlgorithmType type : algorithmsToUse) {
            AlgorithmConfiguration ac = new AlgorithmConfiguration();
            ac.setAlgorithm(type);
            ac.setVariableEncoding(encodingMap.get(type));
            for (OperatorConfiguration oc : algorithmSpecificOperators.get(type)) {
                OperatorConfiguration ocCopy = new OperatorConfiguration();
                ocCopy.setName(oc.getName());
                ocCopy.setType(oc.getType());

                ocCopy.getOperatorParameters().addAll(generateOperatorParameters(ocCopy.getType(), ocCopy.getName()));
                ac.getOperators().add(ocCopy);
            }
            ac.getStoppingRules().addAll(stoppingRules);
            ac.getAlgorithmParameters().addAll(generateAlgorithmParameters(type));
            eaConfig.getAlgorithms().add(ac);
        }

        return eaConfig;
    }
}
