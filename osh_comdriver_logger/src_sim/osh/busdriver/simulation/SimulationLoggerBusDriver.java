package osh.busdriver.simulation;

import osh.busdriver.LoggerBusDriver;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.DeviceTypes;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.limit.PowerLimitSignal;
import osh.datatypes.limit.PriceSignal;
import osh.datatypes.logger.LoggerAncillaryCommoditiesHALExchange;
import osh.datatypes.logger.LoggerDetailedCostsHALExchange;
import osh.datatypes.logger.LoggerEpsPlsHALExchange;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.DevicesPowerStateExchange;
import osh.eal.hal.exchange.IHALExchange;
import osh.hal.exchange.LoggerCommodityPowerHALExchange;
import osh.hal.exchange.LoggerDevicesPowerHALExchange;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class SimulationLoggerBusDriver extends LoggerBusDriver {

    private boolean firstLineSum = true;
    private boolean firstLineDetails = true;
    private boolean firstLineVirtualCommodities = true;
    private boolean firstLineEpsPls = true;


    /**
     * CONSTRUCTOR
     *
     * @param osh          general osh object reference
     * @param deviceID     unique device if dor this driver
     * @param driverConfig parameter configuration of this driver
     */
    public SimulationLoggerBusDriver(IOSH osh,
                                     UUID deviceID, OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

    }


    /**
     * Register to Timer for timed logging operations (logger gets data to log by itself)<br>
     * Register to DriverRegistry for logging operations trigger by Drivers
     */
    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getTimer().registerComponent(this, 1);
    }

    /**
     * Get things to log from O/C-layer
     *
     * @param exchangeObject log data
     */
    @SuppressWarnings("unused")
    @Override
    public void updateDataFromBusManager(IHALExchange exchangeObject) {
        long now = this.getTimer().getUnixTime();

        if (this.valueLoggerConfiguration != null && this.valueLoggerConfiguration.getIsValueLoggingToFileActive()) {
            if (exchangeObject instanceof LoggerCommodityPowerHALExchange) {

                LoggerCommodityPowerHALExchange lcphe = (LoggerCommodityPowerHALExchange) exchangeObject;

                CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                        lcphe.getDeviceID(),
                        lcphe.getTimestamp(),
                        lcphe.getPowerState(),
                        DeviceTypes.OTHER);

                if (this.firstLineSum) {
                    StringBuilder firstEntry = new StringBuilder();
                    for (Commodity c : Commodity.values()) {
                        firstEntry.append(c).append(";");
                    }
                    this.fileLog.logPower(firstEntry.toString());
                    this.firstLineSum = false;
                }

                StringBuilder entryLine = new StringBuilder();

                for (Commodity c : Commodity.values()) {
                    Double power = cpse.getPowerState(c);
                    if (power != null) {
                        entryLine.append(power).append(";");
                    } else {
                        entryLine.append("0;");
                    }
                }

                this.fileLog.logPower(entryLine.toString());
            } else if (exchangeObject instanceof LoggerDevicesPowerHALExchange) {

                LoggerDevicesPowerHALExchange ldphe = (LoggerDevicesPowerHALExchange) exchangeObject;

                DevicesPowerStateExchange dpse = new DevicesPowerStateExchange(
                        ldphe.getDeviceID(),
                        ldphe.getTimestamp(),
                        ldphe.getPowerStates());

                if (this.firstLineDetails) {
                    StringBuilder entryLine = new StringBuilder();

                    // induction cooktop
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("HB_").append(c).append(";");
                    }

                    // dishwasher
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("DW_").append(c).append(";");
                    }

                    // oven
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("OV_").append(c).append(";");
                    }

                    // dryer
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("DR_").append(c).append(";");
                    }

                    // washer
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("WM_").append(c).append(";");
                    }

                    // PV
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("PV_").append(c).append(";");
                    }

                    // baseload
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("BL_").append(c).append(";");
                    }

                    // CHP
                    for (Commodity c : Commodity.values()) {
                        entryLine.append("CHP_").append(c).append(";");
                    }

                    this.fileLog.logPowerDetails(entryLine.toString());
                    this.firstLineDetails = false;
                }

                HashMap<UUID, EnumMap<Commodity, Double>> map = dpse.getPowerStateMap();
                StringBuilder entryLine = new StringBuilder();

                // induction cooktop
                UUID hb = UUID.fromString("00000000-4D49-4D49-4948-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(hb) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(hb).get(c)).append(";");
                }
                // dishwasher
                UUID dw = UUID.fromString("00000000-4D49-4D49-4457-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(dw) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(dw).get(c)).append(";");
                }
                // oven
                UUID ov = UUID.fromString("00000000-4D49-4D49-4F56-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(ov) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(ov).get(c)).append(";");
                }

                // dryer
                UUID dr = UUID.fromString("00000000-4D49-4D49-5444-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(dr) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(dr).get(c)).append(";");
                }

                // washer
                UUID wm = UUID.fromString("00000000-4D49-4D49-574D-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(wm) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(wm).get(c)).append(";");
                }

                // PV
                UUID pv = UUID.fromString("484F4C4C-0000-0000-5056-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(pv) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(pv).get(c)).append(";");
                }

                // baseload
                UUID bl = UUID.fromString("00000000-0000-5348-424C-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(bl) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(bl).get(c)).append(";");
                }

                // CHP
                UUID chp_eshl = UUID.fromString("44414348-5300-0043-4850-000000000000");
                for (Commodity c : Commodity.values()) {
                    if (map.get(chp_eshl) == null) {
                        entryLine.append("0;");
                    } else
                        entryLine.append(map.get(chp_eshl).get(c)).append(";");
                }

                this.fileLog.logPowerDetails(entryLine.toString());
            } else if (exchangeObject instanceof LoggerAncillaryCommoditiesHALExchange) {
                LoggerAncillaryCommoditiesHALExchange lvche = (LoggerAncillaryCommoditiesHALExchange) exchangeObject;
                EnumMap<AncillaryCommodity, Integer> map = lvche.getMap();

                if (this.firstLineVirtualCommodities) {
                    StringBuilder entryLine = new StringBuilder("timestamp;");

                    for (AncillaryCommodity vc : AncillaryCommodity.values()) {
                        entryLine.append(vc.toString()).append(";");
                    }

                    this.fileLog.logAncillaryCommodityPowerDetails(entryLine.toString());
                    this.firstLineVirtualCommodities = false;
                }

                StringBuilder entryLine = new StringBuilder(now + ";");
                this.constructAncillaryCommodityLogString(entryLine, map);

                this.fileLog.logAncillaryCommodityPowerDetails(entryLine.toString());
            } else if (exchangeObject instanceof LoggerEpsPlsHALExchange) {
                LoggerEpsPlsHALExchange lephe = (LoggerEpsPlsHALExchange) exchangeObject;

                if (this.firstLineEpsPls) {
                    StringBuilder entryLine = new StringBuilder();

                    this.constructHeaderLogString(entryLine);

                    this.fileLog.logExternalSignals(entryLine.toString());
                    this.firstLineEpsPls = false;
                }

                StringBuilder entryLine = new StringBuilder();

                this.constructPsPlsLogString(now, entryLine, lephe.getPs(), lephe.getPwrLimit());

                this.fileLog.logExternalSignals(entryLine.toString());
            } else if (exchangeObject instanceof LoggerDetailedCostsHALExchange) {
                LoggerDetailedCostsHALExchange lvche = (LoggerDetailedCostsHALExchange) exchangeObject;
                EnumMap<AncillaryCommodity, Integer> map = lvche.getPowerValueMap();

                if (this.firstLineVirtualCommodities) {
                    StringBuilder entryLine = new StringBuilder("timestamp;");

                    for (AncillaryCommodity vc : AncillaryCommodity.values()) {
                        entryLine.append(vc.toString()).append(";");
                    }

                    this.constructHeaderLogString(entryLine);

                    // total costs
                    entryLine.append("totalEPScosts");

                    this.fileLog.logCostDetailed(entryLine.toString());
                    this.firstLineVirtualCommodities = false;
                }

                StringBuilder entryLine = new StringBuilder(now + ";");
                this.constructAncillaryCommodityLogString(entryLine, map);

                this.constructPsPlsLogString(now, entryLine, lvche.getPs(), lvche.getPwrLimit());

                // total costs
                double costs = 0.0;

                {
                    // EPS
                    double additional = 0;

                    double currentActivePowerExternal = 0;
                    if (lvche.getPowerValueMap().get(AncillaryCommodity.ACTIVEPOWEREXTERNAL) != null
                            && lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null
                            && lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
                        currentActivePowerExternal = lvche.getPowerValueMap().get(AncillaryCommodity.ACTIVEPOWEREXTERNAL)
                                + lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN)
                                + lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN);
                    }

                    double currentActivePowerPv = 0;
                    if (lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null
                            && lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION) != null) {
                        currentActivePowerPv = lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERFEEDIN)
                                + lvche.getPowerValueMap().get(AncillaryCommodity.PVACTIVEPOWERAUTOCONSUMPTION);
                    }

                    double currentActivePowerChp = 0;
                    if (lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null
                            && lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION) != null) {
                        currentActivePowerPv = lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN)
                                + lvche.getPowerValueMap().get(AncillaryCommodity.CHPACTIVEPOWERAUTOCONSUMPTION);
                    }

                    // FIXME

