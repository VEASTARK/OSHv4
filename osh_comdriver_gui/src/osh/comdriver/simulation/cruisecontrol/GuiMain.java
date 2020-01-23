package osh.comdriver.simulation.cruisecontrol;

import bibliothek.gui.dock.common.CControl;
import bibliothek.gui.dock.common.CLocation;
import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.SingleCDockable;
import bibliothek.gui.dock.common.location.CContentAreaCenterLocation;
import bibliothek.gui.dock.common.location.TreeLocationRoot;
import bibliothek.gui.dock.common.theme.ThemeMap;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewer;
import osh.comdriver.simulation.cruisecontrol.stateviewer.StateViewerListener;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;
import osh.datatypes.cruisecontrol.PowerSum;
import osh.datatypes.ea.Schedule;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.AbstractExchange;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.Map.Entry;


/**
 * @author Till Schuberth, Ingo Mauser, Jan Mueller
 */
public class GuiMain {

    private final CControl control;
    private final CContentAreaCenterLocation normalLocation;

    private final CruiseControl cruiseControl;
    private final ScheduleDrawer scheduleDrawer;
    private final Map<UUID, WaterTemperatureDrawer> waterDrawer = new HashMap<>();
    private final Map<UUID, BatteryStateOfChargeDrawer> batteryDrawer = new HashMap<>();
    private final PowerDrawer powerSumsDrawer;
    private final DeviceTable deviceTable;
    private final StateViewer stateViewer;
    private final boolean isMultiThread;
    private AncillaryMeterDrawer ancillaryMeterDrawer;
    private WaterPredictionDrawer waterPredictionDrawer;

    /**
     * CONSTRUCTOR
     */
    public GuiMain(boolean isMultiThread) {
        this.isMultiThread = isMultiThread;

        JFrame rootFrame = new JFrame("OSH Simulation GUI");
        rootFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.control = new CControl(rootFrame);
        rootFrame.setLayout(new BorderLayout());
        rootFrame.add(this.control.getContentArea(), BorderLayout.CENTER);
        this.control.setTheme(ThemeMap.KEY_ECLIPSE_THEME);

        this.scheduleDrawer = createScheduleDrawer();
        this.powerSumsDrawer = this.createPowerSumsDrawer();
        this.deviceTable = createDeviceTable();
        this.stateViewer = createStateViewer();
        //cruiseControl must be the last one to be in front of the others
        this.cruiseControl = this.createCruiseControl();

        SingleCDockable scheduleDock = new DefaultSingleCDockable("scheduleDrawer", "Schedule", this.scheduleDrawer);
        SingleCDockable powerSumsDock = new DefaultSingleCDockable("powerSumsDrawer", this.powerSumsDrawer.getName(),
                this.powerSumsDrawer);
        SingleCDockable deviceTableDock = new DefaultSingleCDockable("deviceTable", "Device Table", this.deviceTable);
        SingleCDockable stateViewerDock = new DefaultSingleCDockable("stateViewer", "Registry State Viewer",
                this.stateViewer);
        SingleCDockable cruiseControlDock = new DefaultSingleCDockable("cruiseControl", "OSH Simulation GUI",
                this.cruiseControl);

        this.control.addDockable(scheduleDock);
        this.control.addDockable(powerSumsDock);
        this.control.addDockable(deviceTableDock);
        this.control.addDockable(stateViewerDock);
        this.control.addDockable(cruiseControlDock);

        this.normalLocation = CLocation.base().normal();

        scheduleDock.setLocation(this.normalLocation);
        powerSumsDock.setLocation(this.normalLocation.stack());

        TreeLocationRoot south = CLocation.base().normalSouth(0.4);

        deviceTableDock.setLocation(south);
        stateViewerDock.setLocation(south.stack());

        TreeLocationRoot north = CLocation.base().normalNorth(0.1);

        cruiseControlDock.setLocation(north);

        scheduleDock.setVisible(true);
        powerSumsDock.setVisible(true);
        deviceTableDock.setVisible(true);
        stateViewerDock.setVisible(true);
        cruiseControlDock.setVisible(true);

        rootFrame.pack();
        rootFrame.setBounds(50, 50, 1000, 700);
        rootFrame.setVisible(true);
    }

    private static ScheduleDrawer createScheduleDrawer() {
        return new ScheduleDrawer();
    }

    private static AncillaryMeterDrawer createAncillaryMeterDrawer() {
        return new AncillaryMeterDrawer();
    }

    private static WaterTemperatureDrawer createWaterDrawer(UUID id) {
        return new WaterTemperatureDrawer(id);
    }

    private static BatteryStateOfChargeDrawer createBatteryDrawer(UUID id) {
        return new BatteryStateOfChargeDrawer(id);
    }

    private static DeviceTable createDeviceTable() {
        return new DeviceTable();
    }

    private static StateViewer createStateViewer() {
        return new StateViewer();
    }

    ScheduleDrawer getScheduleDrawer() {
        return this.scheduleDrawer;
    }

    AncillaryMeterDrawer getAncillaryMeterDrawer() {
        return this.ancillaryMeterDrawer;
    }

    PowerDrawer getPowerSumsDrawer() {
        return this.powerSumsDrawer;
    }

    CruiseControl getCruiseControl() {
        return this.cruiseControl;
    }

    DeviceTable getDeviceTable() {
        return this.deviceTable;
    }

