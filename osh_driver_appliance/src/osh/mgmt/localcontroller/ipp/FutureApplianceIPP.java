package osh.mgmt.localcontroller.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SequentialSparseLoadProfileIterator;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.BinaryFullRangeVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.translators.RealVariableTranslator;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.DecodedSolutionWrapper;
import osh.datatypes.registry.oc.ipp.solutionEncoding.variables.VariableType;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class FutureApplianceIPP
        extends ControllableIPP<ISolution, IPrediction> {


    private static final long serialVersionUID = 3070293649618474988L;

    // NOTE: tDoF = latestStartingTime - earliestStartingTime

    /**
     * earliest starting time for the device
     */
    private long earliestStartingTime; // to be update when rescheduled
    /**
     * latest starting time for the device
     */
    private long latestStartingTime;

    private static final double cervisiaDofUsedFactor = 0.01;

    private ApplianceProgramConfigurationStatus acp;
    private SparseLoadProfile[][] compressedDLProfiles;
    private SparseLoadProfile[] initializedLoadProfiles;
    private SequentialSparseLoadProfileIterator[] sequentialIterators;
    private long[] initializedStartingTimes;

    /**
     * [0] = # bits for alternative profiles (gray encoded binary string)<br>
     * [1] = # bits for tDoF (gray encoded binary string)<br>
     * [2] = max # of phases
     */
    private int[] header;

    /**
     * max value of every phase (depending on tDoF and minMaxValues)<br>
     * dim0 : profile<br>
     * dim1 : phase<br>
     */
    private int[][] maxValues; // to be updated when rescheduled

    /**
     * sum of dim0 of maxValues
     */
    private int[] sumOfAllMaxValues; // to be updated when rescheduled

    private Commodity[] usedCommodities;
    private int lastUsedIndex;
    private double[] powers;

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected FutureApplianceIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
     */
    public FutureApplianceIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            long optimizationHorizon,
            DeviceTypes deviceType,
            long earliestStartingTime,
            long latestStartingTime,
            ApplianceProgramConfigurationStatus acp,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                false, //does not react to input states
                optimizationHorizon,
                deviceType,
                EnumSet.noneOf(Commodity.class), //we will calculate this and set in our constructor so this is a dummy value
                EnumSet.noneOf(Commodity.class),
                compressionType,
                compressionValue,
                new BinaryFullRangeVariableTranslator(),
                new RealVariableTranslator());

        this.earliestStartingTime = earliestStartingTime;
        this.latestStartingTime = latestStartingTime;

        if (acp != null) {
            this.acp = (ApplianceProgramConfigurationStatus) acp.clone();
        }

        this.compressedDLProfiles = SparseLoadProfile.getCompressedProfile(compressionType, this.acp.getDynamicLoadProfiles(), compressionValue, compressionValue);

        /*
         * here we check which commodities are existant in the load profiles, so we can have better priming of our ESC
         */
        EnumSet<Commodity> tempUsedComm = EnumSet.noneOf(Commodity.class);
        for (SparseLoadProfile[] compressedDLProfile : this.compressedDLProfiles) {
            for (SparseLoadProfile sparseLoadProfile : compressedDLProfile) {
                if (!tempUsedComm.contains(Commodity.ACTIVEPOWER) &&
                        sparseLoadProfile.getFloorEntry(Commodity.ACTIVEPOWER, this.getReferenceTime()) != null) {
                    tempUsedComm.add(Commodity.ACTIVEPOWER);
                }
                if (!tempUsedComm.contains(Commodity.REACTIVEPOWER) &&
                        sparseLoadProfile.getFloorEntry(Commodity.REACTIVEPOWER, this.getReferenceTime()) != null) {
                    tempUsedComm.add(Commodity.REACTIVEPOWER);
                }
                if (!tempUsedComm.contains(Commodity.HEATINGHOTWATERPOWER) &&
                        sparseLoadProfile.getFloorEntry(Commodity.HEATINGHOTWATERPOWER, this.getReferenceTime()) != null) {
                    tempUsedComm.add(Commodity.HEATINGHOTWATERPOWER);
                }
                if (!tempUsedComm.contains(Commodity.NATURALGASPOWER) &&
                        sparseLoadProfile.getFloorEntry(Commodity.NATURALGASPOWER, this.getReferenceTime()) != null) {
                    tempUsedComm.add(Commodity.NATURALGASPOWER);
                }
            }
        }

        this.allOutputCommodities = tempUsedComm.clone();
        this.powers = new double[tempUsedComm.size()];
        this.usedCommodities = new Commodity[tempUsedComm.size()];
        this.usedCommodities = tempUsedComm.toArray(this.usedCommodities);
        this.internalInterdependentOutputStates = new LimitedCommodityStateMap(this.allOutputCommodities);

        // recalculate partition of solution ("header")
        this.header = calculateHeader(
                this.earliestStartingTime,
                this.latestStartingTime,
                acp);

        int[][][] minMaxTimes = acp.getMinMaxDurations();


        // recalculate max values (max lengths of phases)
        this.maxValues = calculateMaxValuesArray(
                this.earliestStartingTime,
                this.latestStartingTime,
                minMaxTimes);

        // calculate sum of all max values (for determination of how to distribute tdof)
        this.sumOfAllMaxValues = new int[this.maxValues.length];
        for (int i = 0; i < this.maxValues.length; i++) {
            this.sumOfAllMaxValues[i] = 0;
            for (int j = 0; j < this.maxValues[i].length; j++) {
                this.sumOfAllMaxValues[i] += this.maxValues[i][j];
            }
        }

        this.updateSolutionInformation(this.getReferenceTime(), this.getOptimizationHorizon());
    }

    public FutureApplianceIPP(FutureApplianceIPP other) {
        super(other);

        this.earliestStartingTime = other.earliestStartingTime;
        this.latestStartingTime = other.latestStartingTime;

        this.acp = other.acp;
        this.compressedDLProfiles = other.compressedDLProfiles;
        this.initializedLoadProfiles = null;
        this.sequentialIterators = null;
        this.initializedStartingTimes = null;
        this.currentSolution = other.currentSolution;

        this.header = other.header;
        this.maxValues = other.maxValues;
        this.sumOfAllMaxValues = other.sumOfAllMaxValues;
        this.usedCommodities = Arrays.copyOf(other.usedCommodities, other.usedCommodities.length);
        this.powers = Arrays.copyOf(other.powers, other.powers.length);

        this.lastUsedIndex = other.lastUsedIndex;
    }

    /**
     * [0] = # of profiles<br>
     * [1] = tDoF in Bits (gray encoded binary string)<br>
     * [2] = max # of phases
     */
    private static int[] calculateHeader(
            long earliestStartTime,
            long latestStartTime,
            ApplianceProgramConfigurationStatus acp) {
        int[] header = new int[3];
        header[0] = acp.getDynamicLoadProfiles().length;
        header[1] = (int) (latestStartTime - earliestStartTime);
        header[2] = getMaxNumberOfPhases(acp);
        return header;
    }

    /**
     * Number of phases (maximum of all profiles)
     */
    private static int getMaxNumberOfPhases(
            ApplianceProgramConfigurationStatus acp) {
        int max = 0;
        for (int i = 0; i < acp.getDynamicLoadProfiles().length; i++) {
            max = Math.max(max, acp.getDynamicLoadProfiles()[i].length);
        }
        return max;
    }

    /**
     * Get selected tDoF times for phases<br>
     * [...] = time in ticks
     */
    private static int[] getSelectedTimeFromTDOFFromSolution(
            DecodedSolutionWrapper solution,
            int[] header,
            long availableTDoF,
            int[] maxValues,
            int sumOfAllMaxValues) {

        /*
         * header (3-dim)<br>
         * [0] = # alternative profiles count <br>
         * [1] = # tDoF <br>
         * [2] = max # of phases
         */

        int maxNoOfPhases = header[2];

        long[] solutionArray = solution.getLongArray();
        int startIndex = header[0] == 1 ? 0 : 1;

        // is there a tDoF?
        if (maxNoOfPhases > 0) {
            // calculate sum of all values of all tDoF partitions
            int sumOfAllValues = 0;
            for (int i = 0; i < maxNoOfPhases; i++) {
                if (maxValues[i] > 0) {
                    sumOfAllValues += solutionArray[i + startIndex];
                }
            }

            // calculate partition
            int[] returnValue = new int[maxNoOfPhases];

            for (int i = 0; i < maxNoOfPhases; i++) {
                int value = (int) Math.min(Math.round(((double) (solutionArray[i + startIndex] * availableTDoF)) / sumOfAllValues), maxValues[i]);
                returnValue[i] = value;
            }

            return returnValue;
        } else {
            return null;
        }
    }


    // ### HEADER CALCULATION ###

    /**
     * [0] = starting time of phase 0
     * conversion: starting time of phase + phase time + selected tDoF -> starting time of next phase
     */
    private static long[] getStartingTimes(
            long referenceTime,
            int[] selectedTimeOfTDOF,
            int[][] selectedMinMaxTimes) {
        // allocate array
        long[] selectedStartTimes = new long[selectedTimeOfTDOF.length];
        // calculate starting times for phases
        long endTimeOfPreviousPhase = referenceTime;
        for (int i = 0; i < selectedStartTimes.length; i++) {
            // calculate start time for this phase
            selectedStartTimes[i] = endTimeOfPreviousPhase;

            // calculate end time for this phase (for next iteration) with minimum length of phase plus selected tDoF
            endTimeOfPreviousPhase += selectedMinMaxTimes[i][0] + selectedTimeOfTDOF[i];
        }
        return selectedStartTimes;
    }

    /**
     * returns [NUMBER]
     */
    private static int getSelectedProfileFromSolution(
            DecodedSolutionWrapper solution,
            int[] header,
            ILoadProfile<Commodity>[][] dlp) {
        // there is only one profile that can be selected
        if (header[0] == 1) {
            return 0;
        } else {
            return (int) solution.getLongArray()[0];
        }
    }

    private static int[][] calculateMaxValuesArray(
            long earliestStartingTime,
            long latestStartingTime,
            int[][][] minMaxTimes) {
        // get available tDOF
        int tDOF = (int) (latestStartingTime - earliestStartingTime);
        int[][] maxValues = new int[minMaxTimes.length][];
        for (int d0 = 0; d0 < minMaxTimes.length; d0++) {
            maxValues[d0] = new int[minMaxTimes[d0].length];
            for (int d1 = 0; d1 < minMaxTimes[d0].length; d1++) {
                // max time from a to b = min(tDOF and max - min length)
                maxValues[d0][d1] = Math.min(
                        minMaxTimes[d0][d1][1] - minMaxTimes[d0][d1][0],
                        tDOF);
            }
        }
        return maxValues;
    }

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        // get eDoF values
        int selectedProfile =
                getSelectedProfileFromSolution(
                        this.currentSolution,
                        this.header,
                        this.acp.getDynamicLoadProfiles());

        // tDoF has been distributed to available phases
        long availableTDoF = this.latestStartingTime - this.earliestStartingTime;
        int[] selectedTDOF =
                getSelectedTimeFromTDOFFromSolution(
                        this.currentSolution,
                        this.header,
                        availableTDoF,
                        this.maxValues[selectedProfile],
                        this.sumOfAllMaxValues[selectedProfile]);

        if (selectedTDOF.length > 0 && availableTDoF > 0) {
            if (selectedTDOF[0] > 0) {
                //starting later is better
                this.addInterdependentCervisia(-((double) selectedTDOF[0] / availableTDoF) * cervisiaDofUsedFactor);
            }
        }

        // get stuff of the selected profile
        SparseLoadProfile[] selectedDlp = this.compressedDLProfiles[selectedProfile];
        this.initializedLoadProfiles = new SparseLoadProfile[selectedDlp.length];


        int[][] selectedMinMaxTimes = this.acp.getMinMaxDurations()[selectedProfile];

        // convert to selected starting times
        long[] selectedStartingTimes = getStartingTimes(
                this.acp.getAcpReferenceTime().toEpochSecond(),
                selectedTDOF,
                selectedMinMaxTimes);

        this.initializedStartingTimes = new long[selectedStartingTimes.length + 1];
        System.arraycopy(selectedStartingTimes, 0, this.initializedStartingTimes, 0, selectedStartingTimes.length);
        this.initializedStartingTimes[selectedStartingTimes.length] = Long.MAX_VALUE;

        // merge phases to profile
        for (int i = 0; i < selectedMinMaxTimes.length; i++) {
            try {
                long availableLength = selectedDlp[i].getEndingTimeOfProfile(); // is has relative times
                if (availableLength == selectedMinMaxTimes[i][0] + selectedTDOF[i]) {
                    // shortcut
                    this.initializedLoadProfiles[i] = selectedDlp[i];
                } else {
                    // profile has to be stripped-down or enlarged...
                    if (availableLength > selectedMinMaxTimes[i][0] + selectedTDOF[i]) {
                        // strip-down...(shorten)...
                        SparseLoadProfile tempLP = selectedDlp[i].clone();
                        tempLP.setEndingTimeOfProfile(selectedMinMaxTimes[i][0] - selectedTDOF[i]);
                        this.initializedLoadProfiles[i] = tempLP;
                    } else {
                        // enlarge...
                        int number = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) / availableLength);

                        if (selectedDlp[i].getEndingTimeOfProfile() == 1) {
                            SparseLoadProfile longerProfile = selectedDlp[i].clone();
                            longerProfile.setEndingTimeOfProfile(number);
                            this.initializedLoadProfiles[i] = longerProfile;
                        } else {
                            SparseLoadProfile lengthened = selectedDlp[i].clone();
                            for (int j = 1; j < number; j++) {
                                lengthened = lengthened.merge(
                                        selectedDlp[i],
                                        selectedStartingTimes[i] + j * availableLength);
                            }
                            // n-times the full profile
                            int remainingPartial = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) % availableLength);
                            // plus partial stripped-down profile...
                            SparseLoadProfile tempLP = selectedDlp[i].clone();
                            tempLP.setEndingTimeOfProfile(remainingPartial);
                            lengthened = lengthened.merge(
                                    tempLP,
                                    selectedStartingTimes[i] + number * availableLength);
                            this.initializedLoadProfiles[i] = lengthened;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        EnumSet<Commodity> tempUsedComm = EnumSet.noneOf(Commodity.class);
        boolean sameCommodities = true;
        this.sequentialIterators = new SequentialSparseLoadProfileIterator[this.initializedLoadProfiles.length];

        for (int i = 0; i < this.initializedLoadProfiles.length; i++) {
            for (Commodity c : this.allOutputCommodities) {
                if (!tempUsedComm.contains(c) &&
                        this.initializedLoadProfiles[i].getFloorEntry(c, maxReferenceTime) != null) {
                    tempUsedComm.add(c);
                    sameCommodities &= Arrays.asList(this.usedCommodities).contains(c);
                }
            }

            long relativeStart = Math.abs(Math.min(this.initializedStartingTimes[i] - maxReferenceTime, 0));
            this.sequentialIterators[i] = this.initializedLoadProfiles[i].initSequentialAverageLoad(relativeStart);
        }

        if (!sameCommodities) {
            this.powers = new double[tempUsedComm.size()];
            this.usedCommodities = new Commodity[tempUsedComm.size()];
            this.usedCommodities = tempUsedComm.toArray(this.usedCommodities);
            this.internalInterdependentOutputStates = new LimitedCommodityStateMap(tempUsedComm);
        } else {
            this.internalInterdependentOutputStates.clear();
        }

        this.lastUsedIndex = 0;
    }

    private void updateSolutionInformation(long referenceTime, long maxHorizon) {

        int variableCount = this.header[2];
        double[][] boundaries;

        if (this.header[0] > 1) {
            variableCount++;
        }

        boundaries = new double[variableCount][];
        int startIndex = 0;

        if (this.header[0] > 1) {
            boundaries[0] = new double[]{0, this.header[0] - 1};
            startIndex++;
        }

        for (int i = 0; i < this.header[2]; i++) {
            boundaries[i + startIndex] = new double[]{0, this.header[1]};
        }

        this.solutionHandler.updateVariableInformation(VariableType.LONG, variableCount, boundaries);

        //fix for not controllable devices (inductioncooktop, oven) or devices with no tDof
        this.currentSolution = new DecodedSolutionWrapper(new long[variableCount]);
    }

    @Override
    protected void interpretNewSolution() {
        //do nothing, solution will be interpreted in initializeInterdependentCalculation
    }

    /*
     * the method for sequential averages in our load profiles uses entrys and iterators which
     * cannot be serialised, so they have to be destroyed prior to deep copying
     */
    @Override
    public void prepareForDeepCopy() {
    }


    // ### INTERPRET SOLUTION ###

    @Override
    public void calculateNextStep() {
        // no next step...
        // ...but give power

        long end = this.getInterdependentTime() + this.getStepSize();

        int index = this.lastUsedIndex;
        long currentTime = this.getInterdependentTime();

//        double[] powers = new double[this.usedCommodities.size()];

        boolean hasValues = false;

        //all power values in one profile
        if (end < this.initializedStartingTimes[index]) {
            long subtractionFactor = this.initializedStartingTimes[index - 1];
            int j = 0;
            for (Commodity c : this.usedCommodities) {
                this.powers[j] = Math.round(this.sequentialIterators[index - 1]
                        .getAverageLoadFromTillSequentialNotRounded(c, (currentTime - subtractionFactor), (end - subtractionFactor)));

                if (this.powers[j] != 0) {
                    this.internalInterdependentOutputStates.setPower(c, this.powers[j]);
                    hasValues = true;
                } else {
                    this.internalInterdependentOutputStates.resetCommodity(c);
                }
                j++;
            }
        }
        //power values in multiple profiles, iterate
        //check if device not already done (delay in scheduling, ipp did not arrive, ...)
        else if (this.initializedStartingTimes.length > 1){
            while (currentTime < end) {

                if (currentTime > this.initializedStartingTimes[index]) {
                    index++;
                    this.lastUsedIndex++;
                } else {

                    long currentEnd = Math.min(this.initializedStartingTimes[index], end);
                    double factor = (currentEnd - currentTime);
                    long subtractionFactor = this.initializedStartingTimes[index - 1];

                    int j = 0;
                    for (Commodity c : this.usedCommodities) {
                        powers[j] += (this.sequentialIterators[index - 1]
                                .getAverageLoadFromTillSequentialNotRounded(c, (currentTime - subtractionFactor),
                                        (currentEnd - subtractionFactor)) * factor);
                        j++;
                    }

                    currentTime = this.initializedStartingTimes[index];
                    index++;
                }
            }

            int j = 0;
            for (Commodity c : this.usedCommodities) {
                powers[j] = Math.round(powers[j] / this.getStepSize());
                if (powers[j] != 0) {
                    this.internalInterdependentOutputStates.setPower(c, powers[j]);
                    hasValues = true;
                } else {
                    this.internalInterdependentOutputStates.resetCommodity(c);
                }
                j++;
            }
        }

        if (hasValues) {
            this.setOutputStates(this.internalInterdependentOutputStates);
        } else {
            this.setOutputStates(null);
        }

        this.incrementInterdependentTime();
    }


    // HELPER METHODS

    @Override
    public Schedule getFinalInterdependentSchedule() {
        // IMPORTANT: cervisia currently not in use and unchecked

        //we dont merge the phases anymore (it's faster that way) so for a schedule we have to do it here now
        if (this.initializedLoadProfiles != null && this.getLoadProfile() != null) {
            SparseLoadProfile lp = new SparseLoadProfile();
            for (int i = 0; i < this.initializedLoadProfiles.length; i++) {
                lp = lp.merge(this.initializedLoadProfiles[i], this.initializedStartingTimes[i]);
            }
            this.setLoadProfile(lp);
        }

        return new Schedule(this.getLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
    }

    //TODo: find better solution (for mapping from 2^x to real length)

    @Override
    public ISolution transformToPhenotype(DecodedSolutionWrapper solution) {
        int selectedProfile = getSelectedProfileFromSolution(
                solution,
                this.header,
                this.acp.getDynamicLoadProfiles());
        long availableTDoF = this.latestStartingTime - this.earliestStartingTime;
        int[] selectedTimeOfTDOF = getSelectedTimeFromTDOFFromSolution(
                solution,
                this.header,
                availableTDoF,
                this.maxValues[selectedProfile],
                this.sumOfAllMaxValues[selectedProfile]);
        return new GenericApplianceSolution(
                this.acp.getAcpID(),
                getStartingTimes(
                        this.earliestStartingTime,
                        selectedTimeOfTDOF,
                        this.acp.getMinMaxDurations()[selectedProfile]),
                selectedProfile);
    }

    @Override
    public ISolution transformToFinalInterdependentPhenotype() {
        int selectedProfile = getSelectedProfileFromSolution(
                this.currentSolution,
                this.header,
                this.acp.getDynamicLoadProfiles());
        long availableTDoF = this.latestStartingTime - this.earliestStartingTime;
        int[] selectedTimeOfTDOF = getSelectedTimeFromTDOFFromSolution(
                this.currentSolution,
                this.header,
                availableTDoF,
                this.maxValues[selectedProfile],
                this.sumOfAllMaxValues[selectedProfile]);

        return new GenericApplianceSolution(
                this.acp.getAcpID(),
                getStartingTimes(
                        this.acp.getAcpReferenceTime().toEpochSecond(),
                        selectedTimeOfTDOF,
                        this.acp.getMinMaxDurations()[selectedProfile]),
                selectedProfile);
    }

    // MISC

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        // RECALCULATE
        // (from recalculate encoding)
        // earliest starting time has been reached...

        if (currentTime != this.getReferenceTime() || maxHorizon != this.getOptimizationHorizon()) {
            this.setReferenceTime(currentTime);
            this.setOptimizationHorizon(maxHorizon);

            if (this.earliestStartingTime < currentTime) {

                // shorten tDoF

                // if time is running out...
                if (currentTime > this.latestStartingTime) {
                    // tDoF = 0;
                    this.earliestStartingTime = this.latestStartingTime;
                } else {
                    // adjust minMaxValues in ACP
                    int[][][] minMaxTimes = this.acp.getMinMaxDurations();
                    for (int i = 0; i < minMaxTimes.length; i++) {
                        minMaxTimes[i][0][0] += (currentTime - this.earliestStartingTime);
                    }

                    // adjust time frame from optimization
                    this.earliestStartingTime = currentTime;
                }

                this.header = calculateHeader(
                        this.earliestStartingTime,
                        this.latestStartingTime,
                        this.acp);
                this.maxValues = calculateMaxValuesArray(
                        this.earliestStartingTime,
                        this.latestStartingTime,
                        this.acp.getMinMaxDurations());
                // calculate sum of all max values (for determination of how to distribute tdof)
                this.sumOfAllMaxValues = new int[this.maxValues.length];
                for (int i = 0; i < this.maxValues.length; i++) {
                    this.sumOfAllMaxValues[i] = 0;
                    for (int j = 0; j < this.maxValues[i].length; j++) {
                        this.sumOfAllMaxValues[i] += this.maxValues[i][j];
                    }
                }
            }

            this.updateSolutionInformation(currentTime, this.getOptimizationHorizon());
        }
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] [" + this.getOptimizationHorizon() + "] FutureApplianceIPP : EST=" + this.earliestStartingTime + " LST=" + this.latestStartingTime;// + " minMaxValues=" + StringToArray.arrayToString(array);
    }

    @Override
    public FutureApplianceIPP getClone() {
        return new FutureApplianceIPP(this);
    }

    @Override
    public String solutionToString() {
        return "FutureApplianceIPP solution";
    }

}
