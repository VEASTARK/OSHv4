package osh.simulation;

import osh.utils.physics.PhysicalConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 * @author Ingo Mauser
 */
public class OSHSimulationResults extends SimulationResults {

    protected double activePowerConsumption;

    protected double activePowerPV;
    protected double activePowerPVAutoConsumption;
    protected double activePowerPVFeedIn;

    protected double activePowerCHP;
    protected double activePowerCHPAutoConsumption;
    protected double activePowerCHPFeedIn;

    protected double activePowerBatteryCharging;
    protected double activePowerBatteryDischarging;
    protected double activePowerBatteryAutoConsumption;
    protected double activePowerBatteryFeedIn;

    protected double activePowerExternal;

    protected double reactivePowerExternal;

    protected double gasPowerExternal;

    protected double epsCosts;
    protected double plsCosts;
    protected double gasCosts;
    protected double feedInCostsPV;
    protected double feedInCostsCHP;
    protected double totalCosts;
    protected double autoConsumptionCosts;

    public static String[] getDoubleArrayKeys() {
        String[] ret = new String[21];

        ret[0] = "ActivePowerConsumption";
        ret[1] = "ActivePowerPV";
        ret[2] = "ActivePowerPVAutoConsumption";
        ret[3] = "ActivePowerPVFeedIn";
        ret[4] = "ActivePowerCHP";
        ret[5] = "ActivePowerCHPAutoConsumption";
        ret[6] = "ActivePowerCHPFeedIn";
        ret[7] = "ActivePowerBatteryCharging";
        ret[8] = "ActivePowerBatteryDischarging";
        ret[9] = "ActivePowerBatteryAutoConsumption";
        ret[10] = "ActivePowerBatteryFeedIn";
        ret[11] = "ActivePowerExternal";
        ret[12] = "ReactivePowerExternal";
        ret[13] = "GasPowerExternal";
        ret[14] = "EpsCosts";
        ret[15] = "PlsCosts";
        ret[16] = "GasCosts";
        ret[17] = "FeedInCostsPV";
        ret[17] = "FeedInCostsPV";
        ret[18] = "FeedInCostsCHP";
        ret[19] = "AutoConsumptionCosts";
        ret[20] = "TotalCosts";

        return ret;
    }

    public double getTotalCosts() {
        return this.totalCosts;
    }

    public double getEpsCosts() {
        return this.epsCosts;
    }

    public double getPlsCosts() {
        return this.plsCosts;
    }

    public double getGasCosts() {
        return this.gasCosts;
    }

    public double getFeedInCostsPV() {
        return this.feedInCostsPV;
    }

    public double getFeedInCostsCHP() {
        return this.feedInCostsCHP;
    }

    public double getAutoConsumptionCosts() {
        return this.autoConsumptionCosts;
    }

