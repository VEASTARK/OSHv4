package osh.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.eal.hal.HALDeviceDriver;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.simulation.energy.IDeviceEnergySubject;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.ScreenplayType;
import osh.simulation.screenplay.SubjectAction;

import java.util.*;

/**
 * Superclass for simulation subjects like simulated appliances or SmartMeters or ....
 * This class inherits from HALDeviceDriver.
 * This is necessary for the capability of an integration into the OSH's HAL
 *
 * @author Florian Allerding, Kaibin Bao, Sebastian Kramer, Ingo Mauser, Till Schuberth
 */
public abstract class DeviceSimulationDriver
        extends HALDeviceDriver
        implements ISimulationSubject, IDeviceEnergySubject {

    // INNER CLASSES

    /**
     * List of Commodities used by this device
     */
    protected final ArrayList<Commodity> usedCommodities;

    // VARIABLES
    protected LimitedCommodityStateMap commodityInputStates;
    protected AncillaryMeterState ancillaryMeterState;
    /**
     * Compression type of any load profiles
     */
    protected LoadProfileCompressionTypes compressionType;

    // ### ESC STUFFF ###
    protected int compressionValue;
    private final Comparator<SubjectAction> actionComparator = (arg0, arg1) -> (int) ((arg0.getTick() - arg1.getTick()));


    // ### Static values ###
    private BuildingSimulationEngine simulationEngine;
    private ISimulationActionLogger simulationActionLogger;
    private SortedSet<SubjectAction> actions;


    /**
     * CONSTRUCTOR
     */
    public DeviceSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        // get Commodities used by this device
        {
            String commoditiesArray = driverConfig.getParameter("usedcommodities");
            if (commoditiesArray != null) {
                this.usedCommodities = Commodity.parseCommodityArray(commoditiesArray);
            } else {
                throw new HALException("Used Commodities are missing!");
            }
        }

        this.actions = new TreeSet<>(this.actionComparator);

        for (Commodity c : Commodity.values()) {
            this.setPower(c, 0);
        }

        // if DeviceClassification.APPLIANCE (but info at this point not yet available!)
        // all conditions after first && should NOT be necessary (but remain for safety reasons)
        if (driverConfig.getParameter("screenplaytype") != null) {

            ScreenplayType screenplayType = ScreenplayType.fromValue(driverConfig.getParameter("screenplaytype"));

            if (screenplayType == ScreenplayType.STATIC) {
                // screenplay is loaded from file...
            } else if (screenplayType == ScreenplayType.DYNAMIC) {
                // NOTHING here...
            } else {
                throw new RuntimeException("value \"screenplayType\" for variable \"screenplaytype\": unknown value!");
            }
        } else {
            throw new RuntimeException("variable \"screenplaytype\" : missing!");
        }

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
    public void onSystemIsUp() {
        StaticCompressionExchange _stat = new StaticCompressionExchange(this.getUUID(), this.getTimeDriver().getCurrentEpochSecond(),
                this.compressionType, this.compressionValue);

        this.notifyObserver(_stat);
    }


    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        //NOTHING
    }

    @Override
    public void onSimulationPreTickHook() {
        //NOTHING
    }

    @Override
    public void onSimulationPostTickHook() {
        //NOTHING
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) throws HALException {
        //NOTHING
    }


    /**
     * delete all actions from the list
     */
    @Override
    public void flushActions() {
        this.actions.clear();
    }

    /**
     * gets all actions for a subject
     *
     * @return
     */
    @Override
    public Collection<SubjectAction> getActions() {
        return this.actions;
    }

    /**
     * Get another subject depending on this subject perhaps to tell him to do something...
     *
     * @param SubjectID
     */
    @Override
    public ISimulationSubject getAppendingSubject(UUID SubjectID) {
        ISimulationSubject _simSubject;

        //ask the simulation engine...
        _simSubject = this.simulationEngine.getSimulationSubjectByID(SubjectID);

        return _simSubject;
    }

    /**
     * is invoked on every (new) time tick to announce the subject
     */
//	@Override
    public abstract void onNextTimeTick();

    /**
     * @param nextAction is invoked when the subject has to do the action "nextAction"
     */
    @Override
    public abstract void performNextAction(SubjectAction nextAction);

    /**
     * Sets an action for this simulation subject
     *
     * @param action
     */
    @Override
    public void setAction(SubjectAction action) {
        this.actions.add(action);
    }


    protected BuildingSimulationEngine getSimulationEngine() {
        return this.simulationEngine;
    }

    @Override
    public void setSimulationEngine(BuildingSimulationEngine simulationEngine) {
        this.simulationEngine = simulationEngine;
    }


    protected ISimulationActionLogger getSimulationActionLogger() {
        return this.simulationActionLogger;
    }

    @Override
    public void setSimulationActionLogger(ISimulationActionLogger simulationLogger) {
        this.simulationActionLogger = simulationLogger;
    }


    @Override
    public void triggerSubject() {
        //invoke for announcement...
        this.onNextTimeTick();

        long currentTimeTick = this.getOSH().getTimeDriver().getCurrentEpochSecond();

        while (!this.actions.isEmpty() && this.actions.first().getTick() <= currentTimeTick) {
            // delete earlier entries (tbd)
            if (this.actions.first().getTick() < currentTimeTick) {
                this.actions.remove(this.actions.first());
                continue;
            }

            // perform next action
            SubjectAction action = this.actions.first();
            if (this.simulationActionLogger != null) {
                this.simulationActionLogger.logAction(action);
            }
            this.performNextAction(action);

            // remove action entry
            try {
                this.actions.remove(this.actions.first());
            } catch (Exception ex) {
                throw new RuntimeException("actions name: " + this.actions.getClass().getCanonicalName(), ex);
            }
        }
    }


    // ### ESC STUFF ###

    @Override
    public LimitedCommodityStateMap getCommodityOutputStates() {
        LimitedCommodityStateMap map = new LimitedCommodityStateMap(this.usedCommodities);
        for (Commodity c : this.usedCommodities) {
            map.setPower(c, this.getPower(c));
        }
        return map;
    }


    @Override
    public void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
            AncillaryMeterState ancillaryMeterState) {
        this.commodityInputStates = inputStates;
        this.ancillaryMeterState = ancillaryMeterState;
    }

}
