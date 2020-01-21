package osh.comdriver.simulation.cruisecontrol;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;
import osh.datatypes.cruisecontrol.OptimizedDataStorage;
import osh.datatypes.cruisecontrol.PowerSum;
import osh.datatypes.ea.Schedule;
import osh.datatypes.gui.DeviceTableEntry;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.power.AncillaryCommodityLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.localobserver.BatteryStorageOCSX;
import osh.datatypes.registry.oc.localobserver.WaterStorageOCSX;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author Till Schuberth, Kaibin Bao, Ingo Mauser, Jan Mueller, Sebastian Kramer
 */
public class GuiDataCollector {

    private final HashMap<UUID, TreeMap<Long, Double>> tankTemps = new HashMap<>();
    private final HashMap<UUID, TreeMap<Long, Double>> hotWaterDemands = new HashMap<>();
    private final HashMap<UUID, TreeMap<Long, Double>> hotWaterSupplies = new HashMap<>();
    //			new OptimizedDataStorage<GUIWaterStorageStateExchange>();
    private final HashMap<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> batteryStorageHistories =
            new HashMap<>();
    //			new OptimizedDataStorage<GUIWaterStorageStateExchange>();
    private final EnumMap<Commodity, OptimizedDataStorage<PowerSum>> powerHistory = new EnumMap<>(Commodity.class);
    private final HashMap<UUID, Long> lastWaterRefresh = new HashMap<>();
    private final HashMap<UUID, Long> lastBatteryRefresh = new HashMap<>();
    private final int removeOlderThan = 4 * 86400;
    private GuiMain driver;
    private List<Schedule> schedules;
    private EnumMap<AncillaryCommodity, PriceSignal> ps;
    private EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit;
    private HashMap<UUID, EnumMap<Commodity, Double>> powerStates;
    private boolean saveGraph;
    private TreeMap<Long, Double> predictedTankTemp = new TreeMap<>();
    private TreeMap<Long, Double> predictedHotWaterDemand = new TreeMap<>();
    private TreeMap<Long, Double> predictedHotWaterSupply = new TreeMap<>();

    public GuiDataCollector(GuiMain driver, boolean saveGraph) {
        if (driver == null) throw new NullPointerException("driver is null");

        this.saveGraph = saveGraph;
        this.driver = driver;
    }

    public void updateEADeviceList(Set<DeviceTableEntry> deviceList) {
        this.driver.refreshDeviceTable(deviceList);
    }

    public void updateStateView(Set<Class<? extends AbstractExchange>> types, Map<UUID, ? extends AbstractExchange> states) {
        this.driver.refreshStateTable(types, states);
    }

    private void waitForUser(long timestamp) {
        //in case we are waiting deliver an updated water diagram
        //this is a dirty hack, but who cares
        //SEKR: the one getting a nullpointerexception at the start, another dirty hack:
        //check if a powerhistory exists before updating
        if (this.powerHistory.containsKey(Commodity.ACTIVEPOWER))
            this.doRealPastUpdate(timestamp, "water");

        this.driver.waitForUserIfRequested();
    }

    public void updateGlobalSchedule(
            EnumMap<AncillaryCommodity, PriceSignal> ps,
            long timestamp) {
        this.ps = ps;
        this.driver.refreshDiagram(this.schedules, ps, this.pwrLimit, timestamp, this.saveGraph);
        this.waitForUser(timestamp);

    }

    public void updateGlobalSchedule(
            long timestamp,
            EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        this.pwrLimit = pwrLimit;
        this.driver.refreshDiagram(this.schedules, this.ps, pwrLimit, timestamp, this.saveGraph);
        this.waitForUser(timestamp);
    }

    public void updateGlobalSchedule(
            List<Schedule> schedules,
            long timestamp) {
        this.schedules = schedules;
        this.driver.refreshDiagram(schedules, this.ps, this.pwrLimit, timestamp, this.saveGraph);
        this.waitForUser(timestamp);
    }

    public void updateAncillaryMeter(
            AncillaryCommodityLoadProfile ancillaryMeter,
            long timestamp) {
        this.driver.refreshMeter(ancillaryMeter, timestamp);
        this.waitForUser(timestamp);
    }

    public synchronized void updateWaterStorageData(WaterStorageOCSX exws) {
        UUID tankId = exws.getTankId();

        if (this.tankTemps.get(tankId) == null) {
            this.tankTemps.put(tankId, new TreeMap<>());
            this.lastWaterRefresh.put(tankId, 0L);
        }
        this.tankTemps.get(tankId).put(exws.getTimestamp(), exws.getCurrentTemp());

        this.hotWaterDemands.computeIfAbsent(tankId, k -> new TreeMap<>());
        this.hotWaterDemands.get(tankId).put(exws.getTimestamp(), exws.getDemand());

        this.hotWaterSupplies.computeIfAbsent(tankId, k -> new TreeMap<>());
        this.hotWaterSupplies.get(tankId).put(exws.getTimestamp(), exws.getSupply());

        this.checkForPastUpdateOfWater(exws.getTimestamp());
    }

