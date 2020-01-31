package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.driver.chp.ChpOperationMode;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChpObserverExchange;
import osh.hal.exchange.ChpStaticDetailsObserverExchange;
import osh.mgmt.mox.DachsChpMOX;

import java.time.Duration;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class DachsChpLocalObserver
        extends LocalObserver {

    // TODO move into config
    // data from WaterTank
    private double waterTemperature = 70;

    // current values
    private int activePower;
    private int reactivePower;
    private int hotWaterPower;
    private int gasPower;

    private Duration runtimeRemaining;
    private boolean running;

    // quasi static values
    private final ChpOperationMode operationMode = ChpOperationMode.UNKNOWN;
    private int typicalActivePower;
    private int typicalReactivePower;
    private int typicalGasPower;
    private int typicalThermalPower;
    private UUID hotWaterTankUuid;

    private Duration rescheduleAfter;
    private Duration newIPPAfter;
    private int relativeHorizonIPP;
    private double currentHotWaterStorageMinTemp;
    private double currentHotWaterStorageMaxTemp;
    private double forcedOnHysteresis;

    private double fixedCostPerStart;
    private double forcedOnOffStepMultiplier;
    private int forcedOffAdditionalCost;
    private double chpOnCervisiaStepSizeMultiplier;
    private int minRunTime;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public DachsChpLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
    }

    @Override
    public void onDeviceStateUpdate() {
        IHALExchange ihex = this.getObserverDataObject();

        if (ihex instanceof ChpObserverExchange) {
            ChpObserverExchange dox = (ChpObserverExchange) ihex;

            // current values...
            this.activePower = dox.getActivePower();
            this.reactivePower = dox.getReactivePower();
            this.hotWaterPower = dox.getHotWaterPower();
            this.gasPower = dox.getGasPower();

            this.running = dox.isRunning();
            this.runtimeRemaining = dox.getMinRuntimeRemaining();


            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    DeviceTypes.CHPPLANT);

            cpse.addPowerState(Commodity.ACTIVEPOWER, this.activePower);
            cpse.addPowerState(Commodity.REACTIVEPOWER, this.reactivePower);
            cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, this.hotWaterPower);
            cpse.addPowerState(Commodity.NATURALGASPOWER, this.gasPower);

            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);

        } else if (ihex instanceof ChpStaticDetailsObserverExchange) {
            ChpStaticDetailsObserverExchange diox = (ChpStaticDetailsObserverExchange) ihex;

            // static details...
            this.typicalActivePower = diox.getTypicalActivePower();
            this.typicalReactivePower = diox.getTypicalReactivePower();
            this.typicalGasPower = diox.getTypicalGasPower();
            this.typicalThermalPower = diox.getTypicalThermalPower();
            this.hotWaterTankUuid = diox.getHotWaterTankUuid();
            this.rescheduleAfter = diox.getRescheduleAfter();
            this.newIPPAfter = diox.getNewIPPAfter();
            this.currentHotWaterStorageMinTemp = diox.getCurrentHotWaterStorageMinTemp();
            this.currentHotWaterStorageMaxTemp = diox.getCurrentHotWaterStorageMaxTemp();
            this.forcedOnHysteresis = diox.getForcedOnHysteresis();
            this.relativeHorizonIPP = diox.getRelativeHorizonIPP();

            this.fixedCostPerStart = diox.getFixedCostPerStart();
            this.forcedOnOffStepMultiplier = diox.getForcedOnOffStepMultiplier();
            this.forcedOffAdditionalCost = diox.getForcedOffAdditionalCost();
            this.chpOnCervisiaStepSizeMultiplier = diox.getChpOnCervisiaStepSizeMultiplier();
            this.minRunTime = diox.getMinRuntime();


        } else if (ihex instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) ihex;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {

        WaterStorageOCSX sx = (WaterStorageOCSX) this.getOCRegistry().getData(
                WaterStorageOCSX.class,
                this.hotWaterTankUuid);
        this.waterTemperature = sx.getCurrentTemp();

        return new DachsChpMOX(
                this.waterTemperature,
                this.running,
                this.runtimeRemaining,
                this.activePower,
                this.reactivePower,
                this.hotWaterPower,
                this.gasPower,
                this.operationMode,
                this.typicalActivePower,
                this.typicalReactivePower,
                this.typicalGasPower,
                this.typicalThermalPower,
                this.rescheduleAfter,
                this.newIPPAfter,
                this.relativeHorizonIPP,
                this.currentHotWaterStorageMinTemp,
                this.currentHotWaterStorageMaxTemp,
                this.forcedOnHysteresis,
                this.fixedCostPerStart,
                this.forcedOnOffStepMultiplier,
                this.forcedOffAdditionalCost,
                this.chpOnCervisiaStepSizeMultiplier,
                this.minRunTime,
                this.compressionType,
                this.compressionValue);
    }
}
