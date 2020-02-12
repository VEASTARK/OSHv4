package osh.mgmt.localcontroller.ipp;

import osh.datatypes.ea.interfaces.ISolution;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.UUID;


/**
 * @author Ingo Mauser
 */
public class GenericApplianceSolution implements ISolution {

    public final UUID acpUUID;
    public final long[] startingTimes;
    public final int profileId;


    /**
     * CONSTRUCTOR
     *
     * @param acpUUID
     * @param startingTimes
     * @param profileId
     */
    public GenericApplianceSolution(
            UUID acpUUID,
            long[] startingTimes,
            int profileId) {
        super();

        this.acpUUID = acpUUID;
        this.startingTimes = startingTimes;
        this.profileId = profileId;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.acpUUID == null) ? 0 : this.acpUUID.hashCode());
        result = prime * result + this.profileId;
        result = prime * result + Arrays.hashCode(this.startingTimes);
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        GenericApplianceSolution other = (GenericApplianceSolution) obj;
        if (this.acpUUID == null) {
            if (other.acpUUID != null)
                return false;
        } else if (!this.acpUUID.equals(other.acpUUID))
            return false;
        if (this.profileId != other.profileId)
            return false;
        return Arrays.equals(this.startingTimes, other.startingTimes);
    }

    public ZonedDateTime[] getZonedStartingTimes() {
        return Arrays.stream(this.startingTimes).mapToObj(TimeConversion::convertUnixTimeToZonedDateTime).toArray(ZonedDateTime[]::new);
    }


    @Override
    public GenericApplianceSolution clone() {
        return new GenericApplianceSolution(
                this.acpUUID,
                Arrays.copyOf(this.startingTimes, this.startingTimes.length),
                this.profileId);
    }


    @Override
    public String toString() {
        StringBuilder pausesString = new StringBuilder("[");
        if (this.startingTimes != null) {
            for (int i = 0; i < this.startingTimes.length; i++) {
                if (i > 0) {
                    pausesString.append(",");
                }
                pausesString.append(this.startingTimes[i]);
            }
        }
        pausesString.append("]");
        return "referenceTime=" + this.acpUUID
                + " | profileId=" + this.profileId
                + " | pauses=" + pausesString;
    }
}