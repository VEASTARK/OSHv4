package osh.mgmt.localobserver;

import osh.configuration.system.DeviceTypes;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.hal.exchange.BatteryStorageOX;
import osh.mgmt.ipp.BatteryStorageNonControllableIPP;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * @author Jan Müller, Sebastian Kramer
 */
public class NonControllableInverterBatteryStorageObserver
        extends LocalObserver {


    private Duration NEW_IPP_AFTER;
    private ZonedDateTime lastTimeIPPSent;
    private double lastSOCIPP = Integer.MIN_VALUE;
    private int TRIGGER_IPP_IF_DELTASoC_BIGGER;

    /**
     * CONSTRUCTOR
     */
    public NonControllableInverterBatteryStorageObserver(IOSHOC osh) {
        super(osh);
        //NOTHING
    }

    @Override
    public void onDeviceStateUpdate() {

        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        // get OX
        BatteryStorageOX ox = (BatteryStorageOX) this.getObserverDataObject();
        this.NEW_IPP_AFTER = ox.getNewIppAfter();
        this.TRIGGER_IPP_IF_DELTASoC_BIGGER = ox.getTriggerIppIfDeltaSoCBigger();

        if (this.lastTimeIPPSent == null ||
                this.lastTimeIPPSent.plus(this.NEW_IPP_AFTER).isBefore(now) || Math.abs((ox.getBatteryStateOfCharge() - this.lastSOCIPP)) > this.TRIGGER_IPP_IF_DELTASoC_BIGGER) {
            // build SIPP
            BatteryStorageNonControllableIPP sipp = new BatteryStorageNonControllableIPP(
                    this.getUUID(),
                    this.getGlobalLogger(),
                    now,
                    ox.getBatteryStateOfCharge(),
                    ox.getBatteryStateOfHealth(),
                    ox.getBatteryStandingLoss(),
                    ox.getBatteryMinChargingState(),
                    ox.getBatteryMaxChargingState(),
                    ox.getBatteryMinChargePower(),
                    ox.getBatteryMaxChargePower(),
                    ox.getBatteryMinDischargePower(),
                    ox.getBatteryMaxDischargePower(),
                    ox.getInverterMinComplexPower(),
                    ox.getInverterMaxComplexPower(),
                    ox.getInverterMinPower(),
                    ox.getInverterMaxPower(),
                    ox.getCompressionType(),
                    ox.getCompressionValue()
            );
            this.getOCRegistry().publish(
                    InterdependentProblemPart.class, this, sipp);
            this.lastTimeIPPSent = now;
            this.lastSOCIPP = ox.getBatteryStateOfCharge();
        }

        // build SX
        CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                this.getUUID(),
                now,
                DeviceTypes.BATTERYSTORAGE);

        cpse.addPowerState(Commodity.ACTIVEPOWER, ox.getActivePower());
        cpse.addPowerState(Commodity.REACTIVEPOWER, ox.getReactivePower());
        this.getOCRegistry().publish(
                CommodityPowerStateExchange.class,
                this,
                cpse);

        // save current state in OCRegistry (for e.g. GUI)
        BatteryStorageOCSX sx = new BatteryStorageOCSX(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime(),
                ox.getBatteryStateOfCharge(),
//						ox.getBatteryStateOfHealth(),
                ox.getBatteryMinChargingState(),
                ox.getBatteryMaxChargingState(),
                this.getUUID());
        this.getOCRegistry().publish(
                BatteryStorageOCSX.class,
                this,
                sx);
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(
            IModelOfObservationType type) {
        return null;
    }
}
