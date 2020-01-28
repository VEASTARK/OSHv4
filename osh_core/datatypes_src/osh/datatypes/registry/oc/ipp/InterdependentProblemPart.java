package osh.datatypes.registry.oc.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.StateExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EAPredictionCommandExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.AbstractVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.IPPSolutionHandler;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.AbstractEncodedVariableInformation;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableEncoding;
import osh.esc.IOCEnergySubject;
import osh.esc.LimitedCommodityStateMap;

import java.io.Serializable;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public abstract class InterdependentProblemPart<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends StateExchange
        implements IOCEnergySubject, Serializable {

    private static final long serialVersionUID = 1852491971934065038L;
    public IGlobalLogger logger;

    //## input & output states ##

    protected LimitedCommodityStateMap interdependentInputStates;
    //states for internal use (so we dont have to constantly instantiate new Maps (time intensive)
    protected LimitedCommodityStateMap internalInterdependentOutputStates;
    //"Real" output states that will be given to the ESC
    private LimitedCommodityStateMap interdependentOutputStates;
    protected EnumSet<Commodity> allOutputCommodities;
    //as most ipps will not require input commodities we can leave that as an empty array and overwrite when necessary
    private EnumSet<Commodity> allInputCommodities = EnumSet.noneOf(Commodity.class);
    protected AncillaryMeterState ancillaryMeterState;

    //## time ##
    private long interdependentTime;
    private int stepSize;
    private long referenceTime;

    //How to compress the load profiles used
    protected LoadProfileCompressionTypes compressionType;
    protected int compressionValue;

    //## solutions ##
    protected IPPSolutionHandler solutionHandler;
    protected DecodedSolutionWrapper currentSolution;
    private double interdependentCervisia;

    private UUID deviceID;

    //## information about needed information of this ipp ##
    private boolean toBeScheduled;
    /**
     * flag if problem part needs ancillary meter state as an input state
     */
    private boolean needsAncillaryMeterState;
    /**
     * flag if problem part needs input (is interdependent and thus dynamic),
     * if false part will not receive any input states
     */
    private boolean reactsToInputStates;
    /**
     * flag if the problem part is completely static and will provide no load values
     */
    private boolean isCompletelyStatic;

    private int id;
    // necessary to distinguish PV power / CHP power / device power / appliance power for pricing
    private DeviceTypes deviceType;
    private transient SparseLoadProfile loadProfile;


    /**
     * for Serialization, never use
     */
    @Deprecated
    protected InterdependentProblemPart() {
        super();
        this.logger = null;
    }

    /**
     * CONSTRUCTOR
     */
    public InterdependentProblemPart(
            UUID deviceId,
            IGlobalLogger logger,
            long timestamp,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            boolean isCompletelyStatic,
            long referenceTime,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue,
            AbstractVariableTranslator<BitSet> binaryTranslator,
            AbstractVariableTranslator<double[]> realTranslator) {

        super(deviceId, timestamp);

        this.deviceID = deviceId;

        this.logger = logger;

        this.toBeScheduled = toBeScheduled;
        this.needsAncillaryMeterState = needsAncillaryMeterState;
        this.reactsToInputStates = reactsToInputStates;
        this.isCompletelyStatic = isCompletelyStatic;
        this.referenceTime = referenceTime;
        this.deviceType = deviceType;
        this.allOutputCommodities = allOutputCommodities;
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
        this.internalInterdependentOutputStates = new LimitedCommodityStateMap(allOutputCommodities);
        this.solutionHandler = new IPPSolutionHandler(binaryTranslator, realTranslator);
    }

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        this.stepSize = stepSize;
        if (createLoadProfile)
            this.loadProfile = new SparseLoadProfile();
        else
            this.loadProfile = null;

        if (this.referenceTime != maxReferenceTime) {
            this.recalculateEncoding(maxReferenceTime, this.getOptimizationHorizon());
        }

        this.interdependentCervisia = 0.0;

        this.interdependentTime = maxReferenceTime;
        this.interdependentOutputStates = null;

        if (this.internalInterdependentOutputStates != null) {
            this.internalInterdependentOutputStates.clear();
        } else {
            this.internalInterdependentOutputStates = new LimitedCommodityStateMap(this.allOutputCommodities);
        }

        this.interdependentInputStates = null;
        this.ancillaryMeterState = null;
    }

    /**
     * Transform to phenotype without interdependencies with other devices,
     * i.e., estimate the behavior!
     * <br>
     * IMPORTANT: Normally, use getFinalInterdependentSchedule() after
     * doing the simulation stepwise.
     */
    public abstract ISolution transformToPhenotype(DecodedSolutionWrapper solution);


    /**
     * Get final (full, complete) schedule that depends on other devices
     * and remove other schedules
     */
    public abstract Schedule getFinalInterdependentSchedule();


    /**
     * Get final phenotype that depends on other devices
     */
    public final EASolutionCommandExchange<PhenotypeType> transformToFinalInterdependentPhenotype(
            UUID sender,
            UUID receiver,
            long timestamp) {
        return new EASolutionCommandExchange<>(
                sender,
                receiver,
                timestamp,
                this.transformToFinalInterdependentPhenotype());
    }

    /**
     * Get final phenotype that depends on other devices
     */
    public abstract PhenotypeType transformToFinalInterdependentPhenotype();


    /**
     * Get final prediction that depends on other devices
     */
    public final EAPredictionCommandExchange<PredictionType> transformToFinalInterdependentPrediction(
            UUID sender,
            UUID receiver,
            long timestamp) {
        return new EAPredictionCommandExchange<>(
                sender,
                receiver,
                timestamp,
                this.transformToFinalInterdependentPrediction());
    }

    /**
     * Get final prediction that depends on other devices
     */
    public abstract PredictionType transformToFinalInterdependentPrediction();


    /**
     * Is invoked before every reschedule:
     * the IPP can adapts its current encoding to the current time
     * (e.g., reduce number of bits and change control model)
     *
     * @param currentTime - absolute time
     * @param maxHorizon  - absolute time
     */
    public abstract void recalculateEncoding(long currentTime, long maxHorizon);

    @Override
    public void setCommodityInputStates(LimitedCommodityStateMap inputStates,
                                        AncillaryMeterState ancillaryMeterState) {
        this.interdependentInputStates = inputStates;
        this.ancillaryMeterState = ancillaryMeterState;

    }

    /**
     * Sets the current solution of this problem-part to the decoded variables of the given binary encoded solution.
     *
     * @param encodedSolution the binary encoded solution
     */
    public void setSolution(BitSet encodedSolution) {
        this.currentSolution = this.solutionHandler.decode(encodedSolution);
        this.interpretNewSolution();
    }

    /**
     * Sets the current solution of this problem-part to the decoded variables of the given real encoded solution.
     *
     * @param encodedSolution the real encoded solution
     */
    public void setSolution(double[] encodedSolution) {
        this.currentSolution = this.solutionHandler.decode(encodedSolution);
        this.interpretNewSolution();
    }

    /**
     * Interprets the newly set solution.
     */
    protected abstract void interpretNewSolution();

    @Override
    public LimitedCommodityStateMap getCommodityOutputStates() {
        return this.interdependentOutputStates;
    }

    public void setOutputStates(LimitedCommodityStateMap states) {
        this.interdependentOutputStates = states;
    }

    // Getters & Setters

    public IGlobalLogger getGlobalLogger() {
        return this.logger;
    }

    public boolean isToBeScheduled() {
        return this.toBeScheduled;
    }

    @Override
    public boolean isNeedsAncillaryMeterState() {
        return this.needsAncillaryMeterState;
    }

    @Override
    public boolean isReactsToInputStates() {
        return this.reactsToInputStates;
    }

    @Override
    public void prepareForDeepCopy() {}

    public boolean isCompletelyStatic() {
        return this.isCompletelyStatic;
    }

    public EnumSet<Commodity> getAllOutputCommodities() {
        return this.allOutputCommodities;
    }

    public EnumSet<Commodity> getAllInputCommodities() {
        return this.allInputCommodities;
    }

    public void setAllInputCommodities(EnumSet<Commodity> allInputCommodities) {
        this.allInputCommodities = allInputCommodities;
    }

    public long getReferenceTime() {
        return this.referenceTime;
    }

    public void setReferenceTime(long referenceTime) {
        this.referenceTime = referenceTime;
    }

    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    @Override
    public UUID getUUID() {
        return this.deviceID;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the solution handler associated with this problem-part.
     * @return the solution handler associated with this problem-part
     */
    public IPPSolutionHandler getSolutionHandler() {
        return this.solutionHandler;
    }

    /**
     * Returns specific encoding information about the variables this problem-part needs for the optimization loop.
     *
     * @param variableEncoding the encoding for which information is required
     * @return information about the required encoding
     */
    public AbstractEncodedVariableInformation getVariableInformation(VariableEncoding variableEncoding) {
        return this.solutionHandler.getVariableInformation(variableEncoding);
    }

    /**
     * Returns a load profile containing all emitted commodities of this problem-part over the optimization loop.
     *
     * @return a load profile containing all emitted commodities
     */
    public SparseLoadProfile getLoadProfile() {
        return this.loadProfile;
    }

    /**
     * Sets the load profile representing all emitted commodities of this problem-part to the given value
     *
     * @param loadProfile the new load profile representing all emitted commodities of this problem-part
     */
    public void setLoadProfile(SparseLoadProfile loadProfile) {
        this.loadProfile = loadProfile;
    }

    /**
     * Returns the additional costs incurred by this problem-part over the optimization loop (e.g. start-stop costs).
     *
     * @return the additional costs incurred by this problem-part
     */
    public double getInterdependentCervisia() {
        return this.interdependentCervisia;
    }

    /**
     * Adds the given value to the additonal costs of this problem-part.
     *
     * @param add the additional value
     */
    public void addInterdependentCervisia(double add) {
        this.interdependentCervisia += add;
    }

    /**
     * Returns the current simulated time this problem-part is at inside the optimization loop.
     *
     * @return the current simulated time of this problem-part
     */
    public long getInterdependentTime() {
        return this.interdependentTime;
    }

    /**
     * Increments the current simulation time of this problem-part by the set step-size of the optimization loop.
     */
    public void incrementInterdependentTime() {
        this.interdependentTime += this.stepSize;
    }

    /**
     * Returns the time-step-size of the optimization loop this problem-part is part of.
     *
     * @return the time-step-size of the optimization loop
     */
    public int getStepSize() {
        return this.stepSize;
    }

    /**
     * Returns a string representation of this problem-part.
     *
     * @return a string representation of this problem-part
     * @return a string representation of this problem-part
     */
    public abstract String problemToString();

    @Override
    public String toString() {
        return this.problemToString();
    }

}