    public synchronized void updateWaterPredictionData(
            TreeMap<Long, Double> predictedTankTemp,
            TreeMap<Long, Double> predictedHotWaterDemand,
            TreeMap<Long, Double> predictedHotWaterSupply,
            long timestamp) {

        if (!predictedTankTemp.isEmpty()) {
            long predictionStart = predictedTankTemp.firstKey();
            this.predictedTankTemp.tailMap(predictionStart).clear();
            this.predictedTankTemp.putAll(predictedTankTemp);
        }
        if (!predictedHotWaterDemand.isEmpty()) {
            long predictionStart = predictedHotWaterDemand.firstKey();
            this.predictedHotWaterDemand.tailMap(predictionStart).clear();
            this.predictedHotWaterDemand.putAll(predictedHotWaterDemand);
        }
        if (!predictedHotWaterSupply.isEmpty()) {
            long predictionStart = predictedHotWaterSupply.firstKey();
            this.predictedHotWaterSupply.tailMap(predictionStart).clear();
            this.predictedHotWaterSupply.putAll(predictedHotWaterSupply);
        }

        this.checkForWaterPredictionHistoryCleanup(timestamp);
        this.driver.refreshWaterPredictionDiagram(this.predictedTankTemp, this.predictedHotWaterDemand, this.predictedHotWaterSupply);
        this.waitForUser(timestamp);
    }

    public synchronized void updateBatteryStorageData(BatteryStorageOCSX exbs) {
        UUID batteryId = exbs.getBatteryId();

        GUIBatteryStorageStateExchange guiBatteryStorageStateExchange =
                new GUIBatteryStorageStateExchange(
                        exbs.getSender(),
                        exbs.getTimestamp(),
                        exbs.getStateOfCharge(),
                        exbs.getMinStateOfCharge(),
                        exbs.getMaxStateOfCharge(),
                        batteryId);
        if (this.batteryStorageHistories.get(batteryId) == null) {
            this.batteryStorageHistories.put(batteryId, new OptimizedDataStorage<>());
            this.lastBatteryRefresh.put(batteryId, 0L);
        }
        this.batteryStorageHistories.get(batteryId).add(exbs.getTimestamp(), guiBatteryStorageStateExchange);
        this.checkForPastUpdateOfBattery(exbs.getTimestamp());
    }


    public void updatePowerStates(
            Long timestamp,
            HashMap<UUID, EnumMap<Commodity, Double>> powerStates) {
        EnumMap<Commodity, PowerSum> commodityPowerSum = new EnumMap<>(Commodity.class);

        this.powerStates = powerStates;

        if (powerStates != null) {
            for (Entry<UUID, EnumMap<Commodity, Double>> e : powerStates.entrySet()) {
                EnumMap<Commodity, Double> comMap = e.getValue();

                for (Entry<Commodity, Double> f : comMap.entrySet()) {
                    PowerSum existingSum = commodityPowerSum.get(f.getKey());
                    if (existingSum == null) {
                        existingSum = new PowerSum(0, 0, 0);
                    }
                    double posSum = existingSum.getPosSum();
                    double negSum = existingSum.getNegSum();
                    double sum = existingSum.getSum();

                    Double value = f.getValue();
                    if (value == null) {
                        value = 0.0;
                    }

                    // add to totalSum
                    sum += value;

                    if (value > 0) {
                        posSum += value;
                    } else if (value < 0) {
                        negSum += value;
                    }

                    PowerSum newSum = new PowerSum(posSum, negSum, sum);
                    commodityPowerSum.put(f.getKey(), newSum);
                } /* for( ... Commodity ... ) */
            } /* for( ... UUID ... ) */

            for (Entry<Commodity, PowerSum> ent : commodityPowerSum.entrySet()) {
                OptimizedDataStorage<PowerSum> timeSeries = this.powerHistory.get(ent.getKey());
                if (timeSeries == null) {
                    timeSeries = new OptimizedDataStorage<>();
                    this.powerHistory.put(ent.getKey(), timeSeries);
                }
                timeSeries.add(timestamp, ent.getValue());
            }
        }

        this.checkForPastUpdateOfWater(timestamp);
        this.checkForPastUpdateOfBattery(timestamp);
        this.checkForPowerHistoryCleanup(timestamp);
        this.checkForWaterStorageHistoryCleanup(timestamp);
        this.checkForBatteryStorageHistoryCleanup(timestamp);
    }

    private void checkForPastUpdateOfWater(long now) {
        if (!this.lastWaterRefresh.isEmpty()) {
            for (Entry<UUID, Long> e : this.lastWaterRefresh.entrySet()) {
                //refresh only every hour
                if (Math.abs(e.getValue() - now) > 3600) {
                    this.lastWaterRefresh.put(e.getKey(), now);
                    this.doRealPastUpdate(now, "water");
                }
            }
        }
    }

