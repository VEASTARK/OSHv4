package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@XmlType(name = "temperatureSensorDetails")
public class RestTemperatureSensorDetails extends RestStateDetail {

    protected double temperature;

    /**
     * for JAXB
     */
    @Deprecated
    public RestTemperatureSensorDetails() {
        this(null, 0);
    }

    public RestTemperatureSensorDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public double getTemperature() {
        return this.temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }


}
