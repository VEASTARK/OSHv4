package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@XmlType(name = "temperatureDetails")
public class RestTemperatureDetails extends RestStateDetail {

    protected double temperature;

    protected Map<String, Double> auxiliaryTemperatures;

    /**
     * for JAXB
     */
    @Deprecated
    public RestTemperatureDetails() {
        this(null, 0);
    }

    public RestTemperatureDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public Map<String, Double> getAuxiliaryTemperatures() {
        return this.auxiliaryTemperatures;
    }

    public void setAuxiliaryTemperatures(
            Map<String, Double> auxiliaryTemperatures) {
        this.auxiliaryTemperatures = auxiliaryTemperatures;
    }

    public void setAuxiliaryTemperature(String name, double temperature) {
        if (this.auxiliaryTemperatures == null) {
            this.auxiliaryTemperatures = new HashMap<>();
        }
        this.auxiliaryTemperatures.put(name, temperature);
    }
}
