package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ChpControllerExchange
        extends HALControllerExchange {

    private boolean stopGenerationFlag;

    private boolean electricityRequest;
    private boolean heatingRequest;

    private final Duration scheduledRuntime;

    /**
     * @param deviceID
     * @param timestamp
     */
    public ChpControllerExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            boolean stopGenerationFlag,
            boolean electricityRequest,
            boolean heatingRequest,
            Duration scheduledRuntime) {
        super(deviceID, timestamp);

        this.stopGenerationFlag = stopGenerationFlag;
        this.electricityRequest = electricityRequest;
        this.heatingRequest = heatingRequest;
        this.scheduledRuntime = scheduledRuntime;
    }


    public boolean isStopGenerationFlag() {
        return this.stopGenerationFlag;
    }

    public void setStopGenerationFlag(boolean stopGenerationFlag) {
        this.stopGenerationFlag = stopGenerationFlag;
    }

    public boolean isElectricityRequest() {
        return this.electricityRequest;
    }

    public void setElectricityRequest(boolean electricityRequest) {
        this.electricityRequest = electricityRequest;
    }

    public boolean isHeatingRequest() {
        return this.heatingRequest;
    }

    public void setHeatingRequest(boolean heatingRequest) {
        this.heatingRequest = heatingRequest;
    }

    public Duration getScheduledRuntime() {
        return this.scheduledRuntime;
    }

}
