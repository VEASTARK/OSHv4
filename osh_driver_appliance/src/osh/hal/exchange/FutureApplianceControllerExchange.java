package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.time.ZonedDateTime;
import java.util.UUID;


/**
 * @author Ingo Mauser, Julian Rothenbacher
 */
public class FutureApplianceControllerExchange
        extends HALControllerExchange {


    private final UUID applianceConfigurationProfileID;
    private final int selectedProfileId;
    private final ZonedDateTime[] selectedStartTimes;


    /**
     * CONSTRUCTOR
     */
    public FutureApplianceControllerExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            UUID applianceConfigurationProfileID,
            int selectedProfileId,
            ZonedDateTime[] selectedStartTimes
    ) {
        super(deviceID, timestamp);

        this.applianceConfigurationProfileID = applianceConfigurationProfileID;
        this.selectedProfileId = selectedProfileId;
        this.selectedStartTimes = selectedStartTimes;
    }

    public UUID getApplianceConfigurationProfileID() {
        return this.applianceConfigurationProfileID;
    }

    public int getSelectedProfileId() {
        return this.selectedProfileId;
    }

    public ZonedDateTime[] getSelectedStartTimes() {
        return this.selectedStartTimes;
    }


    // CLONING not necessary

}
