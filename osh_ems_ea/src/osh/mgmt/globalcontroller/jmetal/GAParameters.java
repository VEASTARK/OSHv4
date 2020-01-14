package osh.mgmt.globalcontroller.jmetal;

import osh.configuration.oc.GAConfiguration;
import osh.configuration.oc.StoppingRule;
import osh.configuration.system.ConfigurationParameter;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

/**
 * INNER CLASS
 *
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class GAParameters implements Cloneable {
    private int numEvaluations;

    private int popSize;

    private String crossoverOperator;
    private String mutationOperator;
    private String selectionOperator;

    private HashMap crossoverParameters;

    private HashMap mutationParameters;

    private HashMap selectionParameters;

    private HashMap<String, HashMap> stoppingRules;

    public GAParameters() {
        this.crossoverOperator = "SingleBinaryNPointsCrossover";
        this.mutationOperator = "BitFlipMutation";
        this.selectionOperator = "BinaryTournament";
        this.numEvaluations = 1000;
        this.popSize = 50;

        //crossOverProbability
        this.crossoverParameters = new HashMap();
        this.crossoverParameters.put("probability", String.valueOf(0.7));
        this.crossoverParameters.put("points", String.valueOf(2));
        //mutationProbability
        this.mutationParameters = new HashMap();
        this.mutationParameters.put("probability", String.valueOf(0.1));

        this.selectionParameters = new HashMap();

        this.stoppingRules = new HashMap<>();
        HashMap ruleParams = new HashMap();
        ruleParams.put("populationSize", this.popSize);
        ruleParams.put("maxEvaluations", this.numEvaluations);
        this.stoppingRules.put("EvaluationsStoppingRule", ruleParams);
    }


    public GAParameters(int numEvaluations, int popSize, String crossoverOperator, String mutationOperator,
                        String selectionOperator, HashMap crossoverParameters, HashMap mutationParameters,
                        HashMap selectionParameters, HashMap<String, HashMap> stoppingRules) {
        super();
        this.numEvaluations = numEvaluations;
        this.popSize = popSize;
        this.crossoverOperator = crossoverOperator;
        this.mutationOperator = mutationOperator;
        this.selectionOperator = selectionOperator;
        this.crossoverParameters = crossoverParameters;
        this.mutationParameters = mutationParameters;
        this.selectionParameters = selectionParameters;
        this.stoppingRules = stoppingRules;
    }

    public GAParameters(GAConfiguration gaConfig) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
        super();
        this.numEvaluations = gaConfig.getNumEvaluations();
        this.popSize = gaConfig.getPopSize();
        this.crossoverOperator = gaConfig.getCrossoverOperator();
        this.mutationOperator = gaConfig.getMutationOperator();
        this.selectionOperator = gaConfig.getSelectionOperator();
        this.crossoverParameters = new HashMap();
        for (ConfigurationParameter cp : gaConfig.getCrossoverParameters()) {
            Class cl = Class.forName(cp.getParameterType());
            this.crossoverParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
        }
        this.mutationParameters = new HashMap();
        for (ConfigurationParameter cp : gaConfig.getMutationParameters()) {
            Class cl = Class.forName(cp.getParameterType());
            this.mutationParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
        }
        this.selectionParameters = new HashMap();
        for (ConfigurationParameter cp : gaConfig.getSelectionParameters()) {
            Class cl = Class.forName(cp.getParameterType());
            this.selectionParameters.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
        }
        this.stoppingRules = new HashMap<>();
        for (StoppingRule sr : gaConfig.getStoppingRules()) {
            HashMap params = new HashMap();
            for (ConfigurationParameter cp : sr.getRuleParameters()) {
                Class cl = Class.forName(cp.getParameterType());
                params.put(cp.getParameterName(), cl.getConstructor(String.class).newInstance(cp.getParameterValue()));
            }
            this.stoppingRules.put(sr.getStoppingRuleName(), params);
        }
    }

    public int getNumEvaluations() {
        return this.numEvaluations;
    }

    public int getPopSize() {
        return this.popSize;
    }

    public String getCrossoverOperator() {
        return this.crossoverOperator;
    }

    public String getMutationOperator() {
        return this.mutationOperator;
    }

    public String getSelectionOperator() {
        return this.selectionOperator;
    }

    public HashMap getCrossoverParameters() {
        return this.crossoverParameters;
    }

    public HashMap getMutationParameters() {
        return this.mutationParameters;
    }

    public HashMap getSelectionParameters() {
        return this.selectionParameters;
    }

    public HashMap<String, HashMap> getStoppingRules() {
        return this.stoppingRules;
    }


    @Override
    public GAParameters clone() {
        GAParameters other = new GAParameters();

        other.crossoverOperator = this.crossoverOperator;
        other.mutationOperator = this.mutationOperator;
        other.selectionOperator = this.selectionOperator;
        other.numEvaluations = this.numEvaluations;
        other.popSize = this.popSize;

        other.crossoverParameters = new HashMap(this.crossoverParameters);

        other.mutationParameters = new HashMap(this.mutationParameters);

        other.selectionParameters = new HashMap(this.selectionParameters);

        other.stoppingRules = new HashMap(this.stoppingRules);

        return other;
    }
}