    //only converting from Ws to kWh when getting values, will minimise errors due to fp-arithemtic
    public double getActivePowerConsumption() {
        return this.activePowerConsumption / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerPV() {
        return this.activePowerPV / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerCHP() {
        return this.activePowerCHP / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerPVAutoConsumption() {
        return this.activePowerPVAutoConsumption / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerPVFeedIn() {
        return this.activePowerPVFeedIn / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerCHPAutoConsumption() {
        return this.activePowerCHPAutoConsumption / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerCHPFeedIn() {
        return this.activePowerCHPFeedIn / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerExternal() {
        return this.activePowerExternal / PhysicalConstants.factor_wsToKWh;
    }

    public double getReactivePowerExternal() {
        return this.reactivePowerExternal / PhysicalConstants.factor_wsToKWh;
    }

    public double getGasPowerExternal() {
        return this.gasPowerExternal / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerBatteryCharging() {
        return this.activePowerBatteryCharging / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerBatteryDischarging() {
        return this.activePowerBatteryDischarging / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerBatteryAutoConsumption() {
        return this.activePowerBatteryAutoConsumption / PhysicalConstants.factor_wsToKWh;
    }

    public double getActivePowerBatteryFeedIn() {
        return this.activePowerBatteryFeedIn / PhysicalConstants.factor_wsToKWh;
    }

    public void addActivePowerConsumption(double additional) {
        this.activePowerConsumption += additional;
    }

    public void addActivePowerPV(double additional) {
        this.activePowerPV += additional;
    }

    public void addActivePowerPVAutoConsumption(double additional) {
        this.activePowerPVAutoConsumption += additional;
    }

    public void addActivePowerPVFeedIn(double additional) {
        this.activePowerPVFeedIn += additional;
    }

    public void addActivePowerCHP(double additional) {
        this.activePowerCHP += additional;
    }

    public void addActivePowerCHPAutoConsumption(double additional) {
        this.activePowerCHPAutoConsumption += additional;
    }

    public void addActivePowerCHPFeedIn(double additional) {
        this.activePowerCHPFeedIn += additional;
    }

    public void addActivePowerBatteryCharging(double additional) {
        this.activePowerBatteryCharging += additional;
    }

    public void addActivePowerBatteryDischarging(double additional) {
        this.activePowerBatteryDischarging += additional;
    }

    public void addActivePowerBatteryAutoConsumption(double additional) {
        this.activePowerBatteryAutoConsumption += additional;
    }

    public void addActivePowerBatteryFeedIn(double additional) {
        this.activePowerBatteryFeedIn += additional;
    }

    public void addActivePowerExternal(double additional) {
        this.activePowerExternal += additional;
    }

    public void addCostsToTotalCosts(double additional) {
        this.totalCosts += additional;
    }

    public void addEpsCostsToEpsCosts(double epsCosts) {
        this.epsCosts += epsCosts;
    }

    public void addPlsCostsToPlsCosts(double plsCosts) {
        this.plsCosts += plsCosts;
    }

    public void addFeedInCostsToFeedInCostsPV(double feedInCostsPV) {
        this.feedInCostsPV += feedInCostsPV;
    }

    public void addFeedInCostsToFeedInCostsCHP(double feedInCostsCHP) {
        this.feedInCostsCHP += feedInCostsCHP;
    }

    public void addReactivePowerExternal(double additional) {
        this.reactivePowerExternal += additional;
    }

    public void addGasPowerExternal(double additional) {
        this.gasPowerExternal += additional;
    }

    public void addGasCostsToGasCosts(double additional) {
        this.gasCosts += additional;
    }

    public void addAutoConsumptionCostsToAutoConsumptionCosts(double additional) {
        this.autoConsumptionCosts += additional;
    }

    public OSHSimulationResults clone() {
        OSHSimulationResults clone = new OSHSimulationResults();
        clone.activePowerCHP = this.activePowerCHP;
        clone.activePowerCHPAutoConsumption = this.activePowerCHPAutoConsumption;
        clone.activePowerCHPFeedIn = this.activePowerCHPFeedIn;
        clone.activePowerConsumption = this.activePowerConsumption;
        clone.activePowerExternal = this.activePowerExternal;
        clone.activePowerPV = this.activePowerPV;
        clone.activePowerPVAutoConsumption = this.activePowerPVAutoConsumption;
        clone.activePowerPVFeedIn = this.activePowerPVFeedIn;
        clone.activePowerBatteryCharging = this.activePowerBatteryCharging;
        clone.activePowerBatteryDischarging = this.activePowerBatteryDischarging;
        clone.activePowerBatteryAutoConsumption = this.activePowerBatteryAutoConsumption;
        clone.activePowerBatteryFeedIn = this.activePowerBatteryFeedIn;
        clone.epsCosts = this.epsCosts;
        clone.gasCosts = this.gasCosts;
        clone.gasPowerExternal = this.gasPowerExternal;
        clone.plsCosts = this.plsCosts;
        clone.reactivePowerExternal = this.reactivePowerExternal;
        clone.totalCosts = this.totalCosts;
        clone.feedInCostsPV = this.feedInCostsPV;
        clone.feedInCostsCHP = this.feedInCostsCHP;
        clone.autoConsumptionCosts = this.autoConsumptionCosts;

        return clone;
    }

    public void generateDiffToOtherResult(OSHSimulationResults result) {

        this.activePowerCHP = result.activePowerCHP - this.activePowerCHP;
        this.activePowerCHPAutoConsumption = result.activePowerCHPAutoConsumption - this.activePowerCHPAutoConsumption;
        this.activePowerCHPFeedIn = result.activePowerCHPFeedIn - this.activePowerCHPFeedIn;
        this.activePowerConsumption = result.activePowerConsumption - this.activePowerConsumption;
        this.activePowerExternal = result.activePowerExternal - this.activePowerExternal;
        this.activePowerPV = result.activePowerPV - this.activePowerPV;
        this.activePowerPVAutoConsumption = result.activePowerPVAutoConsumption - this.activePowerPVAutoConsumption;
        this.activePowerPVFeedIn = result.activePowerPVFeedIn - this.activePowerPVFeedIn;
        this.activePowerBatteryCharging = result.activePowerBatteryCharging - this.activePowerBatteryCharging;
        this.activePowerBatteryDischarging = result.activePowerBatteryDischarging - this.activePowerBatteryDischarging;
        this.activePowerBatteryAutoConsumption = result.activePowerBatteryAutoConsumption - this.activePowerBatteryAutoConsumption;
        this.activePowerBatteryFeedIn = result.activePowerBatteryFeedIn - this.activePowerBatteryFeedIn;
        this.epsCosts = result.epsCosts - this.epsCosts;
        this.gasCosts = result.gasCosts - this.gasCosts;
        this.gasPowerExternal = result.gasPowerExternal - this.gasPowerExternal;
        this.plsCosts = result.plsCosts - this.plsCosts;
        this.reactivePowerExternal = result.reactivePowerExternal - this.reactivePowerExternal;
        this.totalCosts = result.totalCosts - this.totalCosts;
        this.feedInCostsPV = result.feedInCostsPV - this.feedInCostsPV;
        this.feedInCostsCHP = result.feedInCostsCHP - this.feedInCostsCHP;
        this.autoConsumptionCosts = result.autoConsumptionCosts - this.autoConsumptionCosts;
    }

    public Double[] getContentsAsDoubleArray() {
        Double[] ret = new Double[21];

        ret[0] = this.getActivePowerConsumption();
        ret[1] = this.getActivePowerPV();
        ret[2] = this.getActivePowerPVAutoConsumption();
        ret[3] = this.getActivePowerPVFeedIn();
        ret[4] = this.getActivePowerCHP();
        ret[5] = this.getActivePowerCHPAutoConsumption();
        ret[6] = this.getActivePowerCHPFeedIn();
        ret[7] = this.getActivePowerBatteryCharging();
        ret[8] = this.getActivePowerBatteryDischarging();
        ret[9] = this.getActivePowerBatteryAutoConsumption();
        ret[10] = this.getActivePowerBatteryFeedIn();
        ret[11] = this.getActivePowerExternal();
        ret[12] = this.getReactivePowerExternal();
        ret[13] = this.getGasPowerExternal();
        ret[14] = this.epsCosts;
        ret[15] = this.plsCosts;
        ret[16] = this.gasCosts;
        ret[17] = this.feedInCostsPV;
        ret[18] = this.feedInCostsCHP;
        ret[19] = this.autoConsumptionCosts;
        ret[20] = this.totalCosts;

        return ret;
    }

    public void logCurrentStateToFile(File file, long runTime) throws FileNotFoundException {
        PrintWriter pwrFull = new PrintWriter(file);
        pwrFull.println("Runtime;" + runTime);
        String[] keys = OSHSimulationResults.getDoubleArrayKeys();
        Double[] values = this.getContentsAsDoubleArray();

        for (int i = 0; i < keys.length; i++) {
            pwrFull.println(keys[i] + ";" + values[i]);
        }

        pwrFull.close();
    }
}
