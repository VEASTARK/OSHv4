package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
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
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.GasBoilerObserverExchange;
import osh.mgmt.ipp.GasBoilerNonControllableIPP;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author Ingo Mauser
 */
public class NonControllableGasBoilerLocalObserver
        extends LocalObserver {

    private Duration NEW_IPP_AFTER;
    private ZonedDateTime lastTimeIPPSent;
    private boolean initialStateLastIPP;
    private double lastIPPMinTemperature = 60;
    private double lastIPPMaxTemperature = 80;

    private double minTemperature = 60;
    private double maxTemperature = 80;
    private boolean initialState;

    private int maxHotWaterPower = 15000;
    private int maxGasPower = 15000;

    private int typicalActivePowerOn = 100;
    private int typicalActivePowerOff;
    private int typicalReactivePowerOn;
    private int typicalReactivePowerOff;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public NonControllableGasBoilerLocalObserver(IOSHOC osh) {
        super(osh);
        //NOTHING
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        if (this.NEW_IPP_AFTER != null && this.NEW_IPP_AFTER.toSeconds() % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        ZonedDateTime now = exchange.getTime();
        //TODO: change to sending as soon as as lasttime+new_ipp_after is reached not the next tick when the next
        // backwards-compatibility breaking update is released
        if (this.lastTimeIPPSent == null || now.isAfter(this.lastTimeIPPSent.plus(this.NEW_IPP_AFTER))) {
            GasBoilerNonControllableIPP sipp = new GasBoilerNonControllableIPP(
                    this.getUUID(),
                    now,
                    this.minTemperature,
                    this.maxTemperature,
                    this.initialState,
                    this.maxHotWaterPower,
                    this.maxGasPower,
                    this.typicalActivePowerOn,
                    this.typicalActivePowerOff,
                    this.typicalReactivePowerOn,
                    this.typicalReactivePowerOff,
                    this.compressionType,
                    this.compressionValue);
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class, this, sipp);
            this.lastTimeIPPSent = now;
            this.lastIPPMaxTemperature = this.maxTemperature;
            this.lastIPPMinTemperature = this.minTemperature;
        }

    }


    @Override
    public void onDeviceStateUpdate() {
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        IHALExchange _ihal = this.getObserverDataObject();

        if (_ihal instanceof GasBoilerObserverExchange) {
            GasBoilerObserverExchange ox = (GasBoilerObserverExchange) _ihal;

            this.minTemperature = ox.getMinTemperature();
            this.maxTemperature = ox.getMaxTemperature();
            this.initialState = ox.getCurrentState();
            this.maxHotWaterPower = ox.getMaxHotWaterPower();
            this.maxGasPower = ox.getMaxGasPower();
            this.typicalActivePowerOn = ox.getTypicalActivePowerOn();
            this.typicalActivePowerOff = ox.getTypicalActivePowerOff();
            this.typicalReactivePowerOn = ox.getTypicalReactivePowerOn();
            this.typicalReactivePowerOff = ox.getTypicalReactivePowerOff();
            this.NEW_IPP_AFTER = ox.getNewIppAfter();

            if (this.initialStateLastIPP != this.initialState || this.lastIPPMaxTemperature != this.maxTemperature || this.lastIPPMinTemperature != this.minTemperature) {
                // build SIPP
                GasBoilerNonControllableIPP sipp = new GasBoilerNonControllableIPP(
                        this.getUUID(),
                        now,
                        this.minTemperature,
                        this.maxTemperature,
                        this.initialState,
                        this.maxHotWaterPower,
                        this.maxGasPower,
                        this.typicalActivePowerOn,
                        this.typicalActivePowerOff,
                        this.typicalReactivePowerOn,
                        this.typicalReactivePowerOff,
                        this.compressionType,
                        this.compressionValue);
                this.getOCRegistry().publish(
                        InterdependentProblemPart.class, this, sipp);
                this.initialStateLastIPP = this.initialState;
                this.lastTimeIPPSent = now;
                this.lastIPPMaxTemperature = this.maxTemperature;
                this.lastIPPMinTemperature = this.minTemperature;
            }

            // build SX
            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentTime(),
                    DeviceTypes.INSERTHEATINGELEMENT);

            cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
            cpse.addPowerState(Commodity.REACTIVEPOWER, ox.getReactivePower());
            cpse.addPowerState(Commodity.NATURALGASPOWER, ox.getGasPower());
            cpse.addPowerState(Commodity.HEATINGHOTWATERPOWER, ox.getHotWaterPower());
            this.getOCRegistry().publish(
                    CommodityPowerStateExchange.class,
                    this,
                    cpse);
        } else if (_ihal instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) _ihal;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }

    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {
        return null;
    }
}
