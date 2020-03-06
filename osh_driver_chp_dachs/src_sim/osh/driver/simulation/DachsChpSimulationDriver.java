package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.driver.chp.ChpOperationMode;
import osh.driver.chp.model.GenericChpModel;
import osh.eal.hal.exceptions.HALException;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.esc.LimitedCommodityStateMap;
import osh.hal.exchange.ChpControllerExchange;
import osh.hal.exchange.ChpObserverExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.simulation.DatabaseLoggerThread;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.physics.PhysicalConstants;
import osh.utils.string.ParameterConstants;

import java.time.Duration;
import java.util.UUID;

/**
 * Simulates the Dachs microCHP
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class DachsChpSimulationDriver
        extends ChpSimulationDriver {

    // static values
    private double cosPhi;
    private int typicalActivePower;
    private int typicalReactivePower;
    private int typicalThermalPower;
    private int typicalAdditionalThermalPower;
    private int typicalGasPower;

    @SuppressWarnings("unused")
    private double typicalTemperatureIn = 45.0;
    private final double typicalTemperatureOut = 85.0;
    private final double typicalMassFlow = 1;
    private UUID hotWaterTankUuid;

    private Duration rescheduleAfter;
    private Duration newIPPAfter;
    private int relativeHorizonIPP;
    private long timePerSlot;
    private int bitsPerSlot;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double forcedOnHysteresis;

    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;
    private int minRuntime;

    //private final double standbyActivePower = 20.0;

    // real variables
    @SuppressWarnings("unused")
    private double currentTemperatureIn;
    private double currentTemperatureOut;
    private double currentMassFlow;

    private boolean electricityRequest;
    private boolean heatingRequest;
    private Duration runtimeRemaining;
    @SuppressWarnings("unused")
    private int offTimeRemaining;

    private boolean runningRequestFromController;

    // received from other devices...
    private double waterInTemperature = 60.0;

    private GenericChpModel chpModel;

    private double supply;
    private int starts;
    private boolean log;


    /**
     * CONSTRUCTOR
     */
    public DachsChpSimulationDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws HALException {
        super(osh, deviceID, driverConfig);

        try {
            this.typicalActivePower = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.activePower));
        } catch (Exception e) {
            this.typicalActivePower = -5500;
            this.getGlobalLogger().logWarning("Can't get typicalActivePower, using the default value: " + this.typicalActivePower);
        }

        try {
            this.typicalThermalPower = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.thermalPower));
        } catch (Exception e) {
            this.typicalThermalPower = -12500;
            this.getGlobalLogger().logWarning("Can't get typicalThermalPower, using the default value: " + this.typicalThermalPower);
        }

        try {
            this.typicalAdditionalThermalPower =
                    Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.additionalThermalPower));
        } catch (Exception e) {
            this.typicalAdditionalThermalPower = 0;
            this.getGlobalLogger().logWarning("Can't get typicalAdditionalThermalPower, using the default value: " + this.typicalAdditionalThermalPower);
        }

        try {
            this.typicalGasPower = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.gasPower));
        } catch (Exception e) {
            this.typicalGasPower = 20500;
            this.getGlobalLogger().logWarning("Can't get typicalGasPower, using the default value: " + this.typicalGasPower);
        }

        try {
            this.hotWaterTankUuid = UUID.fromString(this.getDriverConfig().getParameter(ParameterConstants.CHP.hotWaterTankUUID));
        } catch (Exception e) {
            this.hotWaterTankUuid = UUID.fromString("00000000-0000-4857-4853-000000000000");
            this.getGlobalLogger().logWarning("Can't get hotWaterTankUuid, using the default value: " + this.hotWaterTankUuid);
        }

        try {
            this.rescheduleAfter = Duration.ofSeconds(Integer.parseInt(this.getDriverConfig().getParameter(
                    ParameterConstants.IPP.rescheduleAfter)));
        } catch (Exception e) {
            this.rescheduleAfter = Duration.ofHours(4);
            this.getGlobalLogger().logWarning("Can't get rescheduleAfter, using the default value: " + this.rescheduleAfter);
        }

        try {
            this.newIPPAfter =
                    Duration.ofSeconds(Long.parseLong(this.getDriverConfig().getParameter(ParameterConstants.IPP.newIPPAfter)));
        } catch (Exception e) {
            this.newIPPAfter = Duration.ofHours(1);
            this.getGlobalLogger().logWarning("Can't get newIPPAfter, using the default value: " + this.newIPPAfter);
        }

        try {
            this.relativeHorizonIPP = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.IPP.relativeHorizon));
        } catch (Exception e) {
            this.relativeHorizonIPP = 18 * 3600; // 18 hours
            this.getGlobalLogger().logWarning("Can't get relativeHorizonIPP, using the default value: " + this.relativeHorizonIPP);
        }

        try {
            this.timePerSlot = Long.parseLong(this.getDriverConfig().getParameter(ParameterConstants.IPP.timePerSlot));
        } catch (Exception e) {
            this.timePerSlot = 5 * 60;
            this.getGlobalLogger().logWarning("Can't get timePerSlot, using the default value: " + this.timePerSlot);
        }

        try {
            this.bitsPerSlot = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.IPP.bitsPerSlot));
        } catch (Exception e) {
            this.bitsPerSlot = 4;
            this.getGlobalLogger().logWarning("Can't get bitsPerSlot, using the default value: " + this.bitsPerSlot);
        }

        try {
            this.currentHotWaterStorageMinTemp =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.TemperatureRestrictions.hotWaterStorageMinTemp));
        } catch (Exception e) {
            this.currentHotWaterStorageMinTemp = 60;
            this.getGlobalLogger().logWarning("Can't get currentHotWaterStorageMinTemp, using the default value: " + this.currentHotWaterStorageMinTemp);
        }

        try {
            this.currentHotWaterStorageMaxTemp =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.TemperatureRestrictions.hotWaterStorageMaxTemp));
        } catch (Exception e) {
            this.currentHotWaterStorageMaxTemp = 80;
            this.getGlobalLogger().logWarning("Can't get currentHotWaterStorageMaxTemp, using the default value: " + this.currentHotWaterStorageMaxTemp);
        }

        try {
            this.forcedOnHysteresis =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.TemperatureRestrictions.forcedOnHysteresis));
        } catch (Exception e) {
            this.forcedOnHysteresis = 5.0;
            this.getGlobalLogger().logWarning("Can't get forcedOnHysteresis, using the default value: " + this.forcedOnHysteresis);
        }

        try {
            this.fixedCostPerStart = Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.CHP.fixedCostPerStart));
        } catch (Exception e) {
            this.fixedCostPerStart = 8.0;
            this.getGlobalLogger().logWarning("Can't get fixedCostPerStart, using the default value: " + this.fixedCostPerStart);
        }

        try {
            this.forcedOnOffStepMultiplier =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.CHP.forcedOnOffStepMultiplier));
        } catch (Exception e) {
            this.forcedOnOffStepMultiplier = 0.1;
            this.getGlobalLogger().logWarning("Can't get forcedOnOffStepMultiplier, using the default value: " + this.forcedOnOffStepMultiplier);
        }

        try {
            this.forcedOffAdditionalCost =
                    Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.forcedOffAdditionalCost));
        } catch (Exception e) {
            this.forcedOffAdditionalCost = 10;
            this.getGlobalLogger().logWarning("Can't get forcedOffAdditionalCost, using the default value: " + this.forcedOffAdditionalCost);
        }

        try {
            this.chpOnCervisiaStepSizeMultiplier =
                    Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.CHP.cervisiaStepSizeMultiplier));
        } catch (Exception e) {
            this.chpOnCervisiaStepSizeMultiplier = 0.0000001;
            this.getGlobalLogger().logWarning("Can't get chpOnCervisiaStepSizeMultiplier, using the default value: " + this.chpOnCervisiaStepSizeMultiplier);
        }

        try {
            this.minRuntime = Integer.parseInt(this.getDriverConfig().getParameter(ParameterConstants.CHP.minRuntime));
        } catch (Exception e) {
            this.minRuntime = 15 * 60;
            this.getGlobalLogger().logWarning("Can't get minRuntime, using the default value: " + this.minRuntime);
        }

        try {
            this.cosPhi = Double.parseDouble(this.getDriverConfig().getParameter(ParameterConstants.CHP.cosPhi));
        } catch (Exception e) {
            this.cosPhi = 0.9;
            this.getGlobalLogger().logWarning("Can't get cosPhi, using the default value: " + this.cosPhi);
        }

        try {
            this.typicalReactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(this.typicalActivePower, this.cosPhi, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.chpModel = new GenericChpModel(
                this.typicalActivePower,
                this.typicalReactivePower,
                this.typicalThermalPower,
                this.typicalGasPower,
                this.cosPhi,
                false,
                0,
                null,
                null);
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();

        ChpStaticDetailsObserverExchange observerExchange =
                new ChpStaticDetailsObserverExchange(this.getUUID(), this.getTimeDriver().getCurrentTime());
        observerExchange.setTypicalActivePower(this.typicalActivePower);
        observerExchange.setTypicalReactivePower(this.typicalReactivePower);
        observerExchange.setTypicalThermalPower(this.typicalThermalPower);
        observerExchange.setTypicalGasPower(this.typicalGasPower);
        observerExchange.setOperationMode(ChpOperationMode.HEAT_AND_ELECTRICITY_LED);
        observerExchange.setHotWaterTankUuid(this.hotWaterTankUuid);
        observerExchange.setRescheduleAfter(this.rescheduleAfter);
        observerExchange.setNewIPPAfter(this.newIPPAfter);
        observerExchange.setRelativeHorizonIPP(this.relativeHorizonIPP);
        observerExchange.setTimePerSlot(this.timePerSlot);
        observerExchange.setBitsPerSlot(this.bitsPerSlot);
        observerExchange.setCurrentHotWaterStorageMinTemp(this.currentHotWaterStorageMinTemp);
        observerExchange.setCurrentHotWaterStorageMaxTemp(this.currentHotWaterStorageMaxTemp);
        observerExchange.setForcedOnHysteresis(this.forcedOnHysteresis);

        observerExchange.setFixedCostPerStart(this.fixedCostPerStart);
        observerExchange.setForcedOnOffStepMultiplier(this.forcedOnOffStepMultiplier);
        observerExchange.setForcedOffAdditionalCost(this.forcedOffAdditionalCost);
        observerExchange.setChpOnCervisiaStepSizeMultiplier(this.chpOnCervisiaStepSizeMultiplier);
        observerExchange.setMinRuntime(this.minRuntime);

        this.notifyObserver(observerExchange);

        this.log = DatabaseLoggerThread.isLogWaterTank();
    }


    @Override
    public void onSystemShutdown() {
        if (this.log) {
            this.supply /= PhysicalConstants.factor_wsToKWh;
            DatabaseLoggerThread.enqueueChp(this.supply, this.starts);
        }
    }


    @Override
    public void onNextTimeTick() {

        // do device control
        if (this.electricityRequest || this.heatingRequest || this.runningRequestFromController) {
            if (!this.isRunning()) {
                if (this.runningRequestFromController) {
                    this.runtimeRemaining = Duration.ofMinutes(30);
                } else {
                    this.runtimeRemaining = Duration.ZERO;
                }
            } else {
                if (this.runtimeRemaining.compareTo(Duration.ZERO) > 0) {
                    this.runtimeRemaining = this.runtimeRemaining.minusSeconds(1);
                }
            }
            this.setRunning(true);
        } else {
            this.setRunning(false);
            this.runtimeRemaining = Duration.ZERO;
        }

        this.chpModel.calcPower(this.getTimeDriver().getCurrentEpochSecond());
        int activePower = this.chpModel.getActivePower();
        int reactivePower = this.chpModel.getReactivePower();
        int thermalPower = this.chpModel.getThermalPower();
        int gasPower = this.chpModel.getGasPower();
        this.setPower(Commodity.ACTIVEPOWER, activePower);
        this.setPower(Commodity.REACTIVEPOWER, reactivePower);
        this.setPower(Commodity.HEATINGHOTWATERPOWER, thermalPower);
        this.setPower(Commodity.NATURALGASPOWER, gasPower);

        if (this.log) {
            this.supply += thermalPower;
        }

        // set power
        if (this.isRunning()) {
            // calculate mass flow
            //TODO
            this.currentMassFlow = this.typicalMassFlow;

            this.currentTemperatureIn = this.waterInTemperature;
            this.currentTemperatureOut = this.typicalTemperatureOut;
        } else {
            this.currentMassFlow = 0;
            this.currentTemperatureIn = 0;
            this.currentTemperatureOut = 0;
        }

        // send ObserverExchange
        ChpObserverExchange observerExchange = new ChpObserverExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime());

        observerExchange.setActivePower(Math.round(this.getPower(Commodity.ACTIVEPOWER)));
        observerExchange.setReactivePower(Math.round(this.getPower(Commodity.REACTIVEPOWER)));
        observerExchange.setThermalPower(Math.round(this.getPower(Commodity.HEATINGHOTWATERPOWER)));
        observerExchange.setGasPower(Math.round(this.getPower(Commodity.NATURALGASPOWER)));

        observerExchange.setTemperatureOut(this.currentTemperatureOut);
        observerExchange.setElectricityRequest(this.electricityRequest || this.runningRequestFromController);
        observerExchange.setHeatingRequest(this.heatingRequest);
        observerExchange.setRunning(this.isRunning());
        observerExchange.setMinRuntimeRemaining(this.runtimeRemaining);
        this.notifyObserver(observerExchange);

    }


    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest)
            throws HALException {
        super.onControllerRequest(controllerRequest);

        ChpControllerExchange cx = (ChpControllerExchange) controllerRequest;
        boolean stop = cx.isStopGenerationFlag();
        boolean er = cx.isElectricityRequest();
        boolean hr = cx.isHeatingRequest();

        if (stop) {
            // actually not possible with Dachs...
            this.runningRequestFromController = false;
            this.electricityRequest = false;
            this.heatingRequest = false;
        } else {
            this.runningRequestFromController = er || hr;
        }
    }


    @Override
    protected void setRunning(boolean running) {
        // for logging
        if (this.log && running != this.isRunning() && running) {
            this.starts++;
        }
        super.setRunning(running);
        this.chpModel.setRunning(running, this.getTimeDriver().getCurrentEpochSecond());
    }

    @Override
    public LimitedCommodityStateMap getCommodityOutputStates() {
//		EnumMap<Commodity, RealCommodityState> map = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
        LimitedCommodityStateMap map = new LimitedCommodityStateMap(this.usedCommodities);
//		map.put(
//				Commodity.ACTIVEPOWER, 
//				new RealElectricalCommodityState(
//						Commodity.ACTIVEPOWER, 
//						this.getPower(Commodity.ACTIVEPOWER) != null 
//								? (double) this.getPower(Commodity.ACTIVEPOWER) 
//								: 0.0, 
//						null));
//		map.put(
//				Commodity.REACTIVEPOWER, 
//				new RealElectricalCommodityState(
//						Commodity.REACTIVEPOWER, 
//						this.getPower(Commodity.REACTIVEPOWER) != null 
//								? (double) this.getPower(Commodity.REACTIVEPOWER)
//								: 0.0,
//						null));
//		map.put(
//				Commodity.HEATINGHOTWATERPOWER,
//				new RealThermalCommodityState(
//						Commodity.HEATINGHOTWATERPOWER, 
//						this.getPower(Commodity.HEATINGHOTWATERPOWER) != null 
//								? (double) this.getPower(Commodity.HEATINGHOTWATERPOWER)
//								: 0.0, 
//						this.currentTemperatureOut, 
//						this.currentMassFlow));
//		map.put(
//				Commodity.NATURALGASPOWER,
//				new RealThermalCommodityState(
//						Commodity.NATURALGASPOWER, 
//						this.getPower(Commodity.NATURALGASPOWER) != null 
//								? (double) this.getPower(Commodity.NATURALGASPOWER)
//								: 0.0, 
//						0.0, 
//						null));
        map.setPower(Commodity.ACTIVEPOWER, this.getPower(Commodity.ACTIVEPOWER) != null
                ? (double) this.getPower(Commodity.ACTIVEPOWER) : 0.0);

        map.setPower(Commodity.REACTIVEPOWER, this.getPower(Commodity.REACTIVEPOWER) != null
                ? (double) this.getPower(Commodity.REACTIVEPOWER) : 0.0);

        map.setAllThermal(Commodity.HEATINGHOTWATERPOWER, this.getPower(Commodity.HEATINGHOTWATERPOWER) != null
                        ? (double) this.getPower(Commodity.HEATINGHOTWATERPOWER) : 0.0,
                new double[]{this.currentTemperatureOut, this.currentMassFlow});

        map.setPower(Commodity.NATURALGASPOWER, this.getPower(Commodity.NATURALGASPOWER) != null
                ? (double) this.getPower(Commodity.NATURALGASPOWER) : 0.0);
        return map;
    }


    @Override
    public void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
//			EnumMap<AncillaryCommodity,AncillaryCommodityState> ancillaryInputStates) {
            AncillaryMeterState ancillaryMeterState) {
        super.setCommodityInputStates(inputStates, ancillaryMeterState);
        // TODO temperature in (later...)
        if (inputStates != null) {
            if (inputStates.containsCommodity(Commodity.HEATINGHOTWATERPOWER)) {
//				RealCommodityState cs = inputStates.get(Commodity.HEATINGHOTWATERPOWER);
//				RealThermalCommodityState tcs = (RealThermalCommodityState) cs;
                this.waterInTemperature = inputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);
//				}
            }
        }
    }


    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }

}
