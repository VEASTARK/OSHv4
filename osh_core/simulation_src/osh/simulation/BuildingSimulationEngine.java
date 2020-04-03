package osh.simulation;

import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.ConfigurationParameter;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.oc.state.globalobserver.EpsPlsStateExchange;
import osh.simulation.energy.IDeviceEnergySubject;
import osh.simulation.energy.SimEnergySimulationCore;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.Screenplay;
import osh.utils.CostReturnType.SingleStepCostReturnType;
import osh.utils.costs.RegularCostFunction;
import osh.utils.dataStructures.Enum2DoubleMap;
import osh.utils.physics.PhysicalConstants;
import osh.utils.string.ParameterConstants;
import osh.utils.string.StringConversions;
import osh.utils.time.TimeConversion;
import osh.utils.xml.XMLSerialization;

import java.io.PrintWriter;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.Map.Entry;

/**
 * Simulation engine for the smart-home-lab
 *
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class BuildingSimulationEngine extends SimulationEngine {

    public double currentActivePower;
    public double currentReactivePower;
    //logging intervals
    final List<Long[]> loggingIntervals = new ArrayList<>();
    private final UUID entityUUID;
    // Simulation Subjects
    private final ArrayList<ISimulationSubject> simSubjectsList;
    private final HashMap<UUID, ISimulationSubject> simSubjectsMap;
    private final OSHParameterCollection engineParameters;
    private Long[] timeStampForInterval;
    private long[] relativeIntervalStart;
    private boolean databaseLogging;
    private boolean logDetailedPower;
    private boolean logEpsPls;
    private boolean logH0;
    private boolean logIntervals;
    private boolean logDevices;
    private boolean logHotWater;
    private boolean logWaterTank;
    private boolean logGA;
    private boolean logSmartHeater;
    private OSHSimulationResults[] intervalResults;
    //saved EPS and PLS
    private final EnumMap<AncillaryCommodity, PriceSignal> priceSignals = new EnumMap<>(AncillaryCommodity.class);
    private final EnumMap<AncillaryCommodity, PowerLimitSignal> powerLimits = new EnumMap<>(AncillaryCommodity.class);
    //saved power
    private AncillaryCommodityLoadProfile loadProfile = new AncillaryCommodityLoadProfile();
    //saved array for H0
    private final double[][][] aggrH0ResultsWeekdays = new double[7][1440][2];
    private final int[][] h0ResultsCounter = new int[7][1440];
    private final double[][] aggrH0ResultsDays = new double[365][2];
    private final int[] h0ResultsCounterDays = new int[365];
    private double aggrActiveConsumption;
    private double aggrReactiveConsumption;
    // Energy Simulation Core
    private final SimEnergySimulationCore energySimulationCore;
    // ESC Subjects
    private final ArrayList<IDeviceEnergySubject> energySimSubjectsList;
    private final HashMap<UUID, IDeviceEnergySubject> energySimSubjectsMap;
    private final PrintWriter powerWriter;


    /**
     * CONSTRUCTOR<br>
     * constructor with a given array of devices to simulate...yes everything is a device!
     *
     * @param deviceList
     * @param simLogger
     */
    public BuildingSimulationEngine(
            ArrayList<? extends OSHComponent> deviceList,
            List<ConfigurationParameter> engineParameters,
            SimEnergySimulationCore esc,
            ISimulationActionLogger simLogger,
            PrintWriter powerWriter,
            UUID entityUUID) throws SimulationEngineException {

        //LOGGING
        this.oshSimulationResults = new OSHSimulationResults();

        this.energySimulationCore = esc;

        this.simSubjectsList = new ArrayList<>();
        this.simSubjectsMap = new HashMap<>();

        this.energySimSubjectsList = new ArrayList<>();
        this.energySimSubjectsMap = new HashMap<>();

        // get simulation subjects
        try {
            for (OSHComponent _driver : deviceList) {
                if (_driver instanceof ISimulationSubject) {
                    ISimulationSubject _simSubj = (ISimulationSubject) _driver;

                    //assign the simulation engine
                    _simSubj.setSimulationEngine(this);

                    //assign logger
                    _simSubj.setSimulationActionLogger(simLogger);

                    //add subject
                    this.simSubjectsList.add(_simSubj);

                    //do the same for the HashMap (better direct Access)
                    this.simSubjectsMap.put(_simSubj.getUUID(), _simSubj);
                }
            }
        } catch (Exception ex) {
            throw new SimulationEngineException(ex);
        }


        // get ESC simulation subjects
        try {
            for (OSHComponent _driver : deviceList) {
                if (_driver instanceof IDeviceEnergySubject) {
                    IDeviceEnergySubject _simSubj = (IDeviceEnergySubject) _driver;

                    //add subject
                    this.energySimSubjectsList.add(_simSubj);

                    //do the same for the HashMap (better direct Access)
                    this.energySimSubjectsMap.put(_simSubj.getUUID(), _simSubj);
                }
            }
        } catch (Exception ex) {
            throw new SimulationEngineException(ex);
        }

        this.engineParameters = new OSHParameterCollection();
        this.engineParameters.loadCollection(engineParameters);

        this.entityUUID = entityUUID;

        try {
            this.logH0 = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logH0));
        } catch (Exception e) {
            this.logH0 = false;
        }

        try {
            this.logIntervals = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logIntervals));
        } catch (Exception e) {
            this.logIntervals = false;
        }

        try {
            this.logDevices = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logDevices));
        } catch (Exception e) {
            this.logDevices = false;
        }

        try {
            this.logHotWater = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logHotWater));
        } catch (Exception e) {
            this.logHotWater = false;
        }

        try {
            this.logEpsPls = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logEpsPls));
        } catch (Exception e) {
            this.logEpsPls = false;
        }

        try {
            this.logDetailedPower = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logDetailedPower));
        } catch (Exception e) {
            this.logDetailedPower = false;
        }

        try {
            this.logWaterTank = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logWaterTank));
        } catch (Exception e) {
            this.logWaterTank = false;
        }

        try {
            this.logGA = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logGA));
        } catch (Exception e) {
            this.logGA = false;
        }

        try {
            this.logSmartHeater = Boolean.parseBoolean(this.engineParameters.getParameter(ParameterConstants.Logging.logSmartHeater));
        } catch (Exception e) {
            this.logSmartHeater = false;
        }


        String loggingIntervalsAsArray = null;

        try {
            loggingIntervalsAsArray = this.engineParameters.getParameter(ParameterConstants.Logging.loggingIntervals);
        } catch (Exception ignored) {
        }

        if (loggingIntervalsAsArray != null && loggingIntervalsAsArray.length() > 2) {
            Long[][] tmp = StringConversions.fromStringTo2DimLongArray(loggingIntervalsAsArray);
            Collections.addAll(this.loggingIntervals, tmp);
        }


        this.powerWriter = powerWriter;

        if (this.logH0) {
            for (int i = 0; i < this.aggrH0ResultsWeekdays.length; i++) {
                for (int j = 0; j < this.aggrH0ResultsWeekdays[i].length; j++) {
                    this.h0ResultsCounter[i][j] = 0;
                    Arrays.fill(this.aggrH0ResultsWeekdays[i][j], 0.0);
                }
            }
            for (int i = 0; i < this.aggrH0ResultsDays.length; i++) {
                Arrays.fill(this.aggrH0ResultsDays[i], 0.0);
                this.h0ResultsCounterDays[i] = 0;
            }
        }
    }


    // ### SCREENPLAY LEGACY CODE ###
    // INFO: currently not used

    /**
     * load the actions for the devices from a screenplay-object for a timespan
     *
     * @param currentScreenplay
     */
    public void loadSingleScreenplay(Screenplay currentScreenplay) {
        for (ISimulationSubject _simSubj : this.simSubjectsList) {
            for (int i = 0; i < currentScreenplay.getSIMActions().size(); i++) {
                //Search for an action for a specific device
                if (currentScreenplay.getSIMActions().get(i).getDeviceID().compareTo(_simSubj.getUUID().toString()) == 0) {
                    _simSubj.setAction(currentScreenplay.getSIMActions().get(i));
                }
            }
        }
    }

    /**
     * @param screenPlaySource the actions for the devices for a timespan or a cycle from a file
     */
    public void loadSingleScreenplayFromFile(String screenPlaySource) throws SimulationEngineException {
        Screenplay currentScreenplaySet;
        try {
            currentScreenplaySet = (Screenplay) XMLSerialization.file2Unmarshal(screenPlaySource, Screenplay.class);
        } catch (Exception ex) {
            throw new SimulationEngineException(ex);
        }
        this.loadSingleScreenplay(currentScreenplaySet);
    }


    /**
     * simulate the next timeTick, increment the real-time driver
     *
     * @param currentTick
     */
    @Override
    public void simulateNextTimeTick(long currentTick) {

        //		Map<AncillaryCommodity,AncillaryCommodityState> ancillaryMeterState;
        AncillaryMeterState ancillaryMeterState;

        // #1 EnergySimulation
        ancillaryMeterState = this.energySimulationCore.doNextEnergySimulation(this.energySimSubjectsList);

        // #2 Notify the Subject that the next Simulation Tick begins
        //    Simulation Pre-tick Hook
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.onSimulationPreTickHook();
        }

        // #3 DeviceSimulation (the Tick)
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.triggerSubject();
        }

        // #4 Notify the Subject that the current Simulation Tick ended
        //	  Simulation Post-tick Hook
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.onSimulationPostTickHook();
        }

        // TEMP LOGGING
        this.logTick(ancillaryMeterState, currentTick);
    }

    @Override
    protected void notifyLocalEngineOnSimulationIsUp() throws SimulationEngineException {
        try {
            for (ISimulationSubject simulationSubject : this.simSubjectsList) {
                simulationSubject.onSimulationIsUp();
            }
        } catch (SimulationSubjectException ex) {
            throw new SimulationEngineException(ex);
        }

    }


    // ### GETTERS ###

    /**
     * get a simulationSubject by his (device) ID.
     * This can be called from another subject to get an appending subject
     *
     * @param subjectID
     * @return
     */
    protected ISimulationSubject getSimulationSubjectByID(UUID subjectID) {
        ISimulationSubject _simSubj;
        _simSubj = this.simSubjectsMap.get(subjectID);
        return _simSubj;
    }

    public void setDatabaseLogging() {
        this.databaseLogging = true;

        this.intervalResults = new OSHSimulationResults[this.loggingIntervals.size()];
        this.timeStampForInterval = new Long[this.loggingIntervals.size()];
        this.relativeIntervalStart = new long[this.loggingIntervals.size()];
        for (int i = 0; i < this.loggingIntervals.size(); i++) {
            this.intervalResults[i] = new OSHSimulationResults();
            this.timeStampForInterval[i] = null;
            this.relativeIntervalStart[i] = 0;
        }

        DatabaseLoggerThread.setLogDevices(this.logDevices);
        DatabaseLoggerThread.setLogHotWater(this.logHotWater);
        DatabaseLoggerThread.setLogWaterTank(this.logWaterTank);
        DatabaseLoggerThread.setLogGA(this.logGA);
        DatabaseLoggerThread.setLogSmartHeater(this.logSmartHeater);
    }


    // ### GETTERS ###
    //NONE

    // LOGGING

    private void logTick(
            AncillaryMeterState ancillaryMeterState,
            long currentTick) {

        // GET EPS and PLS FROM REGISTRY
        EpsPlsStateExchange epse = (EpsPlsStateExchange) this.ocRegistry.getData(
                EpsPlsStateExchange.class,
                UUID.fromString("e5ad4b36-d417-4be6-a1c8-c3ad68e52977"));

        if (ancillaryMeterState != null
                && epse != null) {

            double currentActivePowerConsumption = 0;

            double currentActivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);
            if (currentActivePowerExternal > 0) {
                currentActivePowerConsumption = currentActivePowerExternal;
            }

            double currentActivePowerPvAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerPvFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.PVACTIVEPOWERFEEDIN);

            double currentActivePowerPv = currentActivePowerPvFeedIn + currentActivePowerPvAutoConsumption;
            currentActivePowerConsumption += Math.abs(currentActivePowerPvAutoConsumption);


            double currentActivePowerChpAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerChpFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);

            double currentActivePowerChp = currentActivePowerChpFeedIn + currentActivePowerChpAutoConsumption;
            currentActivePowerConsumption += Math.abs(currentActivePowerChpAutoConsumption);


            double currentActivePowerBatteryCharging = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERCONSUMPTION);
            double currentActivePowerBatteryAutoConsumption = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERAUTOCONSUMPTION);
            double currentActivePowerBatteryFeedIn = ancillaryMeterState.getPower(AncillaryCommodity.BATTERYACTIVEPOWERFEEDIN);

            double currentActivePowerBatteryDischarging = currentActivePowerBatteryFeedIn + currentActivePowerBatteryAutoConsumption;

            //if battery is charging it's power is already contained in the activePowerExternal
            if (currentActivePowerBatteryAutoConsumption < 0)
                currentActivePowerConsumption += Math.abs(currentActivePowerBatteryAutoConsumption);

            double currentReactivePowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.REACTIVEPOWEREXTERNAL);

