package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ChillerControllerExchange
        extends HALControllerExchange {

    private boolean stopGenerationFlag;

    private boolean coolingRequest;

    private final int scheduledRuntime;

    /**
     * @param deviceID
     * @param timestamp
     */
    public ChillerControllerExchange(
            UUID deviceID,
            Long timestamp,
            boolean stopGenerationFlag,
            boolean coolingRequest,
            int scheduledRuntime) {
        super(deviceID, timestamp);

        this.stopGenerationFlag = stopGenerationFlag;
        this.coolingRequest = coolingRequest;
        this.scheduledRuntime = scheduledRuntime;
    }


    public boolean isStopGenerationFlag() {
        return this.stopGenerationFlag;
    }

    public void setStopGenerationFlag(boolean stopGenerationFlag) {
        this.stopGenerationFlag = stopGenerationFlag;
    }

    public boolean isCoolingRequest() {
        return this.coolingRequest;
    }

    public void setCoolingRequest(boolean heatingRequest) {
        this.coolingRequest = heatingRequest;
    }

    public int getScheduledRuntime() {
        return this.scheduledRuntime;
    }
}