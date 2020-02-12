package osh.datatypes.registry.driver.details.energy;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class ElectricPowerDriverDetails extends StateExchange {

    /**
     * Electrical Power Details for Logging
     */
    private UUID meterUuid;

    private double activePower;

    private double reactivePower;

    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public ElectricPowerDriverDetails(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);
    }

    static public ElectricPowerDriverDetails aggregatePowerDetails(UUID sender, Collection<ElectricPowerDriverDetails> details) {
        int _pdCount = 0;
        ZonedDateTime timestamp = null;
        double activeSum = 0, reactiveSum = 0;

        for (ElectricPowerDriverDetails p : details) {
            activeSum += p.activePower;
            reactiveSum += p.reactivePower;
            timestamp = p.getTimestamp(); //why?
            _pdCount++;
        }

        ElectricPowerDriverDetails _pd = new ElectricPowerDriverDetails(sender, timestamp);
        _pd.activePower = activeSum;
        _pd.reactivePower = reactiveSum;

        if (_pdCount == details.size() && _pdCount > 0) {
            return _pd;
        } else {
            // ERROR: undefined state due to missing data
            return null;
        }
    }

    public UUID getMeterUuid() {
        return this.meterUuid;
    }

    public void setMeterUuid(UUID meterUuid) {
        this.meterUuid = meterUuid;
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
    public String toString() {
        return "Electric Power: { " +
                "MeterUUID=" + this.meterUuid + ", " +
                "P=" + this.activePower + "W " +
                "Q=" + this.reactivePower + "var, " +
                "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ElectricPowerDriverDetails)) {
            return false;
        }

        ElectricPowerDriverDetails other = (ElectricPowerDriverDetails) obj;

        return (this.meterUuid.equals(other.meterUuid)) &&
                (this.activePower == other.activePower) &&
                (this.reactivePower == other.reactivePower);
    }

}
