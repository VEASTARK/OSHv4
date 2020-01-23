package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.globalobserver.*;
import osh.hal.exchange.*;
import osh.registry.interfaces.IDataRegistryListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 */
public class GuiComManager extends ComManager implements IDataRegistryListener {

    private final ReentrantLock modifierLock = new ReentrantLock();
    private Class<? extends AbstractExchange> stateViewerType;
    private StateViewerRegistryEnum stateViewerRegistry = StateViewerRegistryEnum.OC;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param uuid
     */
    public GuiComManager(IOSHOC osh, UUID uuid) {
        super(osh, uuid);
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // signals from utility or the like
        this.getOCRegistry().subscribe(EpsStateExchange.class, this);
        this.getOCRegistry().subscribe(PlsStateExchange.class, this);

        // states to visualize
        this.getOCRegistry().subscribe(GUIScheduleStateExchange.class, this);
        this.getOCRegistry().subscribe(GUIHotWaterPredictionStateExchange.class, this);
        this.getOCRegistry().subscribe(GUIDeviceListStateExchange.class, this);
        this.getOCRegistry().subscribe(GUIAncillaryMeterStateExchange.class, this);

        this.getOCRegistry().subscribe(WaterStorageOCSX.class, this);
        this.getOCRegistry().subscribe(BatteryStorageOCSX.class, this);
        this.getOCRegistry().subscribe(DevicesPowerStateExchange.class, this);

        this.getTimer().registerComponent(this, 1);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {

        if (exchange instanceof GUIScheduleStateExchange) {
            GUIScheduleStateExchange se = (GUIScheduleStateExchange) exchange;
            GUIScheduleComExchange gsce = new GUIScheduleComExchange(
                    this.getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
            this.updateOcDataSubscriber(gsce);

        } else if (exchange instanceof GUIDeviceListStateExchange) {
            GUIDeviceListStateExchange se = (GUIDeviceListStateExchange) exchange;
            GUIDeviceListComExchange gdlce = new GUIDeviceListComExchange(
                    this.getUUID(), se.getTimestamp(), se.getDeviceList());
            this.updateOcDataSubscriber(gdlce);
        } else if (exchange instanceof WaterStorageOCSX) {
            WaterStorageOCSX sx = (WaterStorageOCSX) exchange;
            GUIWaterStorageComExchange gwsce = new GUIWaterStorageComExchange(
                    this.getUUID(),
                    sx.getTimestamp(),
                    sx.getCurrentTemp(),
                    sx.getMinTemp(),
                    sx.getMaxTemp(),
                    sx.getDemand(),
                    sx.getSupply(),
                    sx.getTankId());
            this.updateOcDataSubscriber(gwsce);
        } else if (exchange instanceof BatteryStorageOCSX) {
            BatteryStorageOCSX sx = (BatteryStorageOCSX) exchange;
            GUIBatteryStorageComExchange gbsce = new GUIBatteryStorageComExchange(
                    this.getUUID(),
                    sx.getTimestamp(),
                    sx.getStateOfCharge(),
                    sx.getMinStateOfCharge(),
                    sx.getMaxStateOfCharge(),
                    sx.getBatteryId());
            this.updateOcDataSubscriber(gbsce);
        } else if (exchange instanceof EpsStateExchange) {
            EpsStateExchange eee = (EpsStateExchange) exchange;
            GUIEpsComExchange gece = new GUIEpsComExchange(this.getUUID(), eee.getTimestamp());
            gece.setPriceSignals(eee.getPriceSignals());
            this.updateOcDataSubscriber(gece);
        } else if (exchange instanceof PlsStateExchange) {
            PlsStateExchange pse = (PlsStateExchange) exchange;
            GUIPlsComExchange gpce = new GUIPlsComExchange(this.getUUID(), pse.getTimestamp());
            gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
            this.updateOcDataSubscriber(gpce);
        } else if (exchange instanceof DevicesPowerStateExchange) {
            DevicesPowerStateExchange dpsex = (DevicesPowerStateExchange) exchange;
            DevicesPowerComExchange gpce = new DevicesPowerComExchange(this.getUUID(), dpsex.getTimestamp(), dpsex);
            this.updateOcDataSubscriber(gpce);
        } else if (exchange instanceof GUIHotWaterPredictionStateExchange) {
            GUIHotWaterPredictionStateExchange ghwpse = (GUIHotWaterPredictionStateExchange) exchange;
            GUIHotWaterPredictionComExchange ghwpce = new GUIHotWaterPredictionComExchange(
                    this.getUUID(),
                    ghwpse.getTimestamp(),
                    ghwpse.getPredictedTankTemp(),
                    ghwpse.getPredictedHotWaterDemand(),
                    ghwpse.getPredictedHotWaterSupply());
            this.updateOcDataSubscriber(ghwpce);
        } else if (exchange instanceof GUIAncillaryMeterStateExchange) {
            GUIAncillaryMeterStateExchange gamse = (GUIAncillaryMeterStateExchange) exchange;
            GUIAncillaryMeterComExchange gamce = new GUIAncillaryMeterComExchange(this.getUUID(), gamse.getTimestamp(), gamse.getAncillaryMeter());
            this.updateOcDataSubscriber(gamce);
        }
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        if (exchangeObject instanceof GUIStateSelectedComExchange) {
            GUIStateSelectedComExchange gssce = (GUIStateSelectedComExchange) exchangeObject;
            this.modifierLock.lock();
            try {
                this.stateViewerType = gssce.getSelected();
            } finally {
                this.modifierLock.unlock();
            }
        } else if (exchangeObject instanceof GUIStateRegistrySelectedComExchange) {
            osh.hal.exchange.GUIStateRegistrySelectedComExchange gssrce = (GUIStateRegistrySelectedComExchange) exchangeObject;
            this.modifierLock.lock();
            try {
                this.stateViewerRegistry = gssrce.getSelected();
            } finally {
                this.modifierLock.unlock();
            }
        }
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // TODO build clean sum object (NOT WaterStorageSumDetails...)...maybe somewhere else
        // Best place would be GlobalObserver!

        this.modifierLock.lock();
        try {
            if (this.stateViewerRegistry == StateViewerRegistryEnum.OC) {
                Map<UUID, ? extends AbstractExchange> states = null;
                if (this.stateViewerType != null) {
                    states = this.getOCRegistry().getData(this.stateViewerType);
                }

                this.updateOcDataSubscriber(
                        new GUIStatesComExchange(
                                this.getUUID(),
                                this.getTimer().getUnixTime(),
                                this.getOCRegistry().getDataTypes(),
                                states));
            } else if (this.stateViewerRegistry == StateViewerRegistryEnum.DRIVER) {
                this.updateOcDataSubscriber(
                        new GUIStatesComExchange(
                                this.getUUID(),
                                this.getTimer().getUnixTime(),
                                this.stateViewerType));
            }
        } finally {
            this.modifierLock.unlock();
        }
    }
}
