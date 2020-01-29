package osh.mgmt.localcontroller.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.ControllableIPP;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.BitSetConverter;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.EnumSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class FutureApplianceIPP
        extends ControllableIPP<ISolution, IPrediction> {


    private static final long serialVersionUID = 3070293649618474988L;
    public static final Commodity[] DEFAULT_COMMODITIES = {};

    // NOTE: tDoF = latestStartingTime - earliestStartingTime

    /**
     * earliest starting time for the device
     */
    private long earliestStartingTime; // to be update when rescheduled
    /**
     * latest starting time for the device
     */
    private long latestStartingTime;

    private final double cervisiaDofUsedFactor = 0.01;

    private ApplianceProgramConfigurationStatus acp;
    private SparseLoadProfile[][] compressedDLProfiles;
    private SparseLoadProfile[] initializedLoadProfiles;
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


    // ### IPP STUFF ###
    /**
     * used for iteration in interdependent calculation
     */
    private long interdependentTime;

    private SparseLoadProfile lp;

    private double cervisia;

    private Commodity[] usedCommodities;
    private int lastUsedIndex;

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
            IGlobalLogger logger,
            ZonedDateTime timestamp,
            int bitCount,
            boolean toBeScheduled,
            long optimizationHorizon,
            DeviceTypes deviceType,
            long referenceTime,
            long earliestStartingTime,
            long latestStartingTime,
            ApplianceProgramConfigurationStatus acp,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                timestamp,
                bitCount,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                false, //does not react to input states
                optimizationHorizon,
                referenceTime,
                deviceType,
                DEFAULT_COMMODITIES, //we will calculate this and set in our constructor so this is a dummy value
                compressionType,
                compressionValue);

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

        this.allOutputCommodities = new Commodity[tempUsedComm.size()];
        this.allOutputCommodities = tempUsedComm.toArray(this.allOutputCommodities);

        // recalculate partition of solution ("header")
        this.header = calculateHeader(
                this.earliestStartingTime,
                this.latestStartingTime,
                acp);

        int[][][] minMaxTimes = acp.getMinMaxDurations();

