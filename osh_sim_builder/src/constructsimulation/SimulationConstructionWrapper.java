package constructsimulation;

import constructsimulation.datatypes.AppliancesTypes;
import constructsimulation.datatypes.EPSTypes;
import constructsimulation.datatypes.PLSTypes;

import javax.xml.datatype.XMLGregorianCalendar;
import java.util.UUID;

/**
 * temporary data storage wrapper for iterated construction of simulation packages
 *
 * @author Sebastian Kramer
 */
public class SimulationConstructionWrapper {

    //Initialise with default values
    private String resultFilePath;
    private XMLGregorianCalendar simulationStartTime = constructSimulationPackage.simPackage.simulationStartTime;
    private long simulationDuration = constructSimulationPackage.simPackage.simulationDuration;
    private int numberOfPersons = constructSimulationPackage.numberOfPersons;
    private boolean usePvHoll = constructSimulationPackage.usePVRealHOLL;
    private boolean useDachsCHP = constructSimulationPackage.useDachsCHP;
    private boolean useGasHeating = constructSimulationPackage.useGasHeating;
    private boolean useBattery = constructSimulationPackage.useBatteryStorage;
    private long randomSeed = constructSimulationPackage.simPackage.initialRandomSeed;
    private long optimizationMainRandomSeed = constructSimulationPackage.optimizationMainRandomSeed;
    private EPSTypes epsType = constructSimulationPackage.epsType;
    private PLSTypes plsType = constructSimulationPackage.plsType;
    private UUID epsProviderUUID = constructSimulationPackage.epsProviderUUID;
    private UUID plsProviderUUID = constructSimulationPackage.plsProviderUUID;

    private boolean[] genericAppliancesToSimulate = constructSimulationPackage.genericAppliancesToSimulate;
    private String[] appliances1stTDof = constructSimulationPackage.genericAppliances1stTDOF;

    public SimulationConstructionWrapper(String resultFilePath, XMLGregorianCalendar simulationStartTime, long simulationDuration,
                                         int numberOfPersons, boolean usePvHoll, boolean useDachsCHP, boolean useGasHeating, boolean useBattery, long randomSeed,
                                         long optimizationMainRandomSeed, EPSTypes epsType, PLSTypes plsType, UUID epsProviderUUID, UUID plsProviderUUID, boolean[] devicesToSimulate, AppliancesTypes genericDevicesToSimulate,
                                         String[] appliances1stTDof) {
        super();

        this.resultFilePath = resultFilePath;
        this.simulationStartTime = simulationStartTime;
        this.simulationDuration = simulationDuration;
        this.numberOfPersons = numberOfPersons;
        this.usePvHoll = usePvHoll;
        this.useDachsCHP = useDachsCHP;
        this.useGasHeating = useGasHeating;
        this.useBattery = useBattery;
        this.randomSeed = randomSeed;
        this.optimizationMainRandomSeed = optimizationMainRandomSeed;
        this.epsType = epsType;
        this.plsType = plsType;
        this.epsProviderUUID = epsProviderUUID;
        this.plsProviderUUID = plsProviderUUID;
        if (devicesToSimulate != null)
            this.genericAppliancesToSimulate = devicesToSimulate;
        else
            this.genericAppliancesToSimulate = genericDevicesToSimulate.getDeviceValues();

        this.appliances1stTDof = appliances1stTDof;
    }

    public SimulationConstructionWrapper() {
        super();
    }

