package osh.datatypes.registry.driver.details.appliance;

import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * StateExchange for communication of DoF details
 * (from device)
 *
 * @author Ingo Mauser
 */

public class GenericApplianceDofDriverDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -1441502884455073409L;

    /**
     * DoF for initial scheduling
     */
    protected int appliance1stDof;

    /**
     * DoF for rescheduling
     */
    protected int appliance2ndDof;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    private GenericApplianceDofDriverDetails() {
        this(null, null);
    }


    /**
     * CONSTRUCTOR
     */
    public GenericApplianceDofDriverDetails(
            UUID sender,
            ZonedDateTime timestamp) {
        super(sender, timestamp);
    }


    public int getAppliance1stDof() {
        return this.appliance1stDof;
    }

    public void setAppliance1stDof(int appliance1stDof) {
        this.appliance1stDof = appliance1stDof;
    }

    public int getAppliance2ndDof() {
        return this.appliance2ndDof;
    }

    public void setAppliance2ndDof(int appliance2ndDof) {
        this.appliance2ndDof = appliance2ndDof;
    }

}
