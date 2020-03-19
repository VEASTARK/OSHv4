package constructsimulation.configuration.OC;

import constructsimulation.configuration.general.Util;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRuleType;
import org.uma.jmetal.operator.impl.crossover.CrossoverType;
import org.uma.jmetal.operator.impl.mutation.MutationType;
import org.uma.jmetal.operator.impl.selection.SelectionType;
import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.StoppingRule;
import osh.configuration.system.ConfigurationParameter;
import osh.utils.string.ParameterConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Generation class for the {@link GAConfiguration}.
 *
 * @author Sebastian Kramer
 */
public class EAConfig {

    public static CrossoverType defaultBinaryCrossoverOperator = CrossoverType.BINARY_N_POINT;
    public static MutationType defaultBinaryMutationOperator = MutationType.BIT_FLIP_AUTO; //  adjusts the mutation rate
    // to 1/(numberOfBits)

    public static SelectionType defaultSelectionOperator = SelectionType.BINARY_TOURNAMENT;

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
    public static double minDeltaFitnessPercent = 5.0E-15;

    private static List<ConfigurationParameter> generateOperatorParameters(OperatorType type, String operatorName) {
        List<ConfigurationParameter> list = new ArrayList<>();

        if (type == OperatorType.SELECTION) {
            //Nothing for now
        } else if (type == OperatorType.RECOMBINATION) {
            list.add(Util.generateClassedParameter(ParameterConstants.EA.probability, crossoverProbability));
            if (operatorName.equals(CrossoverType.BINARY_N_POINT.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA_RECOMBINATION.points, crossoverPoints));
            }
        } else {
            list.add(Util.generateClassedParameter(ParameterConstants.EA.probability, mutationProbability));
            if (operatorName.equals(MutationType.BIT_FLIP_AUTO.getName())) {
                list.add(Util.generateClassedParameter(ParameterConstants.EA_MUTATION.autoProbMutationFactor, autoProbMutationFactor));
            }
        }

        return list;
    }

    public static GAConfiguration generateEAConfig() {
        GAConfiguration eaConfig = new GAConfiguration();

        List<StoppingRule> stoppingRules = new ArrayList<>();

        if (useMaxEvaluations) {
            StoppingRule src = new StoppingRule();
            src.setStoppingRuleName(StoppingRuleType.MAX_EVALUATIONS.getName());
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.populationSize, popSize));
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.maxEvaluations, numEvaluations));
            stoppingRules.add(src);
        }

        if (useMinDeltaFitness) {
            StoppingRule src = new StoppingRule();
            src.setStoppingRuleName(StoppingRuleType.DELTA_FITNESS.getName());
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.minDeltaFitnessPercent,
                    minDeltaFitnessPercent));
            src.getRuleParameters().add(Util.generateClassedParameter(ParameterConstants.EA.maxGenerationsDeltaFitnessViolated,
                    maxGenerationsViolations));
            stoppingRules.add(src);
        }

        eaConfig.setNumEvaluations(numEvaluations);
        eaConfig.setPopSize(popSize);
        eaConfig.setSelectionOperator(defaultSelectionOperator.getName());
        eaConfig.setCrossoverOperator(defaultBinaryCrossoverOperator.getName());
        eaConfig.setMutationOperator(defaultBinaryMutationOperator.getName());


        eaConfig.getSelectionParameters().addAll(generateOperatorParameters(OperatorType.SELECTION,
                defaultSelectionOperator.getName()));
        eaConfig.getCrossoverParameters().addAll(generateOperatorParameters(OperatorType.RECOMBINATION,
                defaultBinaryCrossoverOperator.getName()));
        eaConfig.getMutationParameters().addAll(generateOperatorParameters(OperatorType.MUTATION,
                defaultBinaryMutationOperator.getName()));

        eaConfig.getStoppingRules().addAll(stoppingRules);

        return eaConfig;
    }
}