    public void run() {
        //save old values
        XMLGregorianCalendar simulationStartTime = constructSimulationPackage.simPackage.simulationStartTime;
        long simulationDuration = constructSimulationPackage.simPackage.simulationDuration;
        int numberOfPersons = constructSimulationPackage.numberOfPersons;
        boolean usePvHoll = constructSimulationPackage.usePVRealHOLL;
        boolean useDachsCHP = constructSimulationPackage.useDachsCHP;
        boolean useGasHeating = constructSimulationPackage.useGasHeating;
        boolean useBattery = constructSimulationPackage.useBatteryStorage;
        long randomSeed = constructSimulationPackage.simPackage.initialRandomSeed;
        long optimizationMainRandomSeed = constructSimulationPackage.optimizationMainRandomSeed;
        EPSTypes epsType = constructSimulationPackage.epsType;
        PLSTypes plsType = constructSimulationPackage.plsType;
        UUID epsProviderUUID = constructSimulationPackage.epsProviderUUID;
        UUID plsProviderUUID = constructSimulationPackage.plsProviderUUID;
        boolean showGui = constructSimulationPackage.showGui;
        boolean[] genericAppliancesToSimulate = constructSimulationPackage.genericAppliancesToSimulate;
        String[] appliances1stTDof = constructSimulationPackage.genericAppliances1stTDOF;

        constructSimulationPackage.simPackage.simulationStartTime = this.simulationStartTime;
        constructSimulationPackage.simPackage.simulationDuration = this.simulationDuration;
        constructSimulationPackage.numberOfPersons = this.numberOfPersons;
        constructSimulationPackage.usePVRealHOLL = this.usePvHoll;
        constructSimulationPackage.useDachsCHP = this.useDachsCHP;
        constructSimulationPackage.useGasHeating = this.useGasHeating;
        constructSimulationPackage.useBatteryStorage = this.useBattery;
        constructSimulationPackage.simPackage.initialRandomSeed = this.randomSeed;
        constructSimulationPackage.optimizationMainRandomSeed = this.optimizationMainRandomSeed;
        constructSimulationPackage.epsType = this.epsType;
        constructSimulationPackage.plsType = this.plsType;
        constructSimulationPackage.epsProviderUUID = this.epsProviderUUID;
        constructSimulationPackage.showGui = false;
        constructSimulationPackage.genericAppliancesToSimulate = this.genericAppliancesToSimulate;
        constructSimulationPackage.genericAppliances1stTDOF = this.appliances1stTDof;

        constructSimulationPackage.generate(this.resultFilePath);

        //restore old values
        constructSimulationPackage.simPackage.simulationStartTime = simulationStartTime;
        constructSimulationPackage.simPackage.simulationDuration = simulationDuration;
        constructSimulationPackage.numberOfPersons = numberOfPersons;
        constructSimulationPackage.usePVRealHOLL = usePvHoll;
        constructSimulationPackage.useDachsCHP = useDachsCHP;
        constructSimulationPackage.useGasHeating = useGasHeating;
        constructSimulationPackage.useBatteryStorage = useBattery;
        constructSimulationPackage.simPackage.initialRandomSeed = randomSeed;
        constructSimulationPackage.optimizationMainRandomSeed = optimizationMainRandomSeed;
        constructSimulationPackage.epsType = epsType;
        constructSimulationPackage.plsType = plsType;
        constructSimulationPackage.epsProviderUUID = epsProviderUUID;
        constructSimulationPackage.showGui = showGui;
        constructSimulationPackage.genericAppliancesToSimulate = genericAppliancesToSimulate;
        constructSimulationPackage.genericAppliances1stTDOF = appliances1stTDof;
    }

    public String getResultFilePath() {
        return this.resultFilePath;
    }

    public void setResultFilePath(String resultFilePath) {
        this.resultFilePath = resultFilePath;
    }

    public XMLGregorianCalendar getSimulationStartTime() {
        return this.simulationStartTime;
    }

    public void setSimulationStartTime(XMLGregorianCalendar simulationStart) {
        this.simulationStartTime = simulationStart;
    }

    public long getSimulationDuration() {
        return this.simulationDuration;
    }

    public void setSimulationDuration(long simulationDuration) {
        this.simulationDuration = simulationDuration;
    }

    public int getNumberOfPersons() {
        return this.numberOfPersons;
    }

    public void setNumberOfPersons(int numberOfPersons) {
        this.numberOfPersons = numberOfPersons;
    }

    public boolean isUsePvHoll() {
        return this.usePvHoll;
    }

    public void setUsePvHoll(boolean usePvHoll) {
        this.usePvHoll = usePvHoll;
    }

    public boolean isUseDachsCHP() {
        return this.useDachsCHP;
    }

    public void setUseDachsCHP(boolean useDachsCHP) {
        this.useDachsCHP = useDachsCHP;
    }

    public boolean isUseGasHeating() {
        return this.useGasHeating;
    }

    public void setUseGasHeating(boolean useGas) {
        this.useGasHeating = useGas;
    }

    public boolean isUseBattery() {
        return this.useBattery;
    }

    public void setUseBattery(boolean useBattery) {
        this.useBattery = useBattery;
    }

    public long getRandomSeed() {
        return this.randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public long getOptimizationMainRandomSeed() {
        return this.optimizationMainRandomSeed;
    }

    public void setOptimizationMainRandomSeed(long optimizationMainRandomSeed) {
        this.optimizationMainRandomSeed = optimizationMainRandomSeed;
    }

    public EPSTypes getEpsType() {
        return this.epsType;
    }

    public void setEpsType(EPSTypes epsType) {
        this.epsType = epsType;
    }

    public PLSTypes getPlsType() {
        return this.plsType;
    }

    public void setPlsType(PLSTypes plsType) {
        this.plsType = plsType;
    }

    public UUID getEpsProviderUUID() {
        return this.epsProviderUUID;
    }

    public void setEpsProviderUUID(UUID epsProviderUUID) {
        this.epsProviderUUID = epsProviderUUID;
    }

    public boolean[] getGenericAppliancesToSimulate() {
        return this.genericAppliancesToSimulate;
    }

    public void setGenericAppliancesToSimulate(boolean[] genericAppliancesToSimulate) {
        this.genericAppliancesToSimulate = genericAppliancesToSimulate;
    }

    public String[] getAppliances1stTDof() {
        return this.appliances1stTDof;
    }

    public void setAppliances1stTDof(String[] appliances1stTDof) {
        this.appliances1stTDof = appliances1stTDof;
    }
}
