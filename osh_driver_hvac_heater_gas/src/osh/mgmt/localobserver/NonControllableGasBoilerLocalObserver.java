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

/**
 * @author Ingo Mauser
 */
public class NonControllableGasBoilerLocalObserver
        extends LocalObserver {

    private int NEW_IPP_AFTER;
    private long lastTimeIPPSent = Long.MIN_VALUE;
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

        if (this.NEW_IPP_AFTER % 60 == 0) {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.MINUTE);
        } else {
            this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);

        long now = exchange.getEpochSecond();

        if (now > this.lastTimeIPPSent + this.NEW_IPP_AFTER) {
            GasBoilerNonControllableIPP sipp = new GasBoilerNonControllableIPP(
                    this.getUUID(),
                    this.getGlobalLogger(),
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
        long now = this.getTimeDriver().getCurrentEpochSecond();

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
                        this.getGlobalLogger(),
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
                this.lastIPPMaxTemperature = this.maxTemperature;
                this.lastIPPMinTemperature = this.minTemperature;
            }

            // build SX
            CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                    this.getUUID(),
                    this.getTimeDriver().getCurrentEpochSecond(),
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
