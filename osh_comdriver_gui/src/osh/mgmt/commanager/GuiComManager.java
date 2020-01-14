package osh.mgmt.commanager;

import osh.cal.ICALExchange;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;
import osh.core.com.ComManager;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.datatypes.registry.EventExchange;
import osh.datatypes.registry.StateChangedExchange;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.details.utility.EpsStateExchange;
import osh.datatypes.registry.oc.details.utility.PlsStateExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.datatypes.registry.oc.state.GUIScheduleDebugExchange;
import osh.datatypes.registry.oc.state.globalobserver.*;
import osh.hal.exchange.*;
import osh.registry.interfaces.IEventTypeReceiver;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;


/**
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 */
public class GuiComManager extends ComManager implements IEventTypeReceiver {

    private final ReentrantLock modifierLock = new ReentrantLock();
    private Class<? extends StateExchange> stateViewerType;
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
        this.getOCRegistry().registerStateChangeListener(EpsStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(PlsStateExchange.class, this);

        // states to visualize
        this.getOCRegistry().registerStateChangeListener(GUIScheduleStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(GUIHotWaterPredictionStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(GUIDeviceListStateExchange.class, this);
        this.getOCRegistry().registerStateChangeListener(GUIAncillaryMeterStateExchange.class, this);

        this.getOCRegistry().registerStateChangeListener(WaterStorageOCSX.class, this);
        this.getOCRegistry().registerStateChangeListener(BatteryStorageOCSX.class, this);
        this.getOCRegistry().registerStateChangeListener(DevicesPowerStateExchange.class, this);


        // schedule to visualize
        this.getOCRegistry().register(GUIScheduleDebugExchange.class, this);

        this.getTimer().registerComponent(this, 1);
    }

    @Override
    public <T extends EventExchange> void onQueueEventTypeReceived(Class<T> type, T event) throws OSHException {

        if (event instanceof StateChangedExchange) {
            StateChangedExchange exsc = (StateChangedExchange) event;

            if (exsc.getType().equals(GUIScheduleStateExchange.class)) {
                GUIScheduleStateExchange se =
                        this.getOCRegistry().getState(GUIScheduleStateExchange.class, exsc.getStatefulEntity());
                GUIScheduleComExchange gsce = new GUIScheduleComExchange(
                        this.getUUID(), se.getTimestamp(), se.getDebugGetSchedules(), se.getStepSize());
                this.updateOcDataSubscriber(gsce);

            } else if (exsc.getType().equals(GUIDeviceListStateExchange.class)) {
                GUIDeviceListStateExchange se =
                        this.getOCRegistry().getState(GUIDeviceListStateExchange.class, exsc.getStatefulEntity());
                GUIDeviceListComExchange gdlce = new GUIDeviceListComExchange(
                        this.getUUID(), se.getTimestamp(), se.getDeviceList());
                this.updateOcDataSubscriber(gdlce);
            } else if (exsc.getType().equals(WaterStorageOCSX.class)) {
                WaterStorageOCSX sx = this.getOCRegistry().getState(WaterStorageOCSX.class, exsc.getStatefulEntity());
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
            } else if (exsc.getType().equals(BatteryStorageOCSX.class)) {
                BatteryStorageOCSX sx = this.getOCRegistry().getState(BatteryStorageOCSX.class, exsc.getStatefulEntity());
                GUIBatteryStorageComExchange gbsce = new GUIBatteryStorageComExchange(
                        this.getUUID(),
                        sx.getTimestamp(),
                        sx.getStateOfCharge(),
                        sx.getMinStateOfCharge(),
                        sx.getMaxStateOfCharge(),
                        sx.getBatteryId());
                this.updateOcDataSubscriber(gbsce);
            } else if (exsc.getType().equals(EpsStateExchange.class)) {
                EpsStateExchange eee = this.getOCRegistry().getState(EpsStateExchange.class, exsc.getStatefulEntity());
                GUIEpsComExchange gece = new GUIEpsComExchange(this.getUUID(), eee.getTimestamp());
                gece.setPriceSignals(eee.getPriceSignals());
                this.updateOcDataSubscriber(gece);
            } else if (exsc.getType().equals(PlsStateExchange.class)) {
                PlsStateExchange pse = this.getOCRegistry().getState(PlsStateExchange.class, exsc.getStatefulEntity());
                GUIPlsComExchange gpce = new GUIPlsComExchange(this.getUUID(), pse.getTimestamp());
                gpce.setPowerLimitSignals(pse.getPowerLimitSignals());
                this.updateOcDataSubscriber(gpce);
            } else if (exsc.getType().equals(DevicesPowerStateExchange.class)) {
                DevicesPowerStateExchange dpsex = this.getOCRegistry().getState(DevicesPowerStateExchange.class, exsc.getStatefulEntity());
                DevicesPowerComExchange gpce = new DevicesPowerComExchange(this.getUUID(), dpsex.getTimestamp(), dpsex);
                this.updateOcDataSubscriber(gpce);
            } else if (exsc.getType().equals(GUIHotWaterPredictionStateExchange.class)) {
                GUIHotWaterPredictionStateExchange ghwpse = this.getOCRegistry().getState(GUIHotWaterPredictionStateExchange.class, exsc.getStatefulEntity());
                GUIHotWaterPredictionComExchange ghwpce = new GUIHotWaterPredictionComExchange(
                        this.getUUID(),
                        ghwpse.getTimestamp(),
                        ghwpse.getPredictedTankTemp(),
                        ghwpse.getPredictedHotWaterDemand(),
                        ghwpse.getPredictedHotWaterSupply());
                this.updateOcDataSubscriber(ghwpce);
            } else if (exsc.getType().equals(GUIAncillaryMeterStateExchange.class)) {
                GUIAncillaryMeterStateExchange gamse = this.getOCRegistry().getState(GUIAncillaryMeterStateExchange.class, exsc.getStatefulEntity());
                GUIAncillaryMeterComExchange gamce = new GUIAncillaryMeterComExchange(this.getUUID(), gamse.getTimestamp(), gamse.getAncillaryMeter());
                this.updateOcDataSubscriber(gamce);
            }
        }
    }

    @Override
    public void onDriverUpdate(ICALExchange exchangeObject) {
        if (exchangeObject instanceof GUIStateSelectedComExchange) {
            GUIStateSelectedComExchange gssce = (GUIStateSelectedComExchange) exchangeObject;
            this.modifierLock.lock();
            this.stateViewerType = gssce.getSelected();
            this.modifierLock.lock();
        } else if (exchangeObject instanceof GUIStateRegistrySelectedComExchange) {
            osh.hal.exchange.GUIStateRegistrySelectedComExchange gssrce = (GUIStateRegistrySelectedComExchange) exchangeObject;
            this.modifierLock.lock();
            this.stateViewerRegistry = gssrce.getSelected();
            this.modifierLock.lock();
        }
    }

    @Override
    public void onNextTimePeriod() throws OSHException {
        super.onNextTimePeriod();

        // TODO build clean sum object (NOT WaterStorageSumDetails...)...maybe somewhere else
        // Best place would be GlobalObserver!

        this.modifierLock.lock();
        if (this.stateViewerRegistry == StateViewerRegistryEnum.OC) {
            Map<UUID, ? extends StateExchange> states = null;
            if (this.stateViewerType != null) {
                states = this.getOCRegistry().getStates(this.stateViewerType);
            }

            this.updateOcDataSubscriber(
                    new GUIStatesComExchange(
                            this.getUUID(),
                            this.getTimer().getUnixTime(),
                            this.getOCRegistry().getTypes(),
                            states));
        } else if (this.stateViewerRegistry == StateViewerRegistryEnum.DRIVER) {
            this.updateOcDataSubscriber(
                    new GUIStatesComExchange(
                            this.getUUID(),
                            this.getTimer().getUnixTime(),
                            this.stateViewerType));
        }
        this.modifierLock.unlock();
    }

}
