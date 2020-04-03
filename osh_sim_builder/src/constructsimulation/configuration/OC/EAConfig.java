package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.Util;
import org.uma.jmetal.operator.impl.crossover.CrossoverType;
import org.uma.jmetal.operator.impl.mutation.MutationType;
import org.uma.jmetal.operator.impl.selection.SelectionType;
import osh.configuration.oc.*;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants;

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

    public static QualityIndicator[] qualityIndicatorsToLog = {
            QualityIndicator.IGD_PLUS,
            QualityIndicator.HV
    };

    //solution Ranking
    public static RankingType solutionRanking = RankingType.OBJECTIVE;
    public static EAObjectives rankingObjective = EAObjectives.MONEY;
    public static Map<EAObjectives, Double> objectiveWeights = new EnumMap<>(EAObjectives.class);
    static {
        objectiveWeights.put(EAObjectives.MONEY, 5.0);
        objectiveWeights.put(EAObjectives.SELF_SUFFICIENCY_RATIO, 1.0);
        objectiveWeights.put(EAObjectives.SELF_CONSUMPTION_RATIO, 1.0);
    }

    //algorithms
    public static AlgorithmType[] algorithmsToUse = {
            AlgorithmType.G_GA,
            AlgorithmType.SS_GA,
            AlgorithmType.DE,
            AlgorithmType.PSO
    };

    public static CrossoverType defaultRealCrossoverOperator = CrossoverType.SBX;
    public static MutationType defaultRealMutationOperator = MutationType.POLYNOMIAL_AUTO;

    public static CrossoverType defaultBinaryCrossoverOperator = CrossoverType.BINARY_N_POINT;
    public static MutationType defaultBinaryMutationOperator = MutationType.BIT_FLIP_AUTO;

    public static CrossoverType deCrossoverOperator = CrossoverType.DE;
    public static SelectionType deSelectionOperator = SelectionType.DE;

    public static SelectionType defaultSelectionOperator = SelectionType.BINARY_TOURNAMENT;

    public static Map<AlgorithmType, List<OperatorConfiguration>> algorithmSpecificOperators =
            new EnumMap<>(AlgorithmType.class);
    public static Set<AlgorithmType> enforcedRealAlgorithms = EnumSet.noneOf(AlgorithmType.class);
    public static Set<AlgorithmType> deAlgorithms = EnumSet.noneOf(AlgorithmType.class);
    public static Collection<AlgorithmType> regularBinaryAlgorithms;
    public static Collection<AlgorithmType> regularBinaryOperatorAlgorithms;

    static {
        enforcedRealAlgorithms.add(AlgorithmType.PSO);
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

    public static Map<AlgorithmType, VariableEncoding> encodingMap = new EnumMap<>(AlgorithmType.class);

    static {
        regularBinaryAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.BINARY));
        enforcedRealAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.REAL));
        deAlgorithms.forEach(algorithm -> encodingMap.put(algorithm, VariableEncoding.REAL));
    }

    // OptimizationAlgorithm specific variables
    public static int numEvaluations = 20000;
    public static int popSize = 100;

    public static double crossoverProbability = 0.99;
    public static int crossoverPoints = 2;
    public static int crossoverDistributionIndex = 20;


    public static double mutationProbability = 0.001; // BitFlipMutation
    public static double autoProbMutationFactor = 9.0; // BitFlipAutoProbMutation
    public static int mutationDistributionIndex = 20;

    //DE
    public static String deVariant = "rand/1/bin";
    public static double de_cr = 0.5;
    public static double de_f = 0.5;
    public static double de_k = 0.5;

    //stopping Rules
    //should never be false
    public static boolean useMaxEvaluations = true;

    //how many generations the minDeltaFitness can be violated before stopping
    public static int maxGenerationsViolations = 20;

    public static boolean useMinDeltaFitness = true;
//	static boolean useMinDeltaFitness = false;

    //min. perc. amount of fitness change required
    public static double minDeltaFitnessPercent = 5.0E-15;

    public static EAObjectives[] eaObjectives = {
            EAObjectives.MONEY,
//            EAObjectives.SELF_SUFFICIENCY_RATIO,
//            EAObjectives.SELF_CONSUMPTION_RATIO
    };

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
                    Arrays.toString(Arrays.stream(eaObjectives).mapToDouble(e -> objectiveWeights.get(e)).toArray())));
        } else if (solutionRanking == RankingType.OBJECTIVE) {
            solRank.getRankingParameters().add(Util.generateClassedParameter(ParameterConstants.EA_MULTI_OBJECTIVE.objective,
                    Arrays.binarySearch(eaObjectives, rankingObjective)));
        }
        eaConfig.setSolutionRanking(solRank);

        eaConfig.getEaObjectives().addAll(Arrays.stream(eaObjectives).collect(Collectors.toList()));


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
