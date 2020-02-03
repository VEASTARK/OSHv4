package osh.mgmt.localobserver.cooling;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceCoolingObserverExchange;
import osh.mgmt.ipp.ChilledWaterDemandNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author Ingo Mauser, Julian Feder
 */
public class SpaceCoolingLocalObserver
        extends ThermalDemandLocalObserver {

    private ArrayList<ChillerCalendarDate> dates;
    private Map<Long, Double> temperaturePrediction;
    private int coldWaterPower;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public SpaceCoolingLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onDeviceStateUpdate() {
        IHALExchange hx = this.getObserverDataObject();

        if (hx instanceof SpaceCoolingObserverExchange) {

            SpaceCoolingObserverExchange ox = (SpaceCoolingObserverExchange) hx;
            this.dates = ox.getDates();
            this.temperaturePrediction = ox.getTemperaturePrediction();
            this.coldWaterPower = ox.getColdWaterPower();

            ChilledWaterDemandNonControllableIPP ipp =
                    new ChilledWaterDemandNonControllableIPP(
                            this.getUUID(),
                            this.getGlobalLogger(),
                            this.getTimeDriver().getCurrentTime(),
                            false,
                            this.dates,
                            this.temperaturePrediction,
                            this.compressionType,
                            this.compressionValue);
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class, this, ipp);

            // set current power state
            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    DeviceTypes.SPACECOOLING);
            cpse.addPowerState(Commodity.COLDWATERPOWER, this.coldWaterPower);
            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);

        } else if (hx instanceof StaticCompressionExchange) {

            StaticCompressionExchange _stat = (StaticCompressionExchange) hx;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }

}