//					additional = additional + EPSCostCalculator.calcEpsOptimizationObjective3(
//							now, 
//							1.0 / 3600000.0, 
//							lvche.getPs(), 
//							currentActivePowerExternal, 
//							currentActivePowerPv, 
//							currentActivePowerChp);

                    double currentGasPower = 0;
                    if (lvche.getPowerValueMap().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL) != null) {
                        currentGasPower = lvche.getPowerValueMap().get(AncillaryCommodity.NATURALGASPOWEREXTERNAL);
                    }

                    // gas
//					additional = additional + EPSCostCalculator.calcNaturalGasPower(
//							now, 
//							1.0 / 3600000.0, 
//							lvche.getPs(), 
//							currentGasPower);

                    costs += additional;

                    //PLS
                    //FIXME


                }

                entryLine.append(costs);

                this.fileLog.logCostDetailed(entryLine.toString());
            }

        }

    }

    private void constructHeaderLogString(StringBuilder entryLine) {
        for (AncillaryCommodity vc : AncillaryCommodity.values()) {
            entryLine.append("EPS_").append(vc.toString()).append(";");
        }

        for (AncillaryCommodity vc : AncillaryCommodity.values()) {
            entryLine.append("PLS_upper_").append(vc.toString()).append(";");
            entryLine.append("PLS_lower_").append(vc.toString()).append(";");
        }
    }

    private void constructPsPlsLogString(long now, StringBuilder entryLine,
                                         EnumMap<AncillaryCommodity, PriceSignal> ps2,
                                         EnumMap<AncillaryCommodity, PowerLimitSignal> pwrLimit) {
        for (AncillaryCommodity vc : AncillaryCommodity.values()) {

            double value = 0.0;
            if (ps2 != null) {
                PriceSignal ps = ps2.get(vc);
                if (ps != null) {
                    value = ps.getPrice(now);
                }
            }

            entryLine.append(value).append(";");
        }

        for (AncillaryCommodity vc : AncillaryCommodity.values()) {

            double uValue = 0.0;
            double lValue = 0.0;
            if (pwrLimit != null) {
                PowerLimitSignal pls = pwrLimit.get(vc);
                if (pls != null) {
                    uValue = pls.getPowerUpperLimit(now);
                    lValue = pls.getPowerLowerLimit(now);
                }
            }

            entryLine.append(uValue).append(";").append(lValue).append(";");
        }
    }

    private void constructAncillaryCommodityLogString(StringBuilder entryLine,
                                                      EnumMap<AncillaryCommodity, Integer> map) {
        for (AncillaryCommodity vc : AncillaryCommodity.values()) {
            Integer value = map.get(vc);
            if (value == null) {
                value = 0;
            }
            entryLine.append(value).append(";");
        }
    }
}
