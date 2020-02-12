package osh.datatypes.appliance.future;

import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.ILoadProfile;
import osh.datatypes.power.SparseLoadProfile;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ApplianceProgramConfigurationStatus implements Cloneable, Serializable {

    private static final long serialVersionUID = -2752978492500340593L;

    private final ZonedDateTime acpReferenceTime;

    /**
     * ID for every update (with changes)
     */
    private final UUID acpID;

    /**
     * Dynamic Profiles using relative times (!)<br>
     * dim 0: profiles<br>
     * dim 1: segments<br>
     */
    private final SparseLoadProfile[][] dynamicLoadProfiles;

    private final int[][][] minMaxDurations;

    private boolean doNotReschedule;


    public ApplianceProgramConfigurationStatus(
            UUID acpID,
            SparseLoadProfile[][] dynamicLoadProfiles,
            int[][][] minMaxDurations,
            ZonedDateTime acpReferenceTime) {

        this.acpID = acpID;
        this.dynamicLoadProfiles = dynamicLoadProfiles;
        this.minMaxDurations = minMaxDurations;
        this.acpReferenceTime = acpReferenceTime;
    }

    public ApplianceProgramConfigurationStatus(
            UUID acpID,
            SparseLoadProfile[][] dynamicLoadProfiles,
            int[][][] minMaxTimes,
            ZonedDateTime acpReferenceTime,
            boolean doNotReschedule) {

        this.acpID = acpID;
        this.dynamicLoadProfiles = dynamicLoadProfiles;
        this.minMaxDurations = minMaxTimes;
        this.acpReferenceTime = acpReferenceTime;
        this.doNotReschedule = doNotReschedule;
    }

    /**
     * @return Maximum duration (longest profile, with minimum times)
     */
    public static long getTotalMaxDuration(ApplianceProgramConfigurationStatus acp) {
        long val = 0;
        for (int d0 = 0; d0 < acp.dynamicLoadProfiles.length; d0++) {
            long temp = 0;
            for (int d1 = 0; d1 < acp.dynamicLoadProfiles[d0].length; d1++) {
                //OLD: use length of phases
//				temp = temp + acp.dynamicLoadProfiles[d0][d1].getEndingTimeOfProfile();
                //NEW: use minimum times
                temp += acp.getMinMaxDurations()[d0][d1][0];
            }
            val = Math.max(val, temp);
        }
        return val;
    }

    public UUID getAcpID() {
        return this.acpID;
    }

    public SparseLoadProfile[][] getDynamicLoadProfiles() {
        return this.dynamicLoadProfiles;
    }

    public int[][][] getMinMaxDurations() {
        return this.minMaxDurations;
    }

    public ILoadProfile<Commodity> getFinishedProfile(int profileNo) {
        return this.dynamicLoadProfiles[profileNo][this.dynamicLoadProfiles[profileNo].length - 1];
    }

    public ZonedDateTime getAcpReferenceTime() {
        return this.acpReferenceTime;
    }

    public boolean isDoNotReschedule() {
        return this.doNotReschedule;
    }


    // HELPER METHODS

    public void setDoNotReschedule(boolean doNotReschedule) {
        this.doNotReschedule = doNotReschedule;
    }

    @Override
    public Object clone() {
        int[][][] clonedMinMaxTimes = new int[this.minMaxDurations.length][][];
        for (int d0 = 0; d0 < this.minMaxDurations.length; d0++) {
            clonedMinMaxTimes[d0] = new int[this.minMaxDurations[d0].length][];
            for (int d1 = 0; d1 < this.minMaxDurations[d0].length; d1++) {
                clonedMinMaxTimes[d0][d1] = Arrays.copyOf(this.minMaxDurations[d0][d1],
                        this.minMaxDurations[d0][d1].length);
            }
        }
        return new ApplianceProgramConfigurationStatus(this.acpID, this.dynamicLoadProfiles, clonedMinMaxTimes, this.acpReferenceTime, this.doNotReschedule);
    }

}
