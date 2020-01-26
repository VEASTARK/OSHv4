package osh.esc;

import osh.datatypes.commodity.Commodity;

import java.io.Serializable;
import java.util.*;

/**
 * @author Sebastian Kramer
 */
public class LimitedCommodityStateMap implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 9167758959788863484L;
    private static final byte thermalTempIndex = 0;
    private static final Commodity[] allCommodities = Commodity.values();
    private static final int commodityCount = Commodity.values().length;
    private static final boolean[] EMPTY_KEYSET = new boolean[commodityCount];
    static {
        Arrays.fill(EMPTY_KEYSET, false);
    }

    private static final Commodity[] allElectricalCommodities = {
            Commodity.ACTIVEPOWER,
            Commodity.REACTIVEPOWER,
    };
    private static final List<Commodity> allElectricCommoditiesList = new ArrayList<>();

    static {
        Collections.addAll(allElectricCommoditiesList, allElectricalCommodities);
    }

    private double[] powers;
    /*
     * 0 = voltage
     */
    private double[][] addElectrical;
    /*
     * 0 = temperature
     * 1 = mass flow
     */
    private double[][] addThermal;
    private final int[] ordinalToPowerMap = new int[commodityCount];
    private final int[] ordinalToElectricityMap = new int[commodityCount];
    private final int[] ordinalToThermalMap = new int[commodityCount];
    private final boolean[] keySet = new boolean[commodityCount];
    private int modCount;

    public LimitedCommodityStateMap() {

        this.powers = new double[commodityCount];
        for (int i = 0; i < commodityCount; i++) {
            this.ordinalToElectricityMap[i] = -1;
            this.ordinalToThermalMap[i] = -1;
        }
        int electricParts = allElectricCommoditiesList.size();
        this.addElectrical = new double[electricParts][1];
        this.addThermal = new double[commodityCount - electricParts][2];
        int electricCount = 0, thermalCount = 0;

        for (int i = 0; i < commodityCount; i++) {
            Commodity c = allCommodities[i];

            this.ordinalToPowerMap[c.ordinal()] = i;

            if (allElectricCommoditiesList.contains(c)) {
                this.ordinalToElectricityMap[c.ordinal()] = electricCount++;
            } else {
                this.ordinalToThermalMap[c.ordinal()] = thermalCount++;
            }
        }
    }

    public LimitedCommodityStateMap(EnumSet<Commodity> allPossibleCommodities) {

        this.powers = new double[allPossibleCommodities.size()];
        for (int i = 0; i < commodityCount; i++) {
            this.ordinalToPowerMap[i] = -1;
            this.ordinalToElectricityMap[i] = -1;
            this.ordinalToThermalMap[i] = -1;
        }
        int electricCount = 0, thermalCount = 0, i = 0;

        for (Commodity c : allPossibleCommodities) {
            this.ordinalToPowerMap[c.ordinal()] = i;

            if (allElectricCommoditiesList.contains(c)) {
                this.ordinalToElectricityMap[c.ordinal()] = electricCount++;
            } else {
                this.ordinalToThermalMap[c.ordinal()] = thermalCount++;
            }
            i++;
        }

        this.addElectrical = new double[electricCount][1];
        this.addThermal = new double[thermalCount][2];
    }

    public LimitedCommodityStateMap(Commodity[] allPossibleCommodities) {

        this.powers = new double[allPossibleCommodities.length];
        for (int i = 0; i < commodityCount; i++) {
            this.ordinalToPowerMap[i] = -1;
            this.ordinalToElectricityMap[i] = -1;
            this.ordinalToThermalMap[i] = -1;
        }
        int electricCount = 0, thermalCount = 0;

        for (int i = 0; i < allPossibleCommodities.length; i++) {
            Commodity c = allPossibleCommodities[i];

            this.ordinalToPowerMap[c.ordinal()] = i;

            if (allElectricCommoditiesList.contains(c)) {
                this.ordinalToElectricityMap[c.ordinal()] = electricCount++;
            } else {
                this.ordinalToThermalMap[c.ordinal()] = thermalCount++;
            }
        }

        this.addElectrical = new double[electricCount][1];
        this.addThermal = new double[thermalCount][2];
    }

    public LimitedCommodityStateMap(List<Commodity> allPossibleCommodities) {

        this.powers = new double[allPossibleCommodities.size()];
        for (int i = 0; i < commodityCount; i++) {
            this.ordinalToPowerMap[i] = -1;
            this.ordinalToElectricityMap[i] = -1;
            this.ordinalToThermalMap[i] = -1;
        }
        int electricityCount = 0, thermalCount = 0;

        for (int i = 0; i < allPossibleCommodities.size(); i++) {
            Commodity c = allPossibleCommodities.get(i);

            this.ordinalToPowerMap[c.ordinal()] = i;

            if (allElectricCommoditiesList.contains(c)) {
                this.ordinalToElectricityMap[c.ordinal()] = electricityCount++;
            } else {
                this.ordinalToThermalMap[c.ordinal()] = thermalCount++;
            }
        }

        this.addElectrical = new double[electricityCount][1];
        this.addThermal = new double[thermalCount][2];
    }

    public double getPower(Commodity c) {
        return this.keySet[c.ordinal()] ? this.powers[this.ordinalToPowerMap[c.ordinal()]] : 0;
    }

    public double getPowerWithoutCheck(Commodity c) {
        return this.powers[this.ordinalToPowerMap[c.ordinal()]];
    }

    public void addPower(Commodity c, double power) {
        this.powers[this.ordinalToPowerMap[c.ordinal()]] += power;
    }

    public double[] getAdditionalElectrical(Commodity c) {
        return this.keySet[c.ordinal()] ? this.addElectrical[this.ordinalToElectricityMap[c.ordinal()]] : null;
    }

    public double[] getAdditionalThermal(Commodity c) {
        return this.keySet[c.ordinal()] ? this.addThermal[this.ordinalToThermalMap[c.ordinal()]] : null;
    }

    public double getTemperature(Commodity c) {
        return this.keySet[c.ordinal()] ? this.addThermal[this.ordinalToThermalMap[c.ordinal()]][thermalTempIndex] : 0;
    }

    public double getTemperatureWithoutCheck(Commodity c) {
        return this.addThermal[this.ordinalToThermalMap[c.ordinal()]][thermalTempIndex];
    }

    public void setPower(Commodity c, double power) {
        this.powers[this.ordinalToPowerMap[c.ordinal()]] = power;
        this.keySet[c.ordinal()] = true;
        this.modCount++;
    }

    public void setOrAddPower(Commodity c, double power) {
        if (this.keySet[c.ordinal()]) {
            this.powers[this.ordinalToPowerMap[c.ordinal()]] += power;
        } else {
            this.powers[this.ordinalToPowerMap[c.ordinal()]] = power;
            this.keySet[c.ordinal()] = true;
        }
        this.modCount++;
    }

    public void setAllThermal(Commodity c, double power, double[] addThermal) {
        this.powers[this.ordinalToPowerMap[c.ordinal()]] = power;
        this.addThermal[this.ordinalToThermalMap[c.ordinal()]] = addThermal;
        this.keySet[c.ordinal()] = true;
        this.modCount++;
    }

    public void setTemperature(Commodity c, double temp) {
        this.addThermal[this.ordinalToThermalMap[c.ordinal()]][0] = temp;
        this.keySet[c.ordinal()] = true;
        this.modCount++;
    }

    public boolean containsCommodity(Commodity c) {
        return this.keySet[c.ordinal()];
    }

    public void resetCommodity(Commodity c) {
        this.keySet[c.ordinal()] = false;
    }

    public boolean isEmpty() {
        return this.modCount == 0;
    }

    public void clear() {
//		Arrays.fill(powers, 0);
//		Arrays.fill(addElectrical, new double[1]);
//		Arrays.fill(addThermal, new double[2]);
//		Arrays.fill(keySet, false);
        ArrayUtils.fillArrayBoolean(this.keySet, false);
        this.modCount = 0;
    }
}
