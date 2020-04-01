package osh.datatypes.registry.oc.ipp;

import osh.configuration.oc.EAObjectives;
import osh.configuration.oc.VariableEncoding;
import osh.configuration.system.DeviceTypes;
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
import osh.esc.IOCEnergySubject;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.dataStructures.Enum2DoubleMap;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public abstract class InterdependentProblemPart<PhenotypeType extends ISolution, PredictionType extends IPrediction>
        extends StateExchange
        implements IOCEnergySubject {

    /**
     * Input energy states received from the energy-simulation-core.
     */
    protected LimitedCommodityStateMap interdependentInputStates;
    /**
     * Internal output energy states, to be used as a temporary storage object
     */
    protected LimitedCommodityStateMap internalInterdependentOutputStates;
    /**
     * Output energy states to be given to the energy-simulation-core.
     */
    private LimitedCommodityStateMap interdependentOutputStates;
    /**
     * Set of all energy types this problem-part produces or consumes.
     */
    protected EnumSet<Commodity> allOutputCommodities;
    /**
     * Set of all energy types this problem-part needs information of as input.
     */
    private EnumSet<Commodity> allInputCommodities = EnumSet.noneOf(Commodity.class);
    /**
     * State of the virtual ancillary meter received from the energy-simulation-core.
     */
    protected AncillaryMeterState ancillaryMeterState;

    /**
     * Current time-state inside the optimization loop.
     */
    private long interdependentTime;
    private int stepSize;
    /**
     * Point-of-time this problem-part is basd on (was created).
     */
    private long referenceTime;

    /**
     * Type of compression to be used for load profiles.
     */
    protected LoadProfileCompressionTypes compressionType;
    /**
     * Associated value to be used for compression.
     */
    protected int compressionValue;

    /**
     * Handler for en-/decoding of solutions and provision of encoding information to optimization algorithms.
     */
    protected final IPPSolutionHandler solutionHandler;
    protected DecodedSolutionWrapper currentSolution;
    /**
     * Additional (non-monetary) costs produced during the optimization loops which would not be computed in the
     * normal cost-calulcation. To be added at the end of the optimization loop.
     */
    private Enum2DoubleMap<EAObjectives> interdependentCervisia = new Enum2DoubleMap<>(EAObjectives.class);

    private final UUID deviceID;

    /**
     * Flag if the publication of this problem-part should cause a rescheduling.
     */
    private final boolean toBeScheduled;
    /**
     * Flag if this problem-part needs the virtual ancillary meter for it's calculation.
     */
    private final boolean needsAncillaryMeterState;
    /**
     * Flag if this problem-part  reacts to any input information inside the optimization loop.
     */
    private final boolean reactsToInputStates;
    /**
     * Flag if this problem part is completely static and will not provide any input/output.
     */
    private final boolean isCompletelyStatic;

    /**
     * Assigned simplified id for use inside the optimization loop.
     */
    private int id;
    /**
     * The type of device represented by this problem-part
     */
    private final DeviceTypes deviceType;
    private transient SparseLoadProfile loadProfile;

    /**
     * Constructs this problem-part with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param needsAncillaryMeterState if this problem-part needs the virtual ancillary meter for it's calculation
     * @param reactsToInputStates if this problem-part  reacts to any input information inside the optimization loop
     * @param isCompletelyStatic if this problem part is completely static and will not provide any input/output
     * @param deviceType type of device represented by this problem-part
     * @param allOutputCommodities set of all energy types this problem-part produces or consumes.
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     * @param binaryTranslator variable translator and information collector for a binary encoding
     * @param realTranslator variable translator and information collector for a real encoding
     */
    public InterdependentProblemPart(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            boolean needsAncillaryMeterState,
            boolean reactsToInputStates,
            boolean isCompletelyStatic,
            DeviceTypes deviceType,
            EnumSet<Commodity> allOutputCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue,
            AbstractVariableTranslator<BitSet> binaryTranslator,
            AbstractVariableTranslator<double[]> realTranslator) {

        super(deviceId, timestamp);

        this.deviceID = deviceId;

        this.toBeScheduled = toBeScheduled;
        this.needsAncillaryMeterState = needsAncillaryMeterState;
        this.reactsToInputStates = reactsToInputStates;
        this.isCompletelyStatic = isCompletelyStatic;
        this.referenceTime = timestamp.toEpochSecond();
        this.deviceType = deviceType;
        this.allOutputCommodities = allOutputCommodities;
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
        this.internalInterdependentOutputStates = new LimitedCommodityStateMap(allOutputCommodities);
        this.solutionHandler = new IPPSolutionHandler(binaryTranslator, realTranslator);

        this.interdependentCervisia.clear();
    }

    /**
     * Limited copy-constructor that constructs a copy of the given problem-part that is as shallow as possible while
     * still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the problem-part to copy
     */
    public InterdependentProblemPart(InterdependentProblemPart<PhenotypeType, PredictionType> other) {
        super(other.getSender(), other.getTimestamp());

        this.deviceID = other.deviceID;
        this.toBeScheduled = other.toBeScheduled;
        this.needsAncillaryMeterState = other.needsAncillaryMeterState;
        this.reactsToInputStates = other.reactsToInputStates;
        this.isCompletelyStatic = other.isCompletelyStatic;
        this.referenceTime = other.referenceTime;
        this.deviceType = other.deviceType;
        this.allOutputCommodities = other.allOutputCommodities;
        this.allInputCommodities = other.allInputCommodities;
        this.compressionType = other.compressionType;
        this.compressionValue = other.compressionValue;
        this.internalInterdependentOutputStates = new LimitedCommodityStateMap(other.internalInterdependentOutputStates);
        this.solutionHandler = other.solutionHandler;

        this.interdependentInputStates = null;
        this.interdependentOutputStates = null;
        this.ancillaryMeterState = null;

        this.interdependentTime = other.interdependentTime;
        this.stepSize = other.stepSize;
        this.referenceTime = other.referenceTime;
        this.compressionType = other.compressionType;
        this.compressionValue = other.compressionValue;

        this.currentSolution = null;
        this.interdependentCervisia.clear();

        this.id = other.id;
        this.loadProfile = other.loadProfile == null ? null : new SparseLoadProfile();
    }

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        this.stepSize = stepSize;
        if (createLoadProfile)
            this.loadProfile = new SparseLoadProfile();
        else
            this.loadProfile = null;

        if (this.referenceTime != interdependentStartingTime) {
            this.recalculateEncoding(interdependentStartingTime, this.getOptimizationHorizon());
        }

        this.interdependentCervisia.clear();

        this.interdependentTime = interdependentStartingTime;
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
     * Returns the final (full, complete) schedule including interdependencies on other devices for the current set
     * solution.
     *
     * @return the final (full, complete) schedule including interdependencies on other devices
     */
    public abstract Schedule getFinalInterdependentSchedule();


    /**
     * Returns the command-exchange wrapper around the final phenotype including interdependencies on other devices
     * with the given additional information to be used in the construction of the command-exchange.
     *
     * @param sender the sender to be set
     * @param receiver the receiver to be set
     * @param timestamp the timestamp to be set
     *
     * @return the command-exchange wrapper around the final phenotype including interdependencies on other devices
     */
    public final EASolutionCommandExchange<PhenotypeType> transformToFinalInterdependentPhenotype(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp) {
        return new EASolutionCommandExchange<>(
                sender,
                receiver,
                timestamp,
                this.transformToFinalInterdependentPhenotype());
    }

    /**
     * Returns the final phenotype including interdependencies on other devices for the current set solution.
     *
     * @return the final phenotype including interdependencies on other devices
     */
    public abstract PhenotypeType transformToFinalInterdependentPhenotype();


    /**
     * Returns the prediction-exchange wrapper around the final prediction including interdependencies on other devices
     * with the given additional information to be used in the construction of the prediction-exchange.
     *
     * @param sender the sender to be set
     * @param receiver the receiver to be set
     * @param timestamp the timestamp to be set
     *
     * @return the prediction-exchange wrapper around the final prediction including interdependencies on other devices
     */
    public final EAPredictionCommandExchange<PredictionType> transformToFinalInterdependentPrediction(
            UUID sender,
            UUID receiver,
            ZonedDateTime timestamp) {
        return new EAPredictionCommandExchange<>(
                sender,
                receiver,
                timestamp,
                this.transformToFinalInterdependentPrediction());
    }

    /**
     * Returns the final prediction including interdependencies on other devices for the current set solution.
     *
     * @return the final prediction including interdependencies on other devices
     */
    public abstract PredictionType transformToFinalInterdependentPrediction();


    /**
     * Updates the point-of-time and maximum optimization horizon this problem-part is based upon and allows it to
     * adapt it's variable demands and encoding to this new time-base.
     *
     * @param currentTime new point of time
     * @param maxHorizon new optimization horizon
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

    /**
     * Sets the output state-map to the given one.
     *
     * @param states the new output state-map
     */
    public void setOutputStates(LimitedCommodityStateMap states) {
        this.interdependentOutputStates = states;
    }

    /**
     * Returns if this problem-part should cause a rescheduling.
     *
     * @return true if this problem-part should cause a rescheduling
     */
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

    /**
     * Returns if this problem-part is completely static, not producing any in- or output.
     *
     * @return true if this problem-part is completely static
     */
    public boolean isCompletelyStatic() {
        return this.isCompletelyStatic;
    }

    /**
     * Returns the set of all energy types this problem-part produces output for.
     *
     * @return the set of all energy types this problem-part produces output for
     */
    public EnumSet<Commodity> getAllOutputCommodities() {
        return this.allOutputCommodities;
    }

    /**
     * Returns the set of all energy types this problem-part need input for.
     *
     * @return the set of all energy types this problem-part need input for
     */
    public EnumSet<Commodity> getAllInputCommodities() {
        return this.allInputCommodities;
    }

    /**
     * Updates the set of all energy types this problem-part need input for.
     *
     * @param allInputCommodities the new set of needed input types
     */
    public void setAllInputCommodities(EnumSet<Commodity> allInputCommodities) {
        this.allInputCommodities = allInputCommodities;
    }

    /**
     * Returns the point-of-time this problem-part is based upon.
     *
     * @return the point-of-time this problem-part is based upon
     */
    public long getReferenceTime() {
        return this.referenceTime;
    }

    /**
     * Sets the point-of-time this problem-part is based upon to the given one.
     *
     * @param referenceTime the new point-of-time
     */
    public void setReferenceTime(long referenceTime) {
        this.referenceTime = referenceTime;
    }

    /**
     * Returns the device type of the underlying device of this problem-part.
     *
     * @return the device type of the underlying device
     */
    public DeviceTypes getDeviceType() {
        return this.deviceType;
    }

    @Override
    public UUID getUUID() {
        return this.deviceID;
    }

    /**
     * Returns the simplified id of this problem-part.
     *
     * @return the simplified id of this problem-part
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the simplified id of this problem-part to the given one.
     *
     * @param id the new simplfied id
     */
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
     * Calculates all the addiotional (non-monetary) costs incurred by this problem-part during the optimization loop.
     */
    public void finalizeInterdependentCervisia() {}

    /**
     * Returns the additional (non-monetary) costs incurred by this problem-part over the optimization loop (e.g.
     * start-stop costs).
     *
     * @return the additional (non-monetary) costs incurred by this problem-part
     */
    public Enum2DoubleMap<EAObjectives> getInterdependentCervisia() {
        return this.interdependentCervisia;
    }

    /**
     * Adds the given value to the additonal costs of this problem-part.
     *
     * @param add the additional value
     */
    public void addInterdependentCervisia(EAObjectives objective, double add) {
        this.interdependentCervisia.add(objective, add);
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
     * Returns the step-size the optimization loop advances time by.
     *
     * @return the step-size the optimization loop advances time by
     */
    public int getStepSize() {
        return this.stepSize;
    }

    /**
     * Returns a string representation of this problem-part.
     *
     * @return a string representation of this problem-part
     */
    public abstract String problemToString();

    @Override
    public String toString() {
        return this.problemToString();
    }

    /**
     * Returns a limited copy of this problem-part that is as shallow as possible while
     * still not conflicting with multithreaded use inside the optimization-loop.
     *
     * @return a limited copy of this problem-part
     */
    public abstract InterdependentProblemPart<PhenotypeType, PredictionType> getClone();
}
