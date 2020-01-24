package osh.hal.exchange;

import osh.eal.hal.exchange.HALControllerExchange;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.hal.interfaces.appliance.IHALGenericApplianceEn50523Command;

import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class MieleApplianceControllerExchange extends HALControllerExchange
        implements IHALGenericApplianceEn50523Command {

    private final EN50523OIDExecutionOfACommandCommands applianceCommand;


    /**
     * CONSTRUCTOR
     *
     * @param deviceID
     * @param timestamp
     */
    public MieleApplianceControllerExchange(
            UUID deviceID,
            long timestamp,
            EN50523OIDExecutionOfACommandCommands applianceCommand) {
        super(deviceID, timestamp);

        this.applianceCommand = applianceCommand;
    }

    @Override
    public EN50523OIDExecutionOfACommandCommands getApplianceCommand() {
        return this.applianceCommand;
    }

    // CLONING not necessary

}