    private void checkForPastUpdateOfBattery(long now) {
        if (!this.lastBatteryRefresh.isEmpty()) {
            for (Entry<UUID, Long> e : this.lastBatteryRefresh.entrySet()) {
                //refresh only every hour
                if (Math.abs(e.getValue() - now) > 3600) {
                    this.lastBatteryRefresh.put(e.getKey(), now);
                    this.doRealPastUpdate(now, "battery");
                }
            }
        }
    }


    private synchronized void doRealPastUpdate(long timestamp, String type) {
        // WaterStorage
        if (type.equals("water")) {
            this.driver.refreshWaterDiagram(this.tankTemps, this.hotWaterDemands, this.hotWaterSupplies);
        }
        // BatteryStorage
        else if (type.equals("battery")) {
            HashMap<UUID, TreeMap<Long, GUIBatteryStorageStateExchange>> optimizedBatteryStorageHistories = new HashMap<>();
            for (Entry<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> e : this.batteryStorageHistories.entrySet()) {
                optimizedBatteryStorageHistories.put(e.getKey(), e.getValue().getMap());
            }
            this.driver.refreshBatteryDiagram(optimizedBatteryStorageHistories);
        }


        // PowerSum History
        EnumMap<Commodity, TreeMap<Long, PowerSum>> commodityPowerSum = new EnumMap<>(Commodity.class);

        for (Entry<Commodity, OptimizedDataStorage<PowerSum>> entry : this.powerHistory.entrySet()) {
            TreeMap<Long, PowerSum> powerSumSeries = entry.getValue().getMap();
            commodityPowerSum.put(entry.getKey(), powerSumSeries);
        }

        this.driver.refreshPowerSumDiagram(timestamp, commodityPowerSum);
    }

    private void checkForPowerHistoryCleanup(long timestamp) {
        if (timestamp % 3600 == 0) {

            synchronized (this.powerHistory) {
                for (OptimizedDataStorage<PowerSum> powerSumOptimizedDataStorage : this.powerHistory.values()) {
                    Map<Long, PowerSum> map = powerSumOptimizedDataStorage.getMap();

                    map.entrySet().removeIf(e -> e.getKey() < (timestamp - this.removeOlderThan));
                }
            }

        }
    }


    private void checkForWaterStorageHistoryCleanup(long timestamp) {
        if (timestamp % 86400 == 0) {
            synchronized (this.tankTemps) {
                for (Entry<UUID, TreeMap<Long, Double>> entry : this.tankTemps.entrySet()) {
                    TreeMap<Long, Double> newerValues = new TreeMap<>(entry.getValue().tailMap(timestamp - this.removeOlderThan));
                    this.tankTemps.put(entry.getKey(), newerValues);
                }
            }
            synchronized (this.hotWaterDemands) {
                for (Entry<UUID, TreeMap<Long, Double>> entry : this.hotWaterDemands.entrySet()) {
                    TreeMap<Long, Double> newerValues = new TreeMap<>(entry.getValue().tailMap(timestamp - this.removeOlderThan));
                    this.hotWaterDemands.put(entry.getKey(), newerValues);
                }
            }
            synchronized (this.hotWaterSupplies) {
                for (Entry<UUID, TreeMap<Long, Double>> entry : this.hotWaterSupplies.entrySet()) {
                    TreeMap<Long, Double> newerValues = new TreeMap<>(entry.getValue().tailMap(timestamp - this.removeOlderThan));
                    this.hotWaterSupplies.put(entry.getKey(), newerValues);
                }
            }
        }
    }

    private void checkForWaterPredictionHistoryCleanup(long timestamp) {
        if (timestamp % 86400 == 0) {
            synchronized (this.predictedTankTemp) {
                this.predictedTankTemp = new TreeMap<>(this.predictedTankTemp.tailMap(timestamp - this.removeOlderThan));
            }
            synchronized (this.predictedHotWaterDemand) {
                this.predictedHotWaterDemand = new TreeMap<>(this.predictedHotWaterDemand.tailMap(timestamp - this.removeOlderThan));
            }
            synchronized (this.predictedHotWaterSupply) {
                this.predictedHotWaterSupply = new TreeMap<>(this.predictedHotWaterSupply.tailMap(timestamp - this.removeOlderThan));
            }
        }
    }

    private void checkForBatteryStorageHistoryCleanup(long timestamp) {
        if (timestamp % 3600 == 0) {
            synchronized (this.batteryStorageHistories) {
                TreeMap<Long, GUIBatteryStorageStateExchange> optimizedBatteryStorageHistories;
                for (Entry<UUID, OptimizedDataStorage<GUIBatteryStorageStateExchange>> e : this.batteryStorageHistories.entrySet()) {
                    optimizedBatteryStorageHistories = (e.getValue().getMap());
                    optimizedBatteryStorageHistories.entrySet().removeIf(ex -> ex.getKey() < (timestamp - this.removeOlderThan));
                }
            }
        }
    }

}
