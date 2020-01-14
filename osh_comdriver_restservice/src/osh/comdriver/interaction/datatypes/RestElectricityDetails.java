package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.Collection;
import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@XmlType(name = "electricityDetails")
public class RestElectricityDetails extends RestStateDetail {

    protected UUID meterUuid;

    protected double voltage;

    protected double current;

    protected double activePower;

    protected double reactivePower;

    protected double totalEnergyConsumption;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public RestElectricityDetails() {
        this(null, 0);
    }

    public RestElectricityDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    static public RestElectricityDetails aggregatePowerDetails(Collection<RestElectricityDetails> details) {
        int _pdCount = 0;
        RestElectricityDetails _pd = new RestElectricityDetails(null, 0);

        for (RestElectricityDetails p : details) {
            _pd.activePower += p.activePower;
            _pd.current += p.current;
            _pd.totalEnergyConsumption += p.totalEnergyConsumption;
            _pd.reactivePower += p.reactivePower;
            _pd.voltage += p.voltage;

            _pdCount++;
        }

        if (_pdCount == details.size() && _pdCount > 0) {
            // only voltage is averaged
            _pd.voltage /= _pdCount;

            return _pd;
        } else // ERROR: undefined state due to missing data
            return null;
    }

    public UUID getMeterUuid() {
        return this.meterUuid;
    }

    public void setMeterUuid(UUID meterUuid) {
        this.meterUuid = meterUuid;
    }

    public double getVoltage() {
        return this.voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return this.current;
    }

    public void setCurrent(double current) {
        this.current = current;
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

    public double getTotalEnergyConsumption() {
        return this.totalEnergyConsumption;
    }

    public void setTotalEnergyConsumption(double totalEnergyConsumption) {
        this.totalEnergyConsumption = totalEnergyConsumption;
    }

    /**
     * Calculates a pseudo distance between two measurements
     * A value >= 1.0 is considered significant ( which is approx. 5 W difference )
     *
     * @param other
     * @return
     */
    public double distance(RestElectricityDetails other) {
        if (other == null)
            return Double.MAX_VALUE;

        double dist = 0.0;

        dist += Math.abs(this.activePower - other.activePower) / 10;
        dist += Math.abs(this.voltage - other.voltage) / 230;
        dist += Math.abs(this.current - other.current) / 16;

        return dist * 2.0;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        long temp;
        temp = Double.doubleToLongBits(this.activePower);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.current);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result
                + ((this.meterUuid == null) ? 0 : this.meterUuid.hashCode());
        temp = Double.doubleToLongBits(this.reactivePower);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.totalEnergyConsumption);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(this.voltage);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        RestElectricityDetails other = (RestElectricityDetails) obj;
        if (Double.doubleToLongBits(this.activePower) != Double
                .doubleToLongBits(other.activePower))
            return false;
        if (Double.doubleToLongBits(this.current) != Double
                .doubleToLongBits(other.current))
            return false;
        if (this.meterUuid == null) {
            if (other.meterUuid != null)
                return false;
        } else if (!this.meterUuid.equals(other.meterUuid))
            return false;
        if (Double.doubleToLongBits(this.reactivePower) != Double
                .doubleToLongBits(other.reactivePower))
            return false;
        if (Double.doubleToLongBits(this.totalEnergyConsumption) != Double
                .doubleToLongBits(other.totalEnergyConsumption))
            return false;
        return Double.doubleToLongBits(this.voltage) == Double
                .doubleToLongBits(other.voltage);
    }

    @Override
    public String toString() {
        return "Power: { " +
                "U=" + this.voltage + "V, " +
                "I=" + this.current + "A, " +
                "P=" + this.activePower + "W " +
                "Q=" + this.reactivePower + "var " +
                "E=" + this.totalEnergyConsumption + "Wh" +
                "}";
    }


}
