package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.miele.DeviceProfile;
import osh.configuration.appliance.miele.ProfileTick.Load;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.eal.hal.exceptions.HALException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.xml.XMLSerialization;

import java.util.List;
import java.util.UUID;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public abstract class MieleApplianceSimulationDriver
        extends ApplianceSimulationDriver {

    // single load profile...
    protected DeviceProfile deviceProfile;

    protected boolean hasProfile;
    protected boolean isIntelligent;
    /**
     * middle in sense of consumption
     */
    protected final int middleOfPowerConsumption = -1;
    protected long programStart = -1;
    /**
     * in Ws (Watt-seconds)
     */
    protected int activePowerSumPerRun = -1;
    protected int device2ndDof;
    private int programDuration;
    private boolean systemState;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     * @throws HALException
     */
    public MieleApplianceSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws HALException {
        super(osh, deviceID, driverConfig);

        String profileSourceName = driverConfig.getParameter("profilesource");
        try {
            this.deviceProfile = (DeviceProfile) XMLSerialization.file2Unmarshal(profileSourceName, DeviceProfile.class);
        } catch (Exception ex) {
            this.getGlobalLogger().logError("An error occurred while loading the device profile: " + ex.getMessage());
        }

        this.device2ndDof = Integer.parseInt(driverConfig.getParameter("device2nddof"));

        this.programDuration = this.deviceProfile.getProfileTicks().getProfileTick().size();

    }


    /*
     * PLZ use onProcessingTimeTick() or onActiveTimeTick()
     */
    @Override
    final public void onNextTimeTick() {

        this.onProcessingTimeTick();

        if (this.systemState) {

            //next tick
            if (this.programStart < 0) {
                this.getGlobalLogger().logError("systemState is true, but programStart < 0", new Exception());
                this.turnOff();
                return;
            }

            long currentTime = this.getTimeDriver().getUnixTime();
            int currentDurationSinceStart = (int) (currentTime - this.programStart);
            if (currentDurationSinceStart < 0) {
                this.getGlobalLogger().logError("timewarp: currentDurationSinceStart is negative", new Exception());
            }


            if (this.programDuration > currentDurationSinceStart) {

                // iterate commodities
                List<Load> loadList = this.deviceProfile.getProfileTicks().getProfileTick().get(currentDurationSinceStart).getLoad();
                for (Load load : loadList) {
                    Commodity currentCommodity = Commodity.fromString(load.getCommodity());

                    if (currentCommodity == Commodity.ACTIVEPOWER) {
                        this.setPower(Commodity.ACTIVEPOWER, load.getValue());
                    } else if (currentCommodity == Commodity.REACTIVEPOWER) {
                        this.setPower(Commodity.REACTIVEPOWER, load.getValue());
                    }
                }

                this.onActiveTimeTick();
            } else {
                // turn off the device
                this.turnOff();
            }
        }
    }

    private void turnOff() {
        this.systemState = false;
        this.setPower(Commodity.ACTIVEPOWER, 0);
        this.setPower(Commodity.REACTIVEPOWER, 0);
        this.programStart = -1;
        this.onProgramEnd();
    }


    /**
     * is always invoked while processing a time tick (when onNextTimeTick() has been invoked)
     */
    protected abstract void onProcessingTimeTick();

    /**
     * is invoked while processing a time tick AND the appliance is running
     */
    protected abstract void onActiveTimeTick();

    /**
     * is invoked when the program stops at the end of a work-item
     */
    protected abstract void onProgramEnd();

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //check if the appliance is running
        this.setSystemState(nextAction.isNextState());
    }

    public void setHasProfile(boolean profile) {
        this.hasProfile = profile;
    }


    public void setSystemState(boolean systemState) {
        if (!this.systemState && systemState) {
            this.programStart = this.getTimeDriver().getUnixTime();
        }

        this.systemState = systemState;
    }

    protected int getProgramDuration() {
        return this.programDuration;
    }

    protected int getMiddleOfDuration() {
        return this.middleOfPowerConsumption;
    }

    @Override
    public boolean isIntelligent() {
        return this.isIntelligent;
    }

    public boolean hasProfile() {
        return this.hasProfile;
    }

}
