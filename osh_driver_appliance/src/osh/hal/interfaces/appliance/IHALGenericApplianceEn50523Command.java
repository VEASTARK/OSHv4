package osh.hal.interfaces.appliance;

import osh.en50523.EN50523OIDExecutionOfACommandCommands;

/**
 * @author Ingo Mauser
 */
public interface IHALGenericApplianceEn50523Command {
    EN50523OIDExecutionOfACommandCommands getApplianceCommand();
}
