package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.exchange.ipp.IPPSchedulingExchange;
import osh.hal.exchange.SmartHeaterOX;
import osh.mgmt.ipp.SmartHeaterNonControllableIPP;
import osh.registry.interfaces.IHasState;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class SmartHeaterLocalObserver
        extends LocalObserver
        implements IHasState {

    private int temperatureSetting = 70;
    private int currentState;
    private long[] timestampOfLastChangePerSubElement = {0, 0, 0};

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    private long NEW_IPP_AFTER;
    private int TRIGGER_IPP_IF_DELTA_TEMP_BIGGER;
    private long lastTimeIppSent = Long.MIN_VALUE;
    private double lastIppTempSetting = Integer.MIN_VALUE;


    /**
     * CONSTRUCTOR
     */
    public SmartHeaterLocalObserver(IOSHOC osh) {
        super(osh);
        //NOTHING
    }


    @Override
    public void onDeviceStateUpdate() {
        long now = this.getTimer().getUnixTime();

        IHALExchange _ihal = this.getObserverDataObject();

        if (_ihal instanceof SmartHeaterOX) {
            // get OX
            SmartHeaterOX ox = (SmartHeaterOX) _ihal;

            this.temperatureSetting = ox.getTemperatureSetting();
            this.currentState = ox.getCurrentState();
            this.timestampOfLastChangePerSubElement = ox.getTimestampOfLastChangePerSubElement();

            long ipp_diff = now - this.lastTimeIppSent;
            if (ipp_diff >= this.NEW_IPP_AFTER || Math.abs(this.temperatureSetting - this.lastIppTempSetting) > this.TRIGGER_IPP_IF_DELTA_TEMP_BIGGER) {
                this.sendIPP(now);
            }

            // build SX
            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getDeviceID(),
                    this.getTimer().getUnixTime(),
                    DeviceTypes.INSERTHEATINGELEMENT);

            cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
            cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, ox.getHotWaterPower());
            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);
        } else if (_ihal instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        } else if (_ihal instanceof IPPSchedulingExchange) {
            IPPSchedulingExchange _ise = (IPPSchedulingExchange) _ihal;
            this.NEW_IPP_AFTER = _ise.getNewIppAfter();
            this.TRIGGER_IPP_IF_DELTA_TEMP_BIGGER = (int) _ise.getTriggerIfDeltaX();
        }
    }

    private void sendIPP(long now) {
        SmartHeaterNonControllableIPP sipp = new SmartHeaterNonControllableIPP(
                this.getDeviceID(),
                this.getGlobalLogger(),
                now,
                this.temperatureSetting,
                this.currentState,
                this.timestampOfLastChangePerSubElement,
                this.compressionType,
                this.compressionValue);
        this.getOCRegistry().publish(
                InterdependentProblemPart.class, this, sipp);
        this.lastTimeIppSent = now;
        this.lastIppTempSetting = this.temperatureSetting;
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {
        return null;
    }

    @Override
    public UUID getUUID() {
        return this.getDeviceID();
    }

}
