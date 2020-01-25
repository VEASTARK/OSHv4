package osh.comdriver.simulation;

import osh.OSH;
import osh.cal.ICALExchange;
import osh.comdriver.simulation.cruisecontrol.GuiDataCollector;
import osh.comdriver.simulation.cruisecontrol.GuiMain;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerListener;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerRegistryEnum;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.*;
import osh.simulation.SimulationComDriver;

import java.util.Map;
import java.util.UUID;


/**
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 */
public class GuiComDriver extends SimulationComDriver implements StateViewerListener {

    boolean saveGraph;
    private final GuiMain driver;
    private final GuiDataCollector collector;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public GuiComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        this.driver = new GuiMain(!this.getOSH().isSimulation());
        this.driver.registerListener(this);
        this.collector = new GuiDataCollector(this.driver, this.saveGraph);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        this.driver.updateTime(exchange.getEpochSecond());
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {

        if (exchangeObject instanceof GUIScheduleComExchange) {
            GUIScheduleComExchange exgs = (GUIScheduleComExchange) exchangeObject;
            this.collector.updateGlobalSchedule(exgs.getSchedules(), exchangeObject.getTimestamp());
        } else if (exchangeObject instanceof GUIDeviceListComExchange) {
            GUIDeviceListComExchange exgdl = (GUIDeviceListComExchange) exchangeObject;
            this.collector.updateEADeviceList(exgdl.getDeviceList());
        } else if (exchangeObject instanceof GUIStatesComExchange) {
            GUIStatesComExchange exgse = (GUIStatesComExchange) exchangeObject;
            if (exgse.isOcMode()) {
                this.collector.updateStateView(exgse.getTypes(), exgse.getStates());
            } else {
                Map<UUID, ? extends AbstractExchange> states = null;
                if (exgse.getDriverStateType() != null) {
                    states = ((OSH) this.getOSH()).getDriverRegistry().getData(exgse.getDriverStateType());
                }

                this.collector.updateStateView(((OSH) this.getOSH()).getDriverRegistry().getDataTypes(), states);
            }
        } else if (exchangeObject instanceof GUIEpsComExchange) {
            GUIEpsComExchange gece = (GUIEpsComExchange) exchangeObject;
            this.collector.updateGlobalSchedule(gece.getPriceSignals(), gece.getTimestamp());
        } else if (exchangeObject instanceof GUIPlsComExchange) {
            GUIPlsComExchange gpce = (GUIPlsComExchange) exchangeObject;
            this.collector.updateGlobalSchedule(gpce.getTimestamp(), gpce.getPowerLimitSignals());
        } else if (exchangeObject instanceof GUIWaterStorageComExchange) {
            GUIWaterStorageComExchange gwsce = (GUIWaterStorageComExchange) exchangeObject;
            WaterStorageOCSX gwsse = new WaterStorageOCSX(
                    gwsce.getDeviceID(),
                    gwsce.getTimestamp(),
                    gwsce.getCurrentTemp(),
                    gwsce.getMinTemp(),
                    gwsce.getMaxTemp(),
                    gwsce.getDemand(),
                    gwsce.getSupply(),
                    gwsce.getTankId());
            this.collector.updateWaterStorageData(gwsse);
        } else if (exchangeObject instanceof GUIHotWaterPredictionComExchange) {
            GUIHotWaterPredictionComExchange ghwpce = (GUIHotWaterPredictionComExchange) exchangeObject;
            this.collector.updateWaterPredictionData(
                    ghwpce.getPredictedTankTemp(), ghwpce.getPredictedHotWaterDemand(), ghwpce.getPredictedHotWaterSupply(), ghwpce.getTimestamp());
        } else if (exchangeObject instanceof GUIAncillaryMeterComExchange) {
            GUIAncillaryMeterComExchange gamce = (GUIAncillaryMeterComExchange) exchangeObject;

            this.collector.updateAncillaryMeter(gamce.getAncillaryMeter(), gamce.getTimestamp());
        } else if (exchangeObject instanceof GUIBatteryStorageComExchange) {
            GUIBatteryStorageComExchange gbsce = (GUIBatteryStorageComExchange) exchangeObject;
            BatteryStorageOCSX gbsse = new BatteryStorageOCSX(
                    gbsce.getDeviceID(),
                    gbsce.getTimestamp(),
                    gbsce.getCurrentStateOfCharge(),
                    gbsce.getMinStateOfCharge(),
                    gbsce.getMaxStateOfCharge(),
                    gbsce.getBatteryId());
            this.collector.updateBatteryStorageData(gbsse);
        } else if (exchangeObject instanceof DevicesPowerComExchange) {
            DevicesPowerComExchange dpsex = (DevicesPowerComExchange) exchangeObject;
            this.collector.updatePowerStates(dpsex.getTimestamp(), dpsex.getPowerStates());
        } else {
            this.getGlobalLogger().logError("unknown exchange data type: " + exchangeObject.getClass().getName());
        }
    }

    @Override
    public void stateViewerClassChanged(Class<? extends AbstractExchange> cls) {
        this.notifyComManager(
                new GUIStateSelectedComExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentEpochSecond(),
                        cls));
    }

    @Override
    public void stateViewerRegistryChanged(StateViewerRegistryEnum registry) {
        this.notifyComManager(
                new GUIStateRegistrySelectedComExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentEpochSecond(),
                        registry));
    }
}
