package osh.mgmt.localcontroller.ipp;

import osh.configuration.system.DeviceTypes;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.registry.oc.ipp.StaticIPP;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * ENDPROGRAMMED IPP -> nothing to do
 *
 * @author Ingo Mauser
 */
public class FutureAppliancesStaticIPP extends StaticIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = 4075698811201735555L;



    /**
     * Constructs this static future appliance ipp with the given information.
     *
     * @param deviceId the unique identifier of the underlying device
     * @param timestamp the time-stamp of creation of this problem-part
     * @param toBeScheduled if the publication of this problem-part should cause a rescheduling
     * @param deviceType type of device represented by this problem-part
     * @param schedule the final schedule of the underlying device
     * @param compressionType type of compression to be used for load profiles
     * @param compressionValue associated value to be used for compression
     */
    public FutureAppliancesStaticIPP(
            UUID deviceId,
            ZonedDateTime timestamp,
            boolean toBeScheduled,
            DeviceTypes deviceType,
            Schedule schedule,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                timestamp,
                toBeScheduled,
                deviceType,
                schedule,
                compressionType,
                compressionValue,
                "FutureStaticApplianceIPP");
    }

    /**
     * Limited copy-constructor that constructs a copy of the given future appliance ipp that is as shallow as
     * possible while still not conflicting with multithreaded use inside the optimization-loop. </br>
     * NOT to be used to generate a complete deep copy!
     *
     * @param other the future appliance ipp to copy
     */
    public FutureAppliancesStaticIPP(FutureAppliancesStaticIPP other) {
        super(other);
    }

    @Override
    public FutureAppliancesStaticIPP getClone() {
        return new FutureAppliancesStaticIPP(this);
    }
}
