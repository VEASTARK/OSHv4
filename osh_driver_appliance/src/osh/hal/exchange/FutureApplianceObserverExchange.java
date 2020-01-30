package osh.hal.exchange;

import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALDeviceObserverExchange;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.hal.interfaces.gas.IHALGasPowerDetails;
import osh.eal.hal.interfaces.thermal.IHALThermalPowerDetails;
import osh.en50523.EN50523DeviceState;
import osh.hal.interfaces.appliance.IHALGenericApplianceDOF;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALGenericApplianceIsCurrentlyControllable;
import osh.hal.interfaces.appliance.IHALGenericApplianceProgramDetails;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class FutureApplianceObserverExchange
        extends HALDeviceObserverExchange
        implements IHALElectricalPowerDetails,
        IHALGasPowerDetails,
        IHALGenericApplianceDetails,
        IHALGenericApplianceProgramDetails,
        IHALGenericApplianceDOF,
        IHALGenericApplianceIsCurrentlyControllable,
        IHALThermalPowerDetails {

    // ### IHALElectricPowerDetails ###
    private final int activePower;
    private final int reactivePower;

    // ### IHALGasPowerDetails ###
    private final int gasPower;

    // ### IHALThermalPowerDetails ###
    private final int hotWaterPower;
    private final int domesticHotWaterPower;


    // ### IHALGenericApplianceDetails ###
    private EN50523DeviceState en50523DeviceState;

    // ### IHALGenericApplianceProgramDetails ###
    private ApplianceProgramConfigurationStatus applianceConfigurationProfile;
    private UUID acpID;
    private ZonedDateTime acpReferenceTime;

    // ### IHALGenericApplianceDOF ###
    private Duration dof;

    // ### IHALGenericApplianceIsCurrentlyControllable ###
    private boolean currentlyControllable;


    /**
     * CONSTRUCTOR
     */
    public FutureApplianceObserverExchange(
            UUID deviceID,
            ZonedDateTime timestamp,
            int activePower,
            int reactivePower,
            int hotWaterPower,
            int domesticHotWaterPower,
            int gasPower) {
        super(deviceID, timestamp);

        this.activePower = activePower;
        this.reactivePower = reactivePower;
        this.hotWaterPower = hotWaterPower;
        this.domesticHotWaterPower = domesticHotWaterPower;
        this.gasPower = gasPower;
    }

    @Override
    public int getActivePower() {
        return this.activePower;
    }

    @Override
    public int getReactivePower() {
        return this.reactivePower;
    }

    @Override
    public int getGasPower() {
        return this.gasPower;
    }


    @Override
    public EN50523DeviceState getEN50523DeviceState() {
        return this.en50523DeviceState;
    }


    public void setEn50523DeviceState(EN50523DeviceState en50523DeviceState) {
        this.en50523DeviceState = en50523DeviceState;
    }


    @Override
    public ApplianceProgramConfigurationStatus getApplianceConfigurationProfile() {
        return this.applianceConfigurationProfile;
    }


    public void setApplianceConfigurationProfile(
            ApplianceProgramConfigurationStatus applianceConfigurationProfile,
            LoadProfileCompressionTypes profileType,
            final int powerEps,
            final int timeSlotDuration) {
        // clone and compress

        if (applianceConfigurationProfile != null) {
            SparseLoadProfile[][] dynamicLoadProfiles = applianceConfigurationProfile.getDynamicLoadProfiles();
            SparseLoadProfile[][] compressedDynamicLoadProfiles =
                    SparseLoadProfile.getCompressedProfile(
                            profileType,
                            dynamicLoadProfiles,
                            powerEps,
                            timeSlotDuration);

            this.applianceConfigurationProfile = new ApplianceProgramConfigurationStatus(
                    applianceConfigurationProfile.getAcpID(),
                    compressedDynamicLoadProfiles,
                    applianceConfigurationProfile.getMinMaxDurations(),
                    applianceConfigurationProfile.getAcpReferenceTime(),
                    applianceConfigurationProfile.isDoNotReschedule());
        } else {
            this.applianceConfigurationProfile = null;
        }

    }


    @Override
    public UUID getAcpID() {
        return this.acpID;
    }

    public void setAcpID(UUID acpID) {
        this.acpID = acpID;
    }

    @Override
    public ZonedDateTime getAcpReferenceTime() {
        return this.acpReferenceTime;
    }

    public void setAcpReferenceTime(ZonedDateTime acpReferenceTime) {
        this.acpReferenceTime = acpReferenceTime;
    }


    @Override
    public boolean isCurrentlyControllable() {
        return this.currentlyControllable;
    }


    public void setCurrentlyControllable(boolean currentlyControllable) {
        this.currentlyControllable = currentlyControllable;
    }


    @Override
    public int getHotWaterPower() {
        return this.hotWaterPower;
    }


    @Override
    public int getDomesticHotWaterPower() {
        return this.domesticHotWaterPower;
    }

    @Override
    public Duration getDOF() {
        return this.dof;
    }

    public void setDOF(Duration dof) {
        this.dof = dof;
    }


}
