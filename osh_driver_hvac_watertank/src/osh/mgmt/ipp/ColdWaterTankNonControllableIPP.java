package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.thermal.SimpleColdWaterTank;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.UUID;


/**
 * @author Florian Allerding, Ingo Mauser, Till Schuberth
 */
public class ColdWaterTankNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = -7475173612656137600L;
    private final double initialTemperature;
    private final double tankCapacity = 3000.0;
    private final double tankDiameter = 1.0;
    private final double ambientTemperature = 20.0;
    private SimpleColdWaterTank waterTank;

    /**
     * CONSTRUCTOR
     */
    public ColdWaterTankNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
            double initialTemperature,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                false, //does not cause scheduling
                false, //does not need ancillary meter state as Input State
                true, // reacts to input states
                false, //is not static
                timeStamp,
                DeviceTypes.COLDWATERSTORAGE,
                new Commodity[]{Commodity.COLDWATERPOWER},
                compressionType,
                compressionValue);

        this.initialTemperature = initialTemperature;
        this.allInputCommodities = new Commodity[]{Commodity.COLDWATERPOWER};
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected ColdWaterTankNonControllableIPP() {
        super();
        this.initialTemperature = 0;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        // get new temperature of tank
        //  better not...new IPP instead
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        this.stepSize = stepSize;
        this.setOutputStates(null);
        this.interdependentInputStates = null;

        this.waterTank = new SimpleColdWaterTank(
                this.tankCapacity,
                this.tankDiameter,
                this.initialTemperature,
                this.ambientTemperature);

        this.waterTank.reduceByStandingHeatLoss(maxReferenceTime - this.getTimestamp().toEpochSecond());
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            // update tank according to interdependentInputStates
            double coldWaterPower = this.interdependentInputStates.getPower(Commodity.COLDWATERPOWER);
            if (coldWaterPower != 0) {
                this.waterTank.addPowerOverTime(coldWaterPower, this.stepSize, null, null);
            }

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setTemperature(Commodity.COLDWATERPOWER, this.waterTank.getCurrentWaterTemperature());
            this.setOutputStates(this.internalInterdependentOutputStates);
        }

        // reduce by standing loss
        this.waterTank.reduceByStandingHeatLoss(this.stepSize);

    }


    @Override
    public Schedule getFinalInterdependentSchedule() {
        return new Schedule(new SparseLoadProfile(), 0, this.getDeviceType().toString());
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "FIXME FIRST !!!! [" + this.getReferenceTime() + "] ColdWaterTank current temperature = " + (this.waterTank != null ? this.waterTank.getCurrentWaterTemperature() : null);
    }

}
