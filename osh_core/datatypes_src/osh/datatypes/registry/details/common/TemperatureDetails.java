package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;
import osh.utils.DeepCopy;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */

public class TemperatureDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 4909123900868946070L;


    private Double temperature;

    private HashMap<String, Double> auxiliaryTemperatures = new HashMap<>();

    public TemperatureDetails(UUID deviceId, ZonedDateTime timestamp) {
        super(deviceId, timestamp);
    }

    public Double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Set<String> getAuxiliaryTemperatureKeys() {
        return this.auxiliaryTemperatures.keySet();
    }

    public HashMap<String, Double> getAuxiliaryTemperatures() {
        return this.auxiliaryTemperatures;
    }

    public void setAuxiliaryTemperatures(HashMap<String, Double> auxiliaryTemperatures) {
        this.auxiliaryTemperatures = auxiliaryTemperatures;
    }

    public void addAuxiliaryTemperatures(String key, Double temperature) {
        this.auxiliaryTemperatures.put(key, temperature);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[ temperature: ");
        if (this.temperature != null) builder.append(this.temperature.toString());
        else builder.append("null");
        builder.append(", auxiliaryTemperatures: [ \n");

        for (Entry<String, Double> e : this.auxiliaryTemperatures.entrySet()) {
            builder.append('\t').append(e.getKey()).append(": ").append(e.getValue()).append('\n');
        }
        builder.append("] ]\n");

        return builder.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof TemperatureDetails))
            return false;

        TemperatureDetails other = (TemperatureDetails) obj;

        return this.temperature.equals(other.temperature)
                && this.auxiliaryTemperatures
                .equals(other.auxiliaryTemperatures);
    }

    @Override
    public StateExchange clone() {
        synchronized (this) {
            return (StateExchange) DeepCopy.copy(this);
        }
    }
}

