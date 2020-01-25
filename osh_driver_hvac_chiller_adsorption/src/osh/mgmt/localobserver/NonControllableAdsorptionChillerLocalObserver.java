package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.ChillerObserverExchange;
import osh.mgmt.mox.AdsorptionChillerMOX;

import java.util.Map;
import java.util.UUID;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class NonControllableAdsorptionChillerLocalObserver extends LocalObserver {

    // Temporary constants
    private final UUID coldWaterTankUuid = UUID.fromString("441c234e-d340-4c85-b0a0-dbac182b8f81");
    private final UUID hotWaterTankUuid = UUID.fromString("00000000-0000-4857-4853-000000000000");
    private boolean running;
    private double coldWaterTemperature = Double.MIN_VALUE;
    private double hotWaterTemperature = Double.MIN_VALUE;
    // current values
    private int activePower;
    private int reactivePower;
    private int hotWaterPower;
    private int coldWaterPower;
    private Map<Long, Double> temperatureMap;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR
     */
    public NonControllableAdsorptionChillerLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onDeviceStateUpdate() {
        IHALExchange hx = this.getObserverDataObject();
        if (hx == null) {
            return;
        }
        if (hx instanceof ChillerObserverExchange) {
            ChillerObserverExchange ox = (ChillerObserverExchange) this.getObserverDataObject();
            this.running = ox.isRunning();

            this.activePower = ox.getActivePower();
            this.reactivePower = ox.getReactivePower();

            this.hotWaterPower = ox.getHotWaterPower();
            this.coldWaterPower = ox.getColdWaterPower();

            //DIRTY HACK
            if (this.temperatureMap == null) {
                this.temperatureMap = ox.getOutdoorTemperature().getMap();
            }

            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentEpochSecond(),
                    DeviceTypes.ADSORPTIONCHILLER);
            cpse.addPowerState(Commodity.ACTIVEPOWER, this.activePower);
            cpse.addPowerState(Commodity.REACTIVEPOWER, this.reactivePower);
            cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, this.hotWaterPower);
            cpse.addPowerState(Commodity.COLDWATERPOWER, this.coldWaterPower);
            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    cpse);
        } else if (hx instanceof StaticCompressionExchange) {
            StaticCompressionExchange sce = (StaticCompressionExchange) hx;
            this.compressionType = sce.getCompressionType();
            this.compressionValue = sce.getCompressionValue();
        }
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {

        WaterStorageOCSX wssx = (WaterStorageOCSX) this.getOCRegistry().getData(WaterStorageOCSX.class, this.coldWaterTankUuid);
        if (wssx != null) {
            this.coldWaterTemperature = wssx.getCurrentTemp();
        }

        WaterStorageOCSX hwssx = (WaterStorageOCSX) this.getOCRegistry().getData(WaterStorageOCSX.class, this.hotWaterTankUuid);
        if (hwssx != null) {
            this.hotWaterTemperature = hwssx.getCurrentTemp();
        }

        // TODO: use real prediction

        return new AdsorptionChillerMOX(
                this.coldWaterTemperature,
                this.hotWaterTemperature,
                this.running,
                0,
                this.temperatureMap,
                this.compressionType,
                this.compressionValue);
    }


}
