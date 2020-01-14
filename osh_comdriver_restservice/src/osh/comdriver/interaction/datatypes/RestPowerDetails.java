package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@XmlType(name = "powerDetails")
public class RestPowerDetails extends RestStateDetail {

    protected double activePower;

    protected double reactivePower;

    /**
     * for JAXB
     */
    @Deprecated
    public RestPowerDetails() {
        this(null, 0);
    }

    public RestPowerDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public double getActivePower() {
        return this.activePower;
    }

    public void setActivePower(double activePower) {
        this.activePower = activePower;
    }

    public double getReactivePower() {
        return this.reactivePower;
    }

    public void setReactivePower(double reactivePower) {
        this.reactivePower = reactivePower;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof RestPowerDetails))
            return false;
        RestPowerDetails other = (RestPowerDetails) obj;

        return (this.activePower == other.activePower) &&
                (this.reactivePower == other.reactivePower);
    }

    @Override
    public String toString() {
        return "Power: { " +
                "P=" + this.activePower + "W " +
                "Q=" + this.reactivePower + "var " +
                "}";
    }
}
