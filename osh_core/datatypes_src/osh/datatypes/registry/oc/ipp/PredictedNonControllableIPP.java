package osh.datatypes.registry.oc.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.esc.LimitedCommodityStateMap;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;

/**
 * Simplified IPP for devices whose loads are completely predicted for the entire optimization loop.
 *
 * @author Sebastian Kramer
 */
public abstract class PredictedNonControllableIPP extends PreCalculatedNonControllableIPP {

    protected final SparseLoadProfile predictedProfile;
    private final EnumSet<Commodity> usedCommodities;

    /**
     * Constructs this simplified problem-part with the given information.
     *
     * @param deviceId the identifier of the devide that is represented by this problem-part
     * @param timestamp the starting-time this problem-part represents at the moment
     * @param toBeScheduled flag if this problem-part should cause a scheduling
     * @param deviceType the type of device that is represented by this problem-part
     * @param predictedProfile the predicted power profile of this problem-part
     * @param usedCommodities all possible commodities that can be emitted by this problem-part
     * @param compressionType the type of compression to use for this problem-part
     * @param compressionValue the associated compression value to be used for compression
     */
    public PredictedNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            SparseLoadProfile predictedProfile,
            EnumSet<Commodity> usedCommodities,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(deviceId,
                timestamp,
                toBeScheduled,
                deviceType,
                usedCommodities,
                compressionType,
                compressionValue);

        this.usedCommodities = usedCommodities;
        this.predictedProfile = predictedProfile.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
    }

    /**
     * Limited copy-constructor that constructs a copy of the given simplified problem-part that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the simplified problem-part to copy
     */
    public PredictedNonControllableIPP(PredictedNonControllableIPP other) {
        super (other);
        this.usedCommodities = other.usedCommodities;
        this.predictedProfile = other.predictedProfile;
    }

    @Override
    public void initializeInterdependentCalculation(
            long interdependentStartingTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(interdependentStartingTime, stepSize, createLoadProfile, keepPrediction);

        if (this.getLoadProfile() != null) {
            this.setLoadProfile(this.predictedProfile.cloneAfter(this.getReferenceTime()));
        }

        if (this.outputStatesCalculatedFor != interdependentStartingTime) {
            long time = interdependentStartingTime;
            ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<>();

            while (time < this.maxHorizon) {
                LimitedCommodityStateMap output = null;
                boolean hasValues = false;
                double[] powers = new double[this.usedCommodities.size()];

                int i = 0;
                for (Commodity c : this.usedCommodities) {
                    powers[i] = this.predictedProfile.getAverageLoadFromTill(c, time, time + stepSize);
                    if (powers[i] != 0.0) {
                        hasValues = true;
                    }
                    i++;
                }

                if (hasValues) {
                    output = new LimitedCommodityStateMap(this.allOutputCommodities);
                    i = 0;
                    for (Commodity c : this.usedCommodities) {
                        output.setPower(c, powers[i]);
                        i++;
                    }
                }
                tempAllOutputStates.add(output);

                time += stepSize;
            }
            //add zero if optimisation goes longer then the profile
            LimitedCommodityStateMap output = new LimitedCommodityStateMap(this.allOutputCommodities);
            for (Commodity c : this.usedCommodities) {
                output.setPower(c, 0.0);
            }
            tempAllOutputStates.add(output);

            this.allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
            this.allOutputStates = tempAllOutputStates.toArray(this.allOutputStates);

            this.outputStatesCalculatedFor = interdependentStartingTime;
        }
    }

    @Override
    public final Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            return new Schedule(this.getLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(),
                    this.getDeviceType().toString());
        }
    }
}
