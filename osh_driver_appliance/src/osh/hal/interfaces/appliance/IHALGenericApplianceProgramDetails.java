package osh.hal.interfaces.appliance;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public interface IHALGenericApplianceProgramDetails {
    ApplianceProgramConfigurationStatus getApplianceConfigurationProfile();

    UUID getAcpID();

    long getAcpReferenceTime();
}
