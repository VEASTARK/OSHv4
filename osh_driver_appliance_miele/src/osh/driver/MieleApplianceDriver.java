package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.configuration.appliance.miele.DeviceProfile;
import osh.configuration.appliance.miele.ProfileTick;
import osh.configuration.system.DeviceTypes;
import osh.core.RegistryType;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.PowerProfileTick;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.commands.StartDeviceRequest;
import osh.datatypes.registry.commands.StopDeviceRequest;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceDriverDetails;
import osh.datatypes.registry.driver.details.appliance.GenericApplianceProgramDriverDetails;
import osh.datatypes.registry.driver.details.appliance.miele.MieleApplianceDriverDetails;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.hal.exchange.GenericApplianceDofObserverExchange;
import osh.hal.exchange.GenericApplianceStartTimesControllerExchange;
import osh.hal.exchange.MieleApplianceControllerExchange;
import osh.hal.exchange.MieleApplianceObserverExchange;
import osh.registry.interfaces.IDataRegistryListener;
import osh.utils.time.TimeUtils;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class MieleApplianceDriver
        extends HALDeviceDriver
        implements IDataRegistryListener {

    // uuid of this appliance as posted by the bus driver
    private UUID applianceBusDriverUUID;

    // driverData
    private DeviceProfile deviceProfile;
    private EnumMap<Commodity, ArrayList<PowerProfileTick>> currentLoadProfiles;
    private ZonedDateTime programStartedTime;

    //temporal degree of freedom
    private Duration firstDof;
    private Duration secondDof;

    // pending command
    private EN50523OIDExecutionOfACommandCommands pendingCommand;

    //successive incomplete errors count
    private int incompleteData;

    // stored data from bus drivers
    private GenericApplianceDriverDetails currentAppDetails;
    private GenericApplianceProgramDriverDetails appProgramDetails;
    private MieleApplianceDriverDetails mieleApplianceDriverDetails;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public MieleApplianceDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) throws HALException {
        super(osh, deviceID, driverConfig);

        String cfgApplianceUUID = driverConfig.getParameter("applianceuuid");
        if (cfgApplianceUUID == null) {
            throw new HALException("Need config parameter applianceuuid");
        }
        this.applianceBusDriverUUID = UUID.fromString(cfgApplianceUUID);

        try {
            this.compressionType = LoadProfileCompressionTypes.valueOf(this.getDriverConfig().getParameter("compressionType"));
        } catch (Exception e) {
            this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
            this.getGlobalLogger().logWarning("Can't get compressionType, using the default value: " + this.compressionType);
        }

        try {
            this.compressionValue = Integer.parseInt(this.getDriverConfig().getParameter("compressionValue"));
        } catch (Exception e) {
            this.compressionValue = 100;
            this.getGlobalLogger().logWarning("Can't get compressionValue, using the default value: " + this.compressionValue);
        }
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.setDataSourcesUsed(this.getMeterUuids());
        this.setDataSourcesConfigured(Collections.singleton(this.applianceBusDriverUUID));

        // at the moment we have only one Power Profile
        this.loadDeviceProfiles();
        this.currentLoadProfiles = this.generatePowerProfiles();

        // register for changes of different details...
        this.getDriverRegistry().subscribe(GenericApplianceDriverDetails.class, this.applianceBusDriverUUID,this);
        this.getDriverRegistry().subscribe(GenericApplianceProgramDriverDetails.class, this.applianceBusDriverUUID, this);
        this.getDriverRegistry().subscribe(DofStateExchange.class, this.getUUID(), this);
        this.getDriverRegistry().subscribe(MieleApplianceDriverDetails.class, this.applianceBusDriverUUID, this);

        //DofStateExchange will only be published to the com registry, we need a data reach through for this
        this.getOSH().getDataBroker().registerDataReachThrough(
                this.getUUID(),
                DofStateExchange.class,
                RegistryType.COM,
                RegistryType.DRIVER);

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

        StaticCompressionExchange stat = new StaticCompressionExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        stat.setCompressionType(this.compressionType);
        stat.setCompressionValue(this.compressionValue);
        this.notifyObserver(stat);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        if (MieleApplianceDriver.this.pendingCommand == EN50523OIDExecutionOfACommandCommands.START) {
            StartDeviceRequest req = new StartDeviceRequest(
                    MieleApplianceDriver.this.getUUID(),
                    MieleApplianceDriver.this.applianceBusDriverUUID,
                    exchange.getTime());
            MieleApplianceDriver.this.getDriverRegistry().publish(StartDeviceRequest.class, req);
        }
    }

    private ArrayList<PowerProfileTick> shrinkPowerProfile(
            Commodity commodity,
            List<PowerProfileTick> powerProfile,
            long programDuration) {
        ArrayList<PowerProfileTick> _tmpList = new ArrayList<>();

        //if it's greater => shrink it!
        if (powerProfile.size() >= programDuration) {
            for (int i = 0; i < programDuration; i++) {
                _tmpList.add(powerProfile.get(i));
            }
        } else {
            _tmpList.addAll(powerProfile);
            //expand it
            for (int i = 0; i < (programDuration - powerProfile.size()); i++) {
                _tmpList.add(powerProfile.get(powerProfile.size() - 1));
            }
        }

        return _tmpList;
    }


    private EnumMap<Commodity, ArrayList<PowerProfileTick>> generatePowerProfiles() {

        EnumMap<Commodity, ArrayList<PowerProfileTick>> profiles = new EnumMap<>(Commodity.class);

        int count = 0;

        // iterate time ticks
        for (ProfileTick profileTick : this.deviceProfile.getProfileTicks().getProfileTick()) {

            // iterate commodities
            for (int i = 0; i < profileTick.getLoad().size(); i++) {

                Commodity currentCommodity = Commodity.fromString(profileTick.getLoad().get(i).getCommodity());

                ArrayList<PowerProfileTick> _pwrProfileList = profiles.computeIfAbsent(currentCommodity, k -> new ArrayList<>());

                PowerProfileTick _pwrPro = new PowerProfileTick();
                _pwrPro.commodity = currentCommodity;
                _pwrPro.timeTick = count;
                _pwrPro.load = profileTick.getLoad().get(i).getValue();
                _pwrProfileList.add(_pwrPro);
            }

            ++count;
        }

        return profiles;
    }

    private void loadDeviceProfiles() throws OSHException {
        String profileSourceName = this.getDriverConfig().getParameter("profilesource");
        //load profiles
        try {
            this.deviceProfile = (DeviceProfile) XMLSerialization.file2Unmarshal(profileSourceName, DeviceProfile.class);
        } catch (FileNotFoundException | JAXBException e) {
            throw new OSHException(e);
        }
    }


    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {

        try {
            if (controllerRequest instanceof MieleApplianceControllerExchange) {
                MieleApplianceControllerExchange controllerExchange = (MieleApplianceControllerExchange) controllerRequest;

                if (controllerExchange.getApplianceCommand() == EN50523OIDExecutionOfACommandCommands.START) {
                    StartDeviceRequest req = new StartDeviceRequest(this.getUUID(), this.applianceBusDriverUUID, controllerRequest.getTimestamp());
                    this.getDriverRegistry().publish(StartDeviceRequest.class, req);
                    this.pendingCommand = EN50523OIDExecutionOfACommandCommands.START;
                }
                if (controllerExchange.getApplianceCommand() == EN50523OIDExecutionOfACommandCommands.STOP) {
                    StopDeviceRequest req = new StopDeviceRequest(this.getUUID(), this.applianceBusDriverUUID, controllerRequest.getTimestamp());
                    this.getDriverRegistry().publish(StopDeviceRequest.class, req);
                }
            } else if (controllerRequest instanceof GenericApplianceStartTimesControllerExchange) {
                GenericApplianceStartTimesControllerExchange gasce = (GenericApplianceStartTimesControllerExchange) controllerRequest;
                ExpectedStartTimeExchange este = new ExpectedStartTimeExchange(this.applianceBusDriverUUID, gasce.getStartTime());
                este.setExpectedStartTime(gasce.getStartTime());
                this.getDriverRegistry().publish(ExpectedStartTimeExchange.class, este);
            }

        } catch (Exception reqEx) {
            this.getGlobalLogger().logError("Request to Miele Gateway failed!", reqEx);
        }
    }


    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        // our device? then: build observer exchange
            boolean updateOx = false;

            if (exchange instanceof DofStateExchange) {
                //making sure dof is only set for 'really' controllable devices
                if (this.getDeviceType() == DeviceTypes.WASHINGMACHINE
                        || this.getDeviceType() == DeviceTypes.DISHWASHER
                        || this.getDeviceType() == DeviceTypes.DRYER) {
                    DofStateExchange dse = (DofStateExchange) exchange;
                    this.firstDof = dse.getDevice1stDegreeOfFreedom();
                    this.secondDof = dse.getDevice2ndDegreeOfFreedom();
                    //sanity
                    if (this.firstDof.isNegative() || this.secondDof.isNegative()) {
                        this.getGlobalLogger().logError("Received illegal dof, not sending to o/c");
                    } else {
                        GenericApplianceDofObserverExchange gadoe = new GenericApplianceDofObserverExchange(this.getUUID(),
                                this.getTimeDriver().getCurrentTime());
                        gadoe.setDevice1stDegreeOfFreedom(this.firstDof);
                        gadoe.setDevice1stDegreeOfFreedom(this.secondDof);
                        this.notifyObserver(gadoe);
                    }

                }

            } else if (exchange instanceof GenericApplianceDriverDetails ||
                    exchange instanceof GenericApplianceProgramDriverDetails ||
                    exchange instanceof MieleApplianceDriverDetails) {
                this.currentAppDetails =
                        (GenericApplianceDriverDetails) this.getDriverRegistry().getData(GenericApplianceDriverDetails.class, this.applianceBusDriverUUID);
                this.appProgramDetails =
                        (GenericApplianceProgramDriverDetails) this.getDriverRegistry().getData(GenericApplianceProgramDriverDetails.class, this.applianceBusDriverUUID);
                this.mieleApplianceDriverDetails =
                        (MieleApplianceDriverDetails) this.getDriverRegistry().getData(MieleApplianceDriverDetails.class, this.applianceBusDriverUUID);

                updateOx = true;
            }



            // generate ox object
            if (updateOx) {
                MieleApplianceObserverExchange _ox = new MieleApplianceObserverExchange(
                        this.getUUID(), this.getTimeDriver().getCurrentTime());

                // check for incomplete data
                if (this.currentAppDetails == null) {
                    if (this.incompleteData == 0)
                        this.getGlobalLogger().logWarning("appDetails not available. Wait for data... UUID: " + this.getUUID());
                    this.incompleteData++;
                    return;
                }
                if (this.appProgramDetails == null) {
                    if (this.incompleteData == 0)
                        this.getGlobalLogger().logWarning("appProgramDetails not available. Wait for data... UUID: " + this.getUUID());
                    this.incompleteData++;
                    return;
                }

                if (this.mieleApplianceDriverDetails == null) {
                    if (this.incompleteData == 0)
                        this.getGlobalLogger().logWarning("mieleApplianceDriverDetails not available. Wait for data... UUID: " + this.getUUID());
                    this.incompleteData++;
                    return;
                }


                _ox.setEn50523DeviceState(this.currentAppDetails.getState());
                _ox.setProgramName(this.appProgramDetails.getProgramName());
                _ox.setPhaseName(this.appProgramDetails.getPhaseName());
                //don't get Power Profile from Program Details, use stuff from file.

                // calculate profile
                if (this.currentAppDetails.getState() != null && this.mieleApplianceDriverDetails != null) {
                    switch (this.currentAppDetails.getState()) {
                        case PROGRAMMEDWAITINGTOSTART:
                        case PROGRAMMED: {
                            Duration maxProgramDuration = this.mieleApplianceDriverDetails.getExpectedProgramDuration();

                            // Miele Gateway needs some time before it delivers the correct information about program duration
                            if (maxProgramDuration.isNegative())
                                return;

                            EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles = new EnumMap<>(Commodity.class);

                            for (Entry<Commodity, ArrayList<PowerProfileTick>> e : this.currentLoadProfiles.entrySet()) {
                                ArrayList<PowerProfileTick> expectedPowerProfile = this.shrinkPowerProfile(e.getKey()
                                        , e.getValue(), maxProgramDuration.toSeconds());
                                expectedLoadProfiles.put(e.getKey(), expectedPowerProfile);
                            }

                            _ox.setExpectedLoadProfiles(expectedLoadProfiles);
                            _ox.setDeviceStartTime(this.mieleApplianceDriverDetails.getStartTime());

                            this.programStartedTime = null;

                        }
                        break;
                        case RUNNING: {
                            synchronized (this) { // reset pending command
                                if (this.pendingCommand == EN50523OIDExecutionOfACommandCommands.START) {
                                    this.pendingCommand = null;
                                }
                            }
                            if (this.programStartedTime == null)
                                this.programStartedTime = this.getTimeDriver().getCurrentTime();

                            Duration remainingProgramDuration;
                            if (this.isControllable()) {
                                remainingProgramDuration = this.mieleApplianceDriverDetails.getProgramRemainingTime();
                                ZonedDateTime now = this.getTimeDriver().getCurrentTime();
                                if (remainingProgramDuration == null && TimeUtils.isBeforeEquals(this.programStartedTime, now)) {
                                    // IMA
                                    // @2016-05-20: FIX for hob/oven are "Controllable"
                                    remainingProgramDuration =
                                            Duration.ofSeconds(this.currentLoadProfiles.get(Commodity.ACTIVEPOWER).size()).minus(Duration.between(this.programStartedTime, now));
                                }
                            } else {
                                remainingProgramDuration =
                                        Duration.ofSeconds(this.currentLoadProfiles.get(Commodity.ACTIVEPOWER).size()).minus(Duration.between(this.programStartedTime, this.getTimeDriver().getCurrentTime()));
                            }

                            Duration finishedProgramDuration = Duration.between(this.programStartedTime,
                                    this.getTimeDriver().getCurrentTime());

                            EnumMap<Commodity, ArrayList<PowerProfileTick>> expectedLoadProfiles = new EnumMap<>(Commodity.class);

                            if (remainingProgramDuration != null && !remainingProgramDuration.isNegative()) { // only
                                // makes sense if gateway doesn't
                                // provide this
                                // information
                                for (Entry<Commodity, ArrayList<PowerProfileTick>> e : this.currentLoadProfiles.entrySet()) {
                                    ArrayList<PowerProfileTick> expectedPowerProfile =
                                            this.shrinkPowerProfile(e.getKey(), e.getValue(),
                                                    remainingProgramDuration.plus(finishedProgramDuration).toSeconds());
                                    expectedLoadProfiles.put(e.getKey(), expectedPowerProfile);
                                }
                            }

//						_ox.setProgramRemainingTime(remainingTime);
                            _ox.setExpectedLoadProfiles(expectedLoadProfiles);
                            _ox.setDeviceStartTime(this.mieleApplianceDriverDetails.getStartTime());

                        }
                        break;
                        default: {
                            this.programStartedTime = null;
                        }
                        break;
                    }
                }

                // meta details
                _ox.setName(this.getName());
                _ox.setLocation(this.getLocation());
                _ox.setDeviceType(this.getDeviceType());
                _ox.setDeviceClass(this.getDeviceClassification());
                _ox.setConfigured(true);

                //all data available -> reset error counter
                if (this.incompleteData > 0) {
                    this.getGlobalLogger().logWarning("data source(s) for device: " + this.getUUID() + " are available again after " + this.incompleteData + " missing");
                }
                this.incompleteData = 0;

                this.notifyObserver(_ox);
            }
    }
}