//		getGlobalLogger().logDebug("earliestStartingTime=" + earliestStartingTime);
//		getGlobalLogger().logDebug("latestStartingTime" + latestStartingTime);

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
//				getGlobalLogger().logDebug("maxValues["+i+"]["+j+"]" + maxValues[i][j]);
//				getGlobalLogger().logDebug("sumOfAllMaxValues[" + i + "]=" + sumOfAllMaxValues[i]);
            }
        }
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
        header[0] = getProfilesBitCount(
                acp);
        header[1] = getTDOFLengthBitCount(
                earliestStartTime,
                latestStartTime);
        header[2] = getMaxNumberOfPhases(
                acp);
        return header;
    }

    private static int getProfilesBitCount(
            ApplianceProgramConfigurationStatus acp) {

        int length = acp.getDynamicLoadProfiles().length;

        // only one profile available -> no DoF
        if (length < 2) {
            return 0;
        } else {
            return (int) Math.ceil(Math.log(length) / Math.log(2));
        }
    }

    /**
     * Necessary bits for tDoF (bits per pause)
     */
    private static int getTDOFLengthBitCount(
            long earliestStartTime,
            long latestStartTime) {
        if (earliestStartTime > latestStartTime) {
            return 0;
        }
        return (int) Math.ceil(Math.log(latestStartTime - earliestStartTime + 1) / Math.log(2));
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
     * returns the needed amount of bits for the EA
     *
     * @param earliestStartTime
     * @param latestStartTime
     */
    public static int calculateBitCount(
            long earliestStartTime,
            long latestStartTime,
            ApplianceProgramConfigurationStatus acp) {
        /*
         * header (3-dim)<br>
         * [0] = # bits for alternative profiles (gray encoded binary string)<br>
         * [1] = # bits for tDoF (gray encoded binary string)<br>
         * [2] = max # of phases
         */
        int[] header = calculateHeader(
                earliestStartTime,
                latestStartTime,
                acp);

        return header[0] + header[1] * header[2];
    }

    /**
     * Get selected tDoF times for phases<br>
     * [...] = time in ticks
     */
    private static int[] getSelectedTimeFromTDOFFromSolution(
            BitSet solution,
            int[] header,
            long availableTDoF,
            int[] maxValues,
            int sumOfAllMaxValues) {

        /*
         * header (3-dim)<br>
         * [0] = # bits for alternative profiles (gray encoded binary string)<br>
         * [1] = # bits for tDoF (gray encoded binary string)<br>
         * [2] = max # of phases
         */

        int noProfileBits = header[0];
        int noTDOFBits = header[1];
        int maxNoOfPhases = header[2];

        // is there a tDoF?
        if (maxNoOfPhases > 0) {
            // calculate sum of total bit value of all tDoF partitions bit values
            int sumOfAllBitValues = 0;
            int currentBit = noProfileBits;
            for (int i = 0; i < maxNoOfPhases; i++) {
                BitSet subset = solution.get(currentBit, currentBit + noTDOFBits);
                if (maxValues[i] > 0) {
                    sumOfAllBitValues += BitSetConverter.gray2long(subset);
                }
                currentBit += noTDOFBits;
            }

            // calculate partition
            int[] returnValue = new int[maxNoOfPhases];

            currentBit = noProfileBits;
            for (int i = 0; i < maxNoOfPhases; i++) {
                BitSet subset = solution.get(currentBit, currentBit + noTDOFBits);
                // IMPORTANT: remember the following!
                // IMPORTANT: this could lead to rounding errors -> program may end sooner than expected
                // IMPORTANT: keep cast to double (totalBitValue)
//				int value = Math.min(
//						(int) (((double) gray2long(subset) * availableTDoF) / (double) sumOfAllBitValues),
//						maxValues[i]);

                int firstVal = (int) BitSetConverter.gray2long(subset);
                //TODO minValues[]
                int value = (int) Math.min(Math.round(((double) (firstVal * availableTDoF)) / sumOfAllBitValues), maxValues[i]);
                returnValue[i] = value;
                // move to next bunch of bits...
                currentBit += noTDOFBits;
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
            BitSet solution,
            int[] header,
            ILoadProfile<Commodity>[][] dlp) {
        // there is only one profile that can be selected
        if (header[0] == 0) {
            return 0;
        }
        int profilesBits = header[0];
        BitSet subset = solution.get(0, profilesBits);
        return (int) Math.floor(BitSetConverter.gray2long(subset) / Math.pow(2, profilesBits) * dlp.length);
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
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        this.stepSize = stepSize;

        // INFO: maxReferenceTime = starting point of interdependent calculation

        // TYPICAL OLD STUFF
        this.cervisia = 0;
        this.interdependentTime = maxReferenceTime;
        this.setOutputStates(null);

        // build final load profile
//		SparseLoadProfile returnProfile = new SparseLoadProfile();

        // get eDoF values
        int selectedProfile =
                getSelectedProfileFromSolution(
                        solution,
                        this.header,
                        this.acp.getDynamicLoadProfiles());

        // tDoF has been distributed to available phases
        long availableTDoF = this.latestStartingTime - this.earliestStartingTime;
        int[] selectedTDOF =
                getSelectedTimeFromTDOFFromSolution(
                        solution,
                        this.header,
                        availableTDoF,
                        this.maxValues[selectedProfile],
                        this.sumOfAllMaxValues[selectedProfile]);

        if (selectedTDOF.length > 0 && availableTDoF > 0) {
            if (selectedTDOF[0] > 0) {
                //starting later is better
                this.cervisia -= ((double) selectedTDOF[0] / availableTDoF) * this.cervisiaDofUsedFactor;
            }
        }

//		getGlobalLogger().logDebug("availableTDoF=" +  availableTDoF);
//		getGlobalLogger().logDebug(maxValues[selectedProfile][0] + " . " + maxValues[selectedProfile][1] + " . " + maxValues[selectedProfile][2]);
//		getGlobalLogger().logDebug(selectedTDOF[0] + " . " + selectedTDOF[1] + " . " + selectedTDOF[2]);

        // get stuff of the selected profile
//		SparseLoadProfile[] selectedDlp = acp.getDynamicLoadProfiles()[selectedProfile];
        SparseLoadProfile[] selectedDlp = this.compressedDLProfiles[selectedProfile];
        this.initializedLoadProfiles = new SparseLoadProfile[selectedDlp.length];


        int[][] selectedMinMaxTimes = this.acp.getMinMaxDurations()[selectedProfile];

        // convert to selected starting times
        long[] selectedStartingTimes = getStartingTimes(
                this.acp.getAcpReferenceTime(),
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
//					returnProfile = (SparseLoadProfile) returnProfile.merge(
//							selectedDlp[i],
//							selectedStartingTimes[i]);
                    this.initializedLoadProfiles[i] = selectedDlp[i];
                } else {
                    // profile has to be stripped-down or enlarged...
                    if (availableLength > selectedMinMaxTimes[i][0] + selectedTDOF[i]) {
                        // strip-down...(shorten)...
                        SparseLoadProfile tempLP = selectedDlp[i].clone();
                        tempLP.setEndingTimeOfProfile(selectedMinMaxTimes[i][0] - selectedTDOF[i]);
                        this.initializedLoadProfiles[i] = tempLP;
//						returnProfile = returnProfile.merge(
//								tempLP,
//								selectedStartingTimes[i]);
                    } else {
                        // enlarge...
                        int number = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) / availableLength);

                        if (selectedDlp[i].getEndingTimeOfProfile() == 1) {
                            SparseLoadProfile longerProfile = selectedDlp[i].clone();
                            longerProfile.setEndingTimeOfProfile(number);
                            this.initializedLoadProfiles[i] = longerProfile;
//							returnProfile = returnProfile.merge(
//									longerProfile,
//									selectedStartingTimes[i]);
                        } else {
                            SparseLoadProfile lengthened = selectedDlp[i].clone();
                            for (int j = 1; j < number; j++) {
                                lengthened = lengthened.merge(
                                        selectedDlp[i],
                                        selectedStartingTimes[i] + j * availableLength);
                            }
                            // n-times the full profile
//							for (int j = 0; j < number; j++) {
//								returnProfile = returnProfile.merge(
//										selectedDlp[i],
//										selectedStartingTimes[i] + j * availableLength);
//							}
                            int remainingPartial = (int) ((selectedMinMaxTimes[i][0] + selectedTDOF[i]) % availableLength);
                            // plus partial stripped-down profile...
                            SparseLoadProfile tempLP = selectedDlp[i].clone();
                            tempLP.setEndingTimeOfProfile(remainingPartial);
                            lengthened = lengthened.merge(
                                    tempLP,
                                    selectedStartingTimes[i] + number * availableLength);
                            this.initializedLoadProfiles[i] = lengthened;
//							returnProfile = returnProfile.merge(
//									tempLP,
//									selectedStartingTimes[i]  + number * availableLength);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//		this.lp = returnProfile;
        this.lp = new SparseLoadProfile();

        EnumSet<Commodity> tempUsedComm = EnumSet.noneOf(Commodity.class);

        for (int i = 0; i < this.initializedLoadProfiles.length; i++) {
            for (Commodity c : this.allOutputCommodities) {
                if (!tempUsedComm.contains(c) &&
                        this.initializedLoadProfiles[i].getFloorEntry(c, maxReferenceTime) != null) {
                    tempUsedComm.add(c);
                }
            }

            long relativeStart = Math.abs(Math.min(this.initializedStartingTimes[i] - maxReferenceTime, 0));
            this.initializedLoadProfiles[i].initSequentialAverageLoad(relativeStart);
        }

        this.usedCommodities = new Commodity[tempUsedComm.size()];
        this.usedCommodities = tempUsedComm.toArray(this.usedCommodities);

        this.internalInterdependentOutputStates = new LimitedCommodityStateMap(this.usedCommodities);

        this.lastUsedIndex = 0;

//		lp.initSequentialAverageLoad(maxReferenceTime);

    }


    // ### CALCULATE BIT COUNT ###

    /*
     * the method for sequential averages in our load profiles uses entrys and iterators which
     * cannot be serialised, so they have to be destroyed prior to deep copying
     */
    @Override
    public void prepareForDeepCopy() {
        if (this.lp != null) {
            this.lp.removeSequentialPriming();
        }
        if (this.initializedLoadProfiles != null) {
            for (SparseLoadProfile initializedLoadProfile : this.initializedLoadProfiles) {
                initializedLoadProfile.removeSequentialPriming();
            }
        }
    }


    // ### INTERPRET SOLUTION ###

    @Override
    public void calculateNextStep() {
        // no next step...
        // ...but give power

        long end = this.interdependentTime + this.stepSize;

        int index = this.lastUsedIndex;
        long currentTime = this.interdependentTime;

        double[] powers = new double[this.usedCommodities.length];

        boolean hasValues = false;

        //all power values in one profile
        if (end < this.initializedStartingTimes[index]) {
            long subtractionFactor = this.initializedStartingTimes[index - 1];
            for (int j = 0; j < this.usedCommodities.length; j++) {
                powers[j] = Math.round(this.initializedLoadProfiles[index - 1]
                        .getAverageLoadFromTillSequentialNotRounded(this.usedCommodities[j], (currentTime - subtractionFactor), (end - subtractionFactor)));

                if (powers[j] != 0) {
                    this.internalInterdependentOutputStates.setPower(this.usedCommodities[j], powers[j]);
                    hasValues = true;
                } else {
                    this.internalInterdependentOutputStates.resetCommodity(this.usedCommodities[j]);
                }
            }
        }
        //power values in multiple profiles, iterate
        else {
            while (currentTime < end) {

                if (currentTime > this.initializedStartingTimes[index]) {
                    index++;
                    this.lastUsedIndex++;
                } else {

                    long currentEnd = Math.min(this.initializedStartingTimes[index], end);
                    double factor = (currentEnd - currentTime);
                    long subtractionFactor = this.initializedStartingTimes[index - 1];

                    for (int j = 0; j < this.usedCommodities.length; j++) {
                        powers[j] += (this.initializedLoadProfiles[index - 1]
                                .getAverageLoadFromTillSequentialNotRounded(this.usedCommodities[j], (currentTime - subtractionFactor), (currentEnd - subtractionFactor)) * factor);
                    }

                    currentTime = this.initializedStartingTimes[index];
                    index++;
                }
            }

            for (int j = 0; j < this.usedCommodities.length; j++) {
                powers[j] = Math.round(powers[j] / this.stepSize);
                if (powers[j] != 0) {
                    this.internalInterdependentOutputStates.setPower(this.usedCommodities[j], powers[j]);
                    hasValues = true;
                } else {
                    this.internalInterdependentOutputStates.resetCommodity(this.usedCommodities[j]);
                }
            }
        }

        if (hasValues) {
            this.setOutputStates(this.internalInterdependentOutputStates);
        } else {
            this.setOutputStates(null);
        }

        this.interdependentTime += this.stepSize;
    }


    // HELPER METHODS

    @Override
    public Schedule getFinalInterdependentSchedule() {
        // IMPORTANT: cervisia currently not in use and unchecked

        //we dont merge the phases anymore (it's faster that way) so for a schedule we have to do it here now
        if (this.initializedLoadProfiles != null) {
            this.lp = new SparseLoadProfile();
            for (int i = 0; i < this.initializedLoadProfiles.length; i++) {
                this.lp.merge(this.initializedLoadProfiles[i], this.initializedStartingTimes[i]);
            }
        }

        return new Schedule(this.lp, this.cervisia, this.getDeviceType().toString());
    }

    //TODo: find better solution (for mapping from 2^x to real length)

    @Override
    public ISolution transformToPhenotype(BitSet solution) {
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
    public ISolution transformToFinalInterdependentPhenotype(
            BitSet solution) {
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
                        this.acp.getAcpReferenceTime(),
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

        this.setReferenceTime(currentTime);

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

            this.setBitCount(
                    calculateBitCount(
                            this.earliestStartingTime,
                            this.latestStartingTime,
                            this.acp));
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
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] [" + this.getOptimizationHorizon() + "] FutureApplianceIPP : EST=" + this.earliestStartingTime + " LST=" + this.latestStartingTime;// + " minMaxValues=" + StringToArray.arrayToString(array);
    }

    @Override
    public String solutionToString(BitSet bits) {
        return "FutureApplianceIPP solution";
    }

}
