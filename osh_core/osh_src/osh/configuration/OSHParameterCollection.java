package osh.configuration;

import osh.configuration.system.ConfigurationParameter;

import java.util.HashMap;
import java.util.List;


/**
 * internal representation for name-value-pairs used by the OSH
 *
 * @author Florian Allerding
 */
public class OSHParameterCollection {

    private final HashMap<String, String> parameterCollection;

    public OSHParameterCollection() {
        this.parameterCollection = new HashMap<>();
    }

    public String getParameter(String name) {
        return this.parameterCollection.get(name);
    }

    public void setParameter(String name, String value) {

        this.parameterCollection.put(name, value);
    }

    public void loadCollection(List<ConfigurationParameter> configParam) {

        for (ConfigurationParameter configurationParameter : configParam) {
            this.parameterCollection.put(configurationParameter.getParameterName(), configurationParameter.getParameterValue());
        }

    }

}