    StateViewer getStateViewer() {
        return this.stateViewer;
    }

    private PowerDrawer createPowerSumsDrawer() {
        return new PowerDrawer();
    }

    private CruiseControl createCruiseControl() {
        return new CruiseControl(!this.isMultiThread);
    }

    public void waitForUserIfRequested() {
        if (this.cruiseControl.isWait()) {
            this.cruiseControl.waitForGo();
        }
    }

    public void updateTime(long timestamp) {
        this.cruiseControl.updateTime(timestamp);
    }

    public void refreshDiagram(
            List<Schedule> schedules,
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pls,
            long time,
            boolean saveGraph) {

        if (this.cruiseControl.isUpdate()) {
            this.scheduleDrawer.refreshDiagram(
                    schedules,
                    ps,
                    pls,
                    time,
                    saveGraph);
        }
    }

    public void refreshMeter(
            AncillaryCommodityLoadProfile ancillaryMeter,
            long time) {

        if (this.ancillaryMeterDrawer == null) {
            this.ancillaryMeterDrawer = createAncillaryMeterDrawer();
            SingleCDockable ancillaryDock = new DefaultSingleCDockable("ancillaryMeterDrawer", "AncillaryMeter", this.ancillaryMeterDrawer);
            this.control.addDockable(ancillaryDock);
            ancillaryDock.setLocation(this.normalLocation.stack());
            ancillaryDock.setVisible(true);
        }
        this.ancillaryMeterDrawer.refreshDiagram(
                ancillaryMeter,
                time);
    }

    public void refreshDeviceTable(Set<DeviceTableEntry> entries) {
        this.deviceTable.refreshDeviceTable(entries);
    }

    public void refreshStateTable(Set<Class<? extends AbstractExchange>> types, Map<UUID, ? extends AbstractExchange> states) {
        this.stateViewer.showTypes(types);
        this.stateViewer.showStates(states);
    }

    public void refreshWaterDiagram(HashMap<UUID, TreeMap<Long, Double>> tankTemps, HashMap<UUID, TreeMap<Long, Double>> hotWaterDemands,
                                    HashMap<UUID, TreeMap<Long, Double>> hotWaterSupplies) {
        if (!tankTemps.isEmpty()) {
            for (Entry<UUID, TreeMap<Long, Double>> entry : tankTemps.entrySet()) {
                UUID key = entry.getKey();
                if (!entry.getValue().isEmpty()) {
                    if (!this.waterDrawer.containsKey(key)) {
                        WaterTemperatureDrawer drawer = createWaterDrawer(key);
                        this.waterDrawer.put(key, drawer);
                        SingleCDockable waterDock = new DefaultSingleCDockable("waterDrawer" + key, drawer.getName(),
                                drawer);
                        this.control.addDockable(waterDock);
                        waterDock.setLocation(this.normalLocation.stack());
                        waterDock.setVisible(true);
                    }
                    this.waterDrawer.get(key).refreshDiagram(entry.getValue(), hotWaterDemands.get(key), hotWaterSupplies.get(key));
                }
            }
        }
    }

    public void refreshWaterPredictionDiagram(TreeMap<Long, Double> predictedTankTemp,
                                              TreeMap<Long, Double> predictedHotWaterDemand,
                                              TreeMap<Long, Double> predictedHotWaterSupply) {
        if (!predictedTankTemp.isEmpty()) {
            if (this.waterPredictionDrawer == null) {
                this.waterPredictionDrawer = new WaterPredictionDrawer();
                SingleCDockable waterDock = new DefaultSingleCDockable("waterPredictionDrawer",
                        this.waterPredictionDrawer.getName(), this.waterPredictionDrawer);
                this.control.addDockable(waterDock);
                waterDock.setLocation(this.normalLocation.stack());
                waterDock.setVisible(true);
            }
            this.waterPredictionDrawer.refreshDiagram(predictedTankTemp, predictedHotWaterDemand, predictedHotWaterSupply);
        }
    }

    public void refreshBatteryDiagram(HashMap<UUID, TreeMap<Long, GUIBatteryStorageStateExchange>> optimizedStorageHistories) {
        if (!optimizedStorageHistories.isEmpty()) {
            for (Entry<UUID, TreeMap<Long, GUIBatteryStorageStateExchange>> e : optimizedStorageHistories.entrySet()) {
                if (!e.getValue().isEmpty()) {
                    if (!this.batteryDrawer.containsKey(e.getKey())) {
                        BatteryStateOfChargeDrawer drawer = createBatteryDrawer(e.getKey());
                        this.batteryDrawer.put(e.getKey(), drawer);
                        SingleCDockable batteryDock = new DefaultSingleCDockable("batteryDrawer" + e.getKey(),
                                drawer.getName(), drawer);
                        this.control.addDockable(batteryDock);
                        batteryDock.setLocation(this.normalLocation.stack());
                        batteryDock.setVisible(true);
                    }
                    this.batteryDrawer.get(e.getKey()).refreshDiagram(e.getValue());
                }
            }
        }
    }


    public void refreshPowerSumDiagram(long now, EnumMap<Commodity, TreeMap<Long, PowerSum>> commodityPowerSum) {
        if (!commodityPowerSum.isEmpty()) {
            this.powerSumsDrawer.refreshDiagram(now, commodityPowerSum);
        }
    }

    public void registerListener(StateViewerListener l) {
        this.stateViewer.registerListener(l);
    }

}
