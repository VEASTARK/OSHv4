package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.Util;
import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.StoppingRule;
import osh.configuration.system.ConfigurationParameter;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the {@link GAConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class EAConfig {

    public static String defaultBinaryCrossoverOperator = "SingleBinaryNPointsCrossover";
    public static String defaultBinaryMutationOperator = "BitFlipAutoProbMutation"; //  adjusts the mutation rate to 1/(numberOfBits)

    public static String defaultSelectionOperator = "BinaryTournament";

    // OptimizationAlgorithm specific variables
    public static int numEvaluations = 20000;
    public static int popSize = 100;

    public static double crossoverProbability = 0.7;
    public static int crossoverPoints = 2;

    public static double mutationProbability = 0.001; // BitFlipMutation
    public static double autoProbMutationFactor = 1.0; // BitFlipAutoProbMutation

    //stopping Rules
    //should never be false
    public static boolean useMaxEvaluations = true;

    //how many generations the minDeltaFitness can be violated before stopping
    public static int maxGenerationsViolations = 20;

    public static boolean useMinDeltaFitness = true;
//	static boolean useMinDeltaFitness = false;

    //min. perc. amount of fitness change required
    public static double minDeltaFitnessPercent = 5.0E-11;

    private static List<ConfigurationParameter> generateOperatorParameters(OperatorType type, String operatorName) {
        List<ConfigurationParameter> list = new ArrayList<>();

        if (type == OperatorType.SELECTION) {
            //Nothing for now
        } else if (type == OperatorType.RECOMBINATION) {
            list.add(Util.generateClassedParameter("probability", crossoverProbability));
            if (operatorName.equals("SingleBinaryNPointsCrossover")) {
                list.add(Util.generateClassedParameter("points", crossoverPoints));
            }
        } else {
            list.add(Util.generateClassedParameter("probability", mutationProbability));
            if (operatorName.equals("BitFlipAutoProbMutation")) {
                list.add(Util.generateClassedParameter("autoProbMutationFactor", autoProbMutationFactor));
            }
        }

        return list;
    }

    public static GAConfiguration generateEAConfig() {
        GAConfiguration eaConfig = new GAConfiguration();

        List<StoppingRule> stoppingRules = new ArrayList<>();

        if (useMaxEvaluations) {
            StoppingRule src = new StoppingRule();
            src.setStoppingRuleName("EvaluationsStoppingRule");
            src.getRuleParameters().add(Util.generateClassedParameter("populationSize", popSize));
            src.getRuleParameters().add(Util.generateClassedParameter("maxEvaluations", numEvaluations));
            stoppingRules.add(src);
        }

        if (useMinDeltaFitness) {
            StoppingRule src = new StoppingRule();
            src.setStoppingRuleName("DeltaFitnessStoppingRule");
            src.getRuleParameters().add(Util.generateClassedParameter("minDeltaFitnessPerc", minDeltaFitnessPercent));
            src.getRuleParameters().add(Util.generateClassedParameter("maxGenerationsDeltaFitnessViolated", maxGenerationsViolations));
            stoppingRules.add(src);
        }

        eaConfig.setNumEvaluations(numEvaluations);
        eaConfig.setPopSize(popSize);
        eaConfig.setSelectionOperator(defaultSelectionOperator);
        eaConfig.setCrossoverOperator(defaultBinaryCrossoverOperator);
        eaConfig.setMutationOperator(defaultBinaryMutationOperator);


        eaConfig.getSelectionParameters().addAll(generateOperatorParameters(OperatorType.SELECTION,
                defaultSelectionOperator));
        eaConfig.getCrossoverParameters().addAll(generateOperatorParameters(OperatorType.RECOMBINATION,
                defaultBinaryCrossoverOperator));
        eaConfig.getMutationParameters().addAll(generateOperatorParameters(OperatorType.MUTATION,
                defaultBinaryMutationOperator));

        eaConfig.getStoppingRules().addAll(stoppingRules);

        return eaConfig;
    }
}
