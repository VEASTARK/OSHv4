package osh.driver.simulation.batterylogic;

import osh.driver.simulation.batterystorage.BatteryStorage;
import osh.driver.simulation.inverter.InverterModel;

import java.util.List;

public abstract class IBatteryControlLogic {

    // ### helper methods ###
    protected static int calcMaxBatteryChargePower(double batteryMaxChargePower, InverterModel inverterModel) {
        int inverterMaxChargePower = inverterModel.getMaxChargePower();
//			int batteryMaxChargePower = batteryModel.getMaxChargePower();
        return (int) (Math.min(inverterMaxChargePower, batteryMaxChargePower));
    }

    protected static int calcMinBatteryChargePower(double batteryMinChargePower, InverterModel inverterModel) {
        int inverterMinChargePower = inverterModel.getMinChargePower();
//			int batteryMinChargePower = batteryModel.getMinChargePower();
        return (int) (Math.max(inverterMinChargePower, batteryMinChargePower));
    }

    protected static int calcMaxDischargePower(double batteryMaxDischargePower, InverterModel inverterModel) {
        int inverterMaxDischargePower = inverterModel.getMaxDischargePower();
//			int batteryMaxDischargePower = batteryModel.getMaxDischargePower();
        return (int) (Math.min(inverterMaxDischargePower, batteryMaxDischargePower));
    }

    protected static int calcMinDischargePower(double batteryMinDischargePower, InverterModel inverterModel) {
        int inverterMinDischargePower = inverterModel.getMinDischargePower();
//			int batteryMinDischargePower = batteryModel.getMinDischargePower();
        return (int) (Math.max(inverterMinDischargePower, batteryMinDischargePower));
    }

    public abstract List<Integer> calculateChargePowers(
            int batteryMaxChargePower,
            int batteryMinChargePower,
            int batteryMaxDischargePower,
            int batteryMinDischargePower,
            List<Integer> listOfControls,
            Long referenceTime,
            Long now);

    public abstract void doStupidBMS(
            int currentPowerAtGridConnection,
            BatteryStorage batteryModel,
            InverterModel inverterModel,
            int stepSize,
            int OptimizedMaxChargePower,
            int OptimizedMinChargePower,
            int OptimizedMaxDisChargePower,
            int OptimizedMinDisChargePower);

}