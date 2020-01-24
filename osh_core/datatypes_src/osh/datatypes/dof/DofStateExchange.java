package osh.datatypes.dof;

import osh.datatypes.registry.StateExchange;

import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class DofStateExchange extends StateExchange {


    /**
     *
     */
    private static final long serialVersionUID = -281161281421829514L;
    private Integer device1stDegreeOfFreedom;
    private Integer device2ndDegreeOfFreedom;


    /**
     * CONSTRUCTOR
     *
     * @param sender
     * @param timestamp
     */
    public DofStateExchange(UUID sender, long timestamp) {
        super(sender, timestamp);

        this.device1stDegreeOfFreedom = 0;
        this.device2ndDegreeOfFreedom = 0;
    }


    public Integer getDevice1stDegreeOfFreedom() {
        return this.device1stDegreeOfFreedom;
    }

    public void setDevice1stDegreeOfFreedom(Integer device1stDegreeOfFreedom) {
        this.device1stDegreeOfFreedom = device1stDegreeOfFreedom;
    }

    public Integer getDevice2ndDegreeOfFreedom() {
        return this.device2ndDegreeOfFreedom;
    }

    public void setDevice2ndDegreeOfFreedom(Integer device2ndDegreeOfFreedom) {
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
