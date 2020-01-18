package constructsimulation.generation.utility;

import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.StoppingRule;
import osh.configuration.system.ConfigurationParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author Sebastian Kramer
 */
public class CreateGAConfiguration {

    public static GAConfiguration createGAConfiguration(int numEvaluations, int popSize, String crossoverOperator,
                                                        String mutationOperator, String selectionOperator, Map<String, ?> crossoverParameters,
                                                        Map<String, ?> mutationParameters, Map<String, ?> selectionParameters, Map<String, Map<String, ?>> stoppingRules) {


        GAConfiguration gaConfiguration = new GAConfiguration();
        gaConfiguration.setNumEvaluations(numEvaluations);
        gaConfiguration.setPopSize(popSize);
        gaConfiguration.setCrossoverOperator(crossoverOperator);
        gaConfiguration.setMutationOperator(mutationOperator);
        gaConfiguration.setSelectionOperator(selectionOperator);
        List<ConfigurationParameter> par = new ArrayList<>();
        for (Entry<String, ?> en : crossoverParameters.entrySet()) {
            ConfigurationParameter cp = new ConfigurationParameter();
            cp.setParameterName(String.valueOf(en.getKey()));
            cp.setParameterType(en.getValue().getClass().getName());
            cp.setParameterValue(String.valueOf(en.getValue()));
            par.add(cp);
        }
        gaConfiguration.getCrossoverParameters().addAll(par);

        par = new ArrayList<>();
        for (Entry<String, ?> en : mutationParameters.entrySet()) {
            ConfigurationParameter cp = new ConfigurationParameter();
            cp.setParameterName(String.valueOf(en.getKey()));
            cp.setParameterType(en.getValue().getClass().getName());
            cp.setParameterValue(String.valueOf(en.getValue()));
            par.add(cp);
        }
        gaConfiguration.getMutationParameters().addAll(par);

        par = new ArrayList<>();
        for (Entry<String, ?> en : selectionParameters.entrySet()) {
            ConfigurationParameter cp = new ConfigurationParameter();
            cp.setParameterName(String.valueOf(en.getKey()));
            cp.setParameterType(en.getValue().getClass().getName());
            cp.setParameterValue(String.valueOf(en.getValue()));
            par.add(cp);
        }
        gaConfiguration.getSelectionParameters().addAll(par);

        for (Entry<String, Map<String, ?>> entry : stoppingRules.entrySet()) {
            par = new ArrayList<>();
            for (Entry<String, ?> en : entry.getValue().entrySet()) {
                ConfigurationParameter cp = new ConfigurationParameter();
                cp.setParameterName(String.valueOf(en.getKey()));
                cp.setParameterType(en.getValue().getClass().getName());
                cp.setParameterValue(String.valueOf(en.getValue()));
                par.add(cp);
            }
            StoppingRule sr = new StoppingRule();
            sr.setStoppingRuleName(entry.getKey());
            sr.getRuleParameters().addAll(par);
            gaConfiguration.getStoppingRules().add(sr);
        }

        return gaConfiguration;
    }
}
