package osh.driver;

import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.commands.ChpElectricityRequest;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChpControllerExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.utils.physics.ComplexPowerUtil;

import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser, Jan Mueller
 */
public abstract class DachsChpDriver extends ChpDriver
{

    private String dachsURL;

    //static values
    private double cosPhi;
    private int typicalActivePower;
    private int typicalReactivePower;
    private int typicalThermalPower;
    private int typicalAdditionalThermalPower;
    private int typicalGasPower;

    //	private double typicalTemperatureIn = 45.0;
//	private double typicalTemperatureOut = 85.0;
//	private double typicalMassFlow = 1;
    private UUID hotWaterTankUuid;

    private int rescheduleAfter;
    private long newIPPAfter;
    private int relativeHorizonIPP;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double forcedOnHysteresis;

    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;
    private int minRuntime;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

//	private final double standbyActivePower = 20.0;

//	private double currentTemperatureIn = 0.0;
//	private double currentTemperatureOut = 0.0;
//	private double currentMassFlow = 0.0;

//	private int runtimeRemaining = 0;
//	private int offtimeRemaining = 0;

//	private boolean runningRequestFromController = false;

    // received from other devices...
//	private double waterInTemperature = 60.0;


    /**
     * CONSTRUCTOR
     */
    public DachsChpDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws OSHException {
        super(osh, deviceID, driverConfig);

        try {
            this.typicalActivePower = Integer.parseInt(this.getDriverConfig().getParameter("typicalActivePower"));
        } catch (Exception e) {
            this.typicalActivePower = -5500;
            this.getGlobalLogger().logWarning("Can't get typicalActivePower, using the default value: " + this.typicalActivePower);
        }

        try {
            this.typicalThermalPower = Integer.parseInt(this.getDriverConfig().getParameter("typicalThermalPower"));
        } catch (Exception e) {
            this.typicalThermalPower = -12500;
            this.getGlobalLogger().logWarning("Can't get typicalThermalPower, using the default value: " + this.typicalThermalPower);
        }

        try {
            this.typicalAdditionalThermalPower = Integer.parseInt(this.getDriverConfig().getParameter("typicalAddditionalThermalPower"));
        } catch (Exception e) {
            this.typicalAdditionalThermalPower = 0;
            this.getGlobalLogger().logWarning("Can't get typicalAdditionalThermalPower, using the default value: " + this.typicalAdditionalThermalPower);
        }

        try {
            this.typicalGasPower = Integer.parseInt(this.getDriverConfig().getParameter("typicalGasPower"));
        } catch (Exception e) {
            this.typicalGasPower = 20500;
            this.getGlobalLogger().logWarning("Can't get typicalGasPower, using the default value: " + this.typicalGasPower);
        }

        try {
            this.hotWaterTankUuid = UUID.fromString(this.getDriverConfig().getParameter("hotWaterTankUuid"));
        } catch (Exception e) {
            this.hotWaterTankUuid = UUID.fromString("00000000-0000-4857-4853-000000000000");
            this.getGlobalLogger().logWarning("Can't get hotWaterTankUuid, using the default value: " + this.hotWaterTankUuid);
        }

        try {
            this.rescheduleAfter = Integer.parseInt(this.getDriverConfig().getParameter("rescheduleAfter"));
        } catch (Exception e) {
            this.rescheduleAfter = 4 * 3600; // 4 hours
            this.getGlobalLogger().logWarning("Can't get rescheduleAfter, using the default value: " + this.rescheduleAfter);
        }

        try {
            this.newIPPAfter = Long.parseLong(this.getDriverConfig().getParameter("newIPPAfter"));
        } catch (Exception e) {
            this.newIPPAfter = 3600; // 1 hour
            this.getGlobalLogger().logWarning("Can't get newIPPAfter, using the default value: " + this.newIPPAfter);
        }

        try {
            this.relativeHorizonIPP = Integer.parseInt(this.getDriverConfig().getParameter("relativeHorizonIPP"));
        } catch (Exception e) {
            this.relativeHorizonIPP = 18 * 3600; // 18 hours
            this.getGlobalLogger().logWarning("Can't get relativeHorizonIPP, using the default value: " + this.relativeHorizonIPP);
        }

        try {
            this.currentHotWaterStorageMinTemp = Double.parseDouble(this.getDriverConfig().getParameter("currentHotWaterStorageMinTemp"));
        } catch (Exception e) {
            this.currentHotWaterStorageMinTemp = 60;
            this.getGlobalLogger().logWarning("Can't get currentHotWaterStorageMinTemp, using the default value: " + this.currentHotWaterStorageMinTemp);
        }

        try {
            this.currentHotWaterStorageMaxTemp = Double.parseDouble(this.getDriverConfig().getParameter("currentHotWaterStorageMaxTemp"));
        } catch (Exception e) {
            this.currentHotWaterStorageMaxTemp = 80;
            this.getGlobalLogger().logWarning("Can't get currentHotWaterStorageMaxTemp, using the default value: " + this.currentHotWaterStorageMaxTemp);
        }

        try {
            this.forcedOnHysteresis = Double.parseDouble(this.getDriverConfig().getParameter("forcedOnHysteresis"));
        } catch (Exception e) {
            this.forcedOnHysteresis = 5.0;
            this.getGlobalLogger().logWarning("Can't get forcedOnHysteresis, using the default value: " + this.forcedOnHysteresis);
        }

        try {
            this.fixedCostPerStart = Double.parseDouble(this.getDriverConfig().getParameter("fixedCostPerStart"));
        } catch (Exception e) {
            this.fixedCostPerStart = 8.0;
            this.getGlobalLogger().logWarning("Can't get fixedCostPerStart, using the default value: " + this.fixedCostPerStart);
        }

        try {
            this.forcedOnOffStepMultiplier = Double.parseDouble(this.getDriverConfig().getParameter("forcedOnOffStepMultiplier"));
        } catch (Exception e) {
            this.forcedOnOffStepMultiplier = 0.1;
            this.getGlobalLogger().logWarning("Can't get forcedOnOffStepMultiplier, using the default value: " + this.forcedOnOffStepMultiplier);
        }

        try {
            this.forcedOffAdditionalCost = Integer.parseInt(this.getDriverConfig().getParameter("forcedOffAdditionalCost"));
        } catch (Exception e) {
            this.forcedOffAdditionalCost = 10;
            this.getGlobalLogger().logWarning("Can't get forcedOffAdditionalCost, using the default value: " + this.forcedOffAdditionalCost);
        }

        try {
            this.chpOnCervisiaStepSizeMultiplier = Double.parseDouble(this.getDriverConfig().getParameter("chpOnCervisiaStepSizeMultiplier"));
        } catch (Exception e) {
            this.chpOnCervisiaStepSizeMultiplier = 0.0000001;
            this.getGlobalLogger().logWarning("Can't get chpOnCervisiaStepSizeMultiplier, using the default value: " + this.chpOnCervisiaStepSizeMultiplier);
        }

        try {
            this.minRuntime = Integer.parseInt(this.getDriverConfig().getParameter("minRuntime"));
        } catch (Exception e) {
            this.minRuntime = 15 * 60;
            this.getGlobalLogger().logWarning("Can't get minRuntime, using the default value: " + this.minRuntime);
        }

        try {
            this.cosPhi = Double.parseDouble(this.getDriverConfig().getParameter("cosPhi"));
        } catch (Exception e) {
            this.cosPhi = 0.9;
            this.getGlobalLogger().logWarning("Can't get cosPhi, using the default value: " + this.cosPhi);
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

        try {
            this.typicalReactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(this.typicalActivePower, this.cosPhi, true);
        } catch (Exception e) {
            e.printStackTrace();
        }

        String dachsHost = driverConfig.getParameter("dachshost");
        String dachsPort = driverConfig.getParameter("dachsport");
        if (dachsHost == null
                || dachsPort == null
                || dachsHost.isEmpty()
                || dachsPort.isEmpty()) {
            throw new OSHException("Invalid Dachs Host or Port");
        }
        this.dachsURL = "http://" + dachsHost + ":" + dachsPort + "/";

        this.setMinimumRuntime(30 * 60); // 30 minutes
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        ChpStaticDetailsObserverExchange observerExchange =
                new ChpStaticDetailsObserverExchange(this.getUUID(), this.getTimer().getUnixTime());
        observerExchange.setTypicalActivePower(this.typicalActivePower);
        observerExchange.setTypicalReactivePower(this.typicalReactivePower);
        observerExchange.setTypicalThermalPower(this.typicalThermalPower);
        observerExchange.setTypicalGasPower(this.typicalGasPower);
        observerExchange.setOperationMode(ChpOperationMode.HEAT_AND_ELECTRICITY_LED);
        observerExchange.setHotWaterTankUuid(this.hotWaterTankUuid);
        observerExchange.setRescheduleAfter(this.rescheduleAfter);
        observerExchange.setNewIPPAfter(this.newIPPAfter);
        observerExchange.setRelativeHorizonIPP(this.relativeHorizonIPP);
        observerExchange.setCurrentHotWaterStorageMinTemp(this.currentHotWaterStorageMinTemp);
        observerExchange.setCurrentHotWaterStorageMaxTemp(this.currentHotWaterStorageMaxTemp);
        observerExchange.setForcedOnHysteresis(this.forcedOnHysteresis);

        observerExchange.setFixedCostPerStart(this.fixedCostPerStart);
        observerExchange.setForcedOnOffStepMultiplier(this.forcedOnOffStepMultiplier);
        observerExchange.setForcedOffAdditionalCost(this.forcedOffAdditionalCost);
        observerExchange.setChpOnCervisiaStepSizeMultiplier(this.chpOnCervisiaStepSizeMultiplier);
        observerExchange.setMinRuntime(this.minRuntime);

        this.notifyObserver(observerExchange);

        StaticCompressionExchange stat = new StaticCompressionExchange(this.getUUID(), this.getTimer().getUnixTime());
        stat.setCompressionType(this.compressionType);
        stat.setCompressionValue(this.compressionValue);
        this.notifyObserver(stat);
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // still alive message
        if (this.getTimer().getUnixTime() % 60 == 0) {
            this.getGlobalLogger().logDebug("onNextTimePeriod() (getTimer().getUnixTime() % 60 == 0) - I'm still alive");
        }
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        super.onSystemShutdown();
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        if (controllerRequest instanceof ChpControllerExchange) {
            ChpControllerExchange chpControllerExchange = (ChpControllerExchange) controllerRequest;
//			getGlobalLogger().logDebug("onControllerRequest(ChpControllerExchange)");

            this.setElectricityRequest(chpControllerExchange.isElectricityRequest());
            this.setHeatingRequest(chpControllerExchange.isHeatingRequest());

            this.sendPowerRequestToChp();
        }
    }

    // for callback of DachsInformationRequestThread
    public abstract void processDachsDetails(DachsDriverDetails dachsDetails);

    protected Integer parseIntegerStatus(String value) {
        if (value == null || value.isEmpty()) return null;

        int i;
        try {
            i = Integer.parseInt(value);
            return i;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected Double parseDoubleStatus(String value) {
        if (value == null || value.isEmpty()) return null;

        double i;
        try {
            i = Double.parseDouble(value);
            return i;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        if (exchange instanceof ChpElectricityRequest) {
            ChpElectricityRequest ceq = (ChpElectricityRequest) exchange;

            this.getGlobalLogger().logDebug("onQueueEventReceived(ChpElectricityRequest)");
            this.getGlobalLogger().logDebug("sendPowerRequestToChp(" + ceq.isOn() + ")");

            this.setElectricityRequest(ceq.isOn());

            this.sendPowerRequestToChp();
        }

    }

    public UUID getHotWaterTankUuid() {
        return this.hotWaterTankUuid;
    }


    protected String getDachsURL() {
        return this.dachsURL;
    }

}