//			if (currentReactivePowerExternal != 0) {
//				@SuppressWarnings("unused")
//				int xxx = 0;
//			}

            double currentGasPowerExternal = ancillaryMeterState.getPower(AncillaryCommodity.NATURALGASPOWEREXTERNAL);

            long currentTime = this.timeDriver.getCurrentEpochSecond();
            ZonedDateTime now = this.timeDriver.getCurrentTime();

            RegularCostFunction costFunction = new RegularCostFunction(epse.getPlsOverLimitFactor(),
                    epse.getCostConfiguration(),
                    epse.getPs(),
                    epse.getPwrLimit(),
                    currentTime);

            Enum2DoubleMap<SingleStepCostReturnType> costs =
                    costFunction.calculateSingleStepCosts(ancillaryMeterState, currentTime, 1L);

            if (this.oshSimulationResults != null
                    && this.oshSimulationResults instanceof OSHSimulationResults) {
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerConsumption(
                        currentActivePowerConsumption);

                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerPV(
                        currentActivePowerPv);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerPVAutoConsumption(
                        currentActivePowerPvAutoConsumption);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerPVFeedIn(
                        currentActivePowerPvFeedIn);

                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerCHP(
                        currentActivePowerChp);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerCHPAutoConsumption(
                        currentActivePowerChpAutoConsumption);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerCHPFeedIn(
                        currentActivePowerChpFeedIn);

                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerBatteryCharging(
                        currentActivePowerBatteryCharging);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerBatteryDischarging(
                        currentActivePowerBatteryDischarging);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerBatteryAutoConsumption(
                        currentActivePowerBatteryAutoConsumption);
                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerBatteryFeedIn(
                        currentActivePowerBatteryFeedIn);

                ((OSHSimulationResults) this.oshSimulationResults).addActivePowerExternal(
                        currentActivePowerExternal);

                ((OSHSimulationResults) this.oshSimulationResults).addReactivePowerExternal(
                        currentReactivePowerExternal);

                ((OSHSimulationResults) this.oshSimulationResults).addGasPowerExternal(
                        currentGasPowerExternal);

                ((OSHSimulationResults) this.oshSimulationResults).addEpsCostsToEpsCosts(costs.get(SingleStepCostReturnType.EPS));
                ((OSHSimulationResults) this.oshSimulationResults).addPlsCostsToPlsCosts(costs.get(SingleStepCostReturnType.PLS));
                ((OSHSimulationResults) this.oshSimulationResults).addCostsToTotalCosts(
                        costs.get(SingleStepCostReturnType.EPS)
                        + costs.get(SingleStepCostReturnType.PLS)
                        + costs.get(SingleStepCostReturnType.GAS)
                        + costs.get(SingleStepCostReturnType.FEED_IN_PV)
                        + costs.get(SingleStepCostReturnType.FEED_IN_CHP)
                        + costs.get(SingleStepCostReturnType.AUTO_CONSUMPTION));
                ((OSHSimulationResults) this.oshSimulationResults).addGasCostsToGasCosts(costs.get(SingleStepCostReturnType.GAS));
                ((OSHSimulationResults) this.oshSimulationResults).addFeedInCostsToFeedInCostsPV(costs.get(SingleStepCostReturnType.FEED_IN_PV));
                ((OSHSimulationResults) this.oshSimulationResults).addFeedInCostsToFeedInCostsCHP(costs.get(SingleStepCostReturnType.FEED_IN_CHP));
                ((OSHSimulationResults) this.oshSimulationResults).addAutoConsumptionCostsToAutoConsumptionCosts(costs.get(SingleStepCostReturnType.AUTO_CONSUMPTION));
                // GAS COSTS
            }

            this.currentActivePower = currentActivePowerExternal;
            this.currentReactivePower = currentReactivePowerExternal;

            if (this.databaseLogging && currentTick > 1 && this.logIntervals) {
                //interval logging of values
                if (!this.loggingIntervals.isEmpty()) {
                    for (int i = 0; i < this.loggingIntervals.size(); i++) {
                        //set up the next timestamps at start of the simulation
                        if (this.timeStampForInterval[i] == null) {
                            Long[] interval = this.loggingIntervals.get(i);
                            if (interval[0] > 0) {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfMonth(now.plusMonths(interval[0])).toEpochSecond();
                            } else if (interval[1] > 0) {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfWeek(now.plusWeeks(interval[1])).toEpochSecond();
                            } else {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfDay(now.plusDays(interval[2])).toEpochSecond();
                            }
                        } else if (this.timeStampForInterval[i] <= currentTime) {
                            Long[] interval = this.loggingIntervals.get(i);
                            OSHSimulationResults newBase = ((OSHSimulationResults) this.oshSimulationResults).clone();
                            OSHSimulationResults toLog = this.intervalResults[i];
                            this.intervalResults[i] = newBase;
                            toLog.generateDiffToOtherResult((OSHSimulationResults) this.oshSimulationResults);
                            DatabaseLoggerThread.enqueueSimResults(toLog, this.relativeIntervalStart[i], currentTick);

                            this.relativeIntervalStart[i] = currentTick + 1;
                            if (interval[0] > 0) {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfMonth(now.plusMonths(interval[0])).toEpochSecond();
                            } else if (interval[1] > 0) {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfWeek(now.plusWeeks(interval[1])).toEpochSecond();
                            } else {
                                this.timeStampForInterval[i] =
                                        TimeConversion.getStartOfDay(now.plusDays(interval[2])).toEpochSecond();
                            }
                        }
                    }
                }
            }

            if (this.databaseLogging && this.logEpsPls) {
                //new eps/pls, so save the past and update the saved one
                if (epse.isEpsPlsChanged()) {
                    //handle eps
                    Map<AncillaryCommodity, PriceSignal> toLogEps = new EnumMap<>(AncillaryCommodity.class);
                    for (Entry<AncillaryCommodity, PriceSignal> en : epse.getPs().entrySet()) {
                        PriceSignal oldPs = this.priceSignals.get(en.getKey());

                        if (oldPs == null) {
                            this.priceSignals.put(en.getKey(), en.getValue().clone());
                        } else {
                            oldPs.extendAndOverride(en.getValue().clone());
                            //log past
                            toLogEps.put(en.getKey(), oldPs.cloneBefore(currentTime));
                            //only keep the future
                            PriceSignal nowAndFuture = oldPs.cloneAfter(currentTime);
                            this.priceSignals.put(en.getKey(), nowAndFuture);
                        }
                    }
                    DatabaseLoggerThread.enqueueEps(toLogEps);

                    //handle pls
                    Map<AncillaryCommodity, PowerLimitSignal> toLogPls = new EnumMap<>(AncillaryCommodity.class);
                    for (Entry<AncillaryCommodity, PowerLimitSignal> en : epse.getPwrLimit().entrySet()) {
                        PowerLimitSignal oldPls = this.powerLimits.get(en.getKey());

                        if (oldPls == null) {
                            this.powerLimits.put(en.getKey(), en.getValue().clone());
                        } else {
                            oldPls.extendAndOverride(en.getValue().clone());
                            //log past
                            toLogPls.put(en.getKey(), oldPls.cloneBefore(currentTime));
                            //only keep the future
                            PowerLimitSignal nowAndFuture = oldPls.cloneAfter(currentTime);
                            this.powerLimits.put(en.getKey(), nowAndFuture);
                        }
                    }
                    DatabaseLoggerThread.enqueuePls(toLogPls);
                }
            }

            if (this.databaseLogging && this.logDetailedPower) {

                this.loadProfile.setLoad(AncillaryCommodity.ACTIVEPOWEREXTERNAL, currentTick, (int) Math.round(currentActivePowerExternal));
                this.loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION, currentTick, (int) Math.round(currentActivePowerChpAutoConsumption));
                this.loadProfile.setLoad(AncillaryCommodity.CHPACTIVEPOWERFEEDIN, currentTick, (int) Math.round(currentActivePowerChpFeedIn));
                this.loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION, currentTick, (int) Math.round(currentActivePowerPvAutoConsumption));
                this.loadProfile.setLoad(AncillaryCommodity.PVACTIVEPOWERFEEDIN, currentTick, (int) Math.round(currentActivePowerPvFeedIn));

                if (currentTick % 43200 == 0) {
                    AncillaryCommodityLoadProfile tmpLoadProfile = this.loadProfile;
                    this.loadProfile = tmpLoadProfile.cloneAfter(currentTick);

                    tmpLoadProfile = tmpLoadProfile.getProfileWithoutDuplicateValues();
                    DatabaseLoggerThread.enqueueDetailedPower(tmpLoadProfile);
                }
            }

            if (this.databaseLogging && this.logH0) {
                this.aggrActiveConsumption += currentActivePowerConsumption;
                this.aggrReactiveConsumption += currentReactivePowerExternal;


                if (currentTick % 60 == 0) {
                    int dayOfWeek = TimeConversion.getCorrectedDayOfWeek(now);
                    int dayOfYear = TimeConversion.getCorrectedDayOfYear(now);
                    int minuteOfDay = TimeConversion.getMinutesSinceDayStart(now);
                    this.aggrH0ResultsWeekdays[dayOfWeek][minuteOfDay][0] += this.aggrActiveConsumption;
                    this.aggrH0ResultsWeekdays[dayOfWeek][minuteOfDay][1] += this.aggrReactiveConsumption;
                    this.h0ResultsCounter[dayOfWeek][minuteOfDay]++;
                    this.aggrH0ResultsDays[dayOfYear % 365][0] += this.aggrActiveConsumption;
                    this.aggrH0ResultsDays[dayOfYear % 365][1] += this.aggrReactiveConsumption;
                    this.h0ResultsCounterDays[dayOfYear % 365]++;

                    this.aggrActiveConsumption = 0;
                    this.aggrReactiveConsumption = 0;
                }
            }

            //System.out.println("currentActivePower: " + ((OSHSimulationResults)oshSimulationResults).activePowerExternal);
            //System.out.println("currentReactivePower: " + currentReactivePowerExternal);

            // MINUTEWISE POWER LOGGER
            if (currentTick % 60 == 0
                    && this.powerWriter != null) {
                this.powerWriter.println(currentTick
                        + ";" + currentActivePowerConsumption
                        + ";" + currentActivePowerPv
                        + ";" + currentActivePowerPvAutoConsumption
                        + ";" + currentActivePowerPvFeedIn
                        + ";" + currentActivePowerChp
                        + ";" + currentActivePowerChpAutoConsumption
                        + ";" + currentActivePowerChpFeedIn
                        + ";" + currentActivePowerBatteryCharging
                        + ";" + currentActivePowerBatteryDischarging
                        + ";" + currentActivePowerBatteryAutoConsumption
                        + ";" + currentActivePowerBatteryFeedIn
                        + ";" + currentActivePowerExternal
                        + ";" + currentReactivePowerExternal
                        + ";" + currentGasPowerExternal
                        + ";" + costs.get(SingleStepCostReturnType.EPS)
                        + ";" + costs.get(SingleStepCostReturnType.PLS)
                        + ";" + costs.get(SingleStepCostReturnType.GAS)
                        + ";" + costs.get(SingleStepCostReturnType.FEED_IN_PV)
                        + ";" + costs.get(SingleStepCostReturnType.FEED_IN_CHP)
                        + ";" + costs.get(SingleStepCostReturnType.AUTO_CONSUMPTION)
                        + ";" + epse.getPs().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPrice(currentTime));
            }

            //			if (comRegistry != null) {
            //				if (entityUUID != null) {
            //					System.out.println("SimEngine Daten abschicken");
            //					System.out.println(entityUUID);
            //					comRegistry.setStateOfSender(BuildingStateExchange.class,
            //							new BuildingStateExchange(entityUUID, currentTick, currentTime, currentActivePower,
            //									currentActivePowerConsumption, currentActivePowerChp, currentActivePowerChpFeedIn,
            //									currentActivePowerChpAutoConsumption, currentActivePowerPv,
            //									currentActivePowerPvFeedIn, currentActivePowerPvAutoConsumption,
            //									currentActivePowerBatteryCharging, currentActivePowerBatteryDischarging,
            //									currentActivePowerBatteryAutoConsumption, currentActivePowerBatteryFeedIn,
            //									currentActivePowerExternal, currentReactivePowerExternal, currentGasPowerExternal));
            //
            //				}
            //			}

        }  //			System.out.println("ERROR");
    }

    public void shutdown() {
        if (this.databaseLogging) {
            if (this.logIntervals) {
                DatabaseLoggerThread.enqueueEps(this.priceSignals);
                DatabaseLoggerThread.enqueuePls(this.powerLimits);
            }
            if (this.logH0) {
                double[][][] avgWeekDays = new double[7][1440][2];
                double[][] avgDays = new double[365][2];

                for (int i = 0; i < avgWeekDays.length; i++) {
                    for (int j = 0; j < avgWeekDays[i].length; j++) {
                        double factor = this.h0ResultsCounter[i][j] * PhysicalConstants.factor_wsToKWh;
                        for (int k = 0; k < avgWeekDays[i][j].length; k++) {
                            avgWeekDays[i][j][k] = this.aggrH0ResultsWeekdays[i][j][k] / factor;
                        }
                    }
                }
                for (int i = 0; i < avgDays.length; i++) {
                    double factor = (this.h0ResultsCounterDays[i] / 1440.0) * PhysicalConstants.factor_wsToKWh;
                    for (int j = 0; j < avgDays[i].length; j++) {
                        avgDays[i][j] = this.aggrH0ResultsDays[i][j] / factor;
                    }
                }

                DatabaseLoggerThread.enqueueH0(avgWeekDays, avgDays);
            }
        }
    }
}
