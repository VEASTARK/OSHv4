package osh.mgmt.globalcontroller.jmetal.builder;

import osh.configuration.oc.*;
import osh.utils.map.MapUtils;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents a wrapper around a {@link AlgorithmConfiguration} providing easier access to contained information.
 *
 * @author Sebastian Kramer
 */
public class AlgorithmConfigurationWrapper {

    private final AlgorithmType algorithm;
    private final VariableEncoding variableEncoding;

    private final Map<StoppingRuleType, StoppingRuleConfiguration> stoppingRuleMap;
    private final Map<OperatorType, OperatorConfiguration> operatorMap;
    private final Map<String, Object> algorithmParameterMap;

    /**
     * Constructs this wrapper around the {@link AlgorithmConfiguration} base.
     *
     * @param base the {@link AlgorithmConfiguration}
     */
    public AlgorithmConfigurationWrapper(AlgorithmConfiguration base) {

        this.algorithm = base.getAlgorithm();
        this.variableEncoding = base.getVariableEncoding();

        this.operatorMap = base.getOperators().stream().collect(Collectors.toMap(OperatorConfiguration::getType, c -> c));
        this.stoppingRuleMap = base.getStoppingRules().stream().collect(Collectors.toMap(StoppingRuleConfiguration::getStoppingRule
                , s -> s));
        this.algorithmParameterMap = MapUtils.mapFromCPCollectionUnpacked(base.getAlgorithmParameters());
    }

    /**
     * Returns the algorithm type.
     *
     * @return the algorithm type
     */
    public AlgorithmType getAlgorithm() {
        return this.algorithm;
    }

    /**
     * Returns the variable encoding for this algorithm configuration.
     *
     * @return the variable encoding
     */
    public VariableEncoding getVariableEncoding() {
        return this.variableEncoding;
    }

    /**
     * Returns a mapping from the stopping rule name to it's parameter configuration.
     *
     * @return a mapping from the stopping rule name to it's parameter configuration
     */
    public Map<StoppingRuleType, StoppingRuleConfiguration> getStoppingRuleMap() {
        return this.stoppingRuleMap;
    }

    /**
     * Returns a mapping from the operator name to it's parameter configuration.
     *
     * @return a mapping from the operator name to it's parameter configuration
     */
    public Map<OperatorType, OperatorConfiguration> getOperatorMap() {
        return this.operatorMap;
    }

    /**
     * Returns a mapping of all algorithm parameters.
     *
     * @return a mapping of all algorithm parameters
     */
    public Map<String, Object> getAlgorithmParameterMap() {
        return this.algorithmParameterMap;
    }
}
