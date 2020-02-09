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
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected FutureAppliancesStaticIPP() {
        super();
    }

    /**
     * CONSTRUCTOR
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

    public FutureAppliancesStaticIPP(FutureAppliancesStaticIPP other) {
        super(other);
    }

    @Override
    public FutureAppliancesStaticIPP getClone() {
        return new FutureAppliancesStaticIPP(this);
    }
}
