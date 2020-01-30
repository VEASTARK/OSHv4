package osh.datatypes.dof;

import osh.datatypes.registry.StateExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class DofStateExchange extends StateExchange {


    /**
     *
     */
    private static final long serialVersionUID = -281161281421829514L;
    private Duration device1stDegreeOfFreedom;
    private Duration device2ndDegreeOfFreedom;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public DofStateExchange(UUID sender, ZonedDateTime timestamp) {
        super(sender, timestamp);

        this.device1stDegreeOfFreedom = Duration.ZERO;
        this.device2ndDegreeOfFreedom = Duration.ZERO;
    }


    public Duration getDevice1stDegreeOfFreedom() {
        return this.device1stDegreeOfFreedom;
    }

    public void setDevice1stDegreeOfFreedom(Duration device1stDegreeOfFreedom) {
        this.device1stDegreeOfFreedom = device1stDegreeOfFreedom;
    }

    public Duration getDevice2ndDegreeOfFreedom() {
        return this.device2ndDegreeOfFreedom;
    }

    public void setDevice2ndDegreeOfFreedom(Duration device2ndDegreeOfFreedom) {
        this.device2ndDegreeOfFreedom = device2ndDegreeOfFreedom;
    }

    @Override
    public DofStateExchange clone() {
        DofStateExchange cloned = new DofStateExchange(this.getSender(), this.getTimestamp());

        cloned.device1stDegreeOfFreedom = this.device1stDegreeOfFreedom;
        cloned.device2ndDegreeOfFreedom = this.device2ndDegreeOfFreedom;
        return cloned;
    }

    @Override
    public String toString() {
        return "Device:" + this.getSender() + ", FirstDoF: " + this.device1stDegreeOfFreedom + "\n SecondDoF: " + this.device2ndDegreeOfFreedom + " }";
    }

}
