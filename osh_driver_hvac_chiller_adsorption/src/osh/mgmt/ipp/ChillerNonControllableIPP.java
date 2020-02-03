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
import osh.driver.chiller.AdsorptionChillerModel;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.BitSet;
import java.util.Map;
import java.util.UUID;


/**
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder
 */
public class ChillerNonControllableIPP extends NonControllableIPP<ISolution, IPrediction> {

    /**
     * prediction horizon in seconds
     */
    public final static int RELATIVE_HORIZON = 0; // no horizon...it's stupid...
    /**
     * slot length in [s]
     */
    public final static long TIME_PER_SLOT = 5 * 60; // 5 minutes
    /**
     *
     */
    private static final long serialVersionUID = -8273266479216299094L;
    private final int typicalStandbyActivePower = 10; // [W]
    private final int typicalRunningActivePower = 420; // [W]
    /**
     * is AdChiller on at the beginning
     */
    private final boolean initialAdChillerState;
    private final Map<Long, Double> temperaturePrediction;

    // temperature control
    private final double coldWaterStorageMinTemp = 10.0;
    private final double coldWaterStorageMaxTemp = 15.0;

    private final double hotWaterStorageMinTemp = 55.0;
    private final double hotWaterStorageMaxTemp = 80.0;

//	/** delta T below maximum cold water temperature */
//	private double hysteresis = 1.0;


    // ### interdependent stuff ###
    /**
     * used for iteration in interdependent calculation (ancillary time in the future)
     */
    private long interdependentTime;
    /**
     * running times of chiller
     */
    private double interdependentCervisia;
    private boolean interdependentLastState;

    /**
     * from cold water tank IPP
     */
    private double currentColdWaterTemperature = 12;
    /**
     * from hot water tank IPP
     */
    private double currentHotWaterTemperature = 60;

    private SparseLoadProfile lp;


    /**
     * CONSTRUCTOR
     *
     * @param deviceId
     * @param timeStamp
     */
    public ChillerNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
            boolean toBeScheduled,
            boolean initialAdChillerState,
            Map<Long, Double> temperaturePrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                toBeScheduled,
                false, //needsAncillaryMeterState
                true, //reactsToInputStates
                false, //is not static
                timeStamp,
                DeviceTypes.ADSORPTIONCHILLER,
                new Commodity[]{Commodity.ACTIVEPOWER,
                        Commodity.REACTIVEPOWER,
                        Commodity.HEATINGHOTWATERPOWER,
                        Commodity.COLDWATERPOWER
                },
                compressionType,
                compressionValue);

        this.initialAdChillerState = initialAdChillerState;
        this.temperaturePrediction = temperaturePrediction;

        this.allInputCommodities = new Commodity[]{Commodity.HEATINGHOTWATERPOWER, Commodity.COLDWATERPOWER};
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        // used for iteration in interdependent calculation
        this.setOutputStates(null);
        this.interdependentInputStates = null;

        if (createLoadProfile) {
            this.lp = new SparseLoadProfile();
        } else {
            this.lp = null;
        }

        if (this.getReferenceTime() != maxReferenceTime) {
            this.setReferenceTime(maxReferenceTime);
        }

        this.stepSize = stepSize;

        this.interdependentCervisia = 0.0;
        this.interdependentTime = this.getReferenceTime();

        this.interdependentLastState = this.initialAdChillerState;
    }

    @Override
    public synchronized void calculateNextStep() {


        // ### interdependent logic (hysteresis control) ###
        // "cold water control" (min, max temperatures)

        // update water temperatures

        if (this.interdependentInputStates == null) {
            System.out.println("No interdependentInputStates available.");
        }

        this.currentHotWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

        this.currentColdWaterTemperature = this.interdependentInputStates.getTemperature(Commodity.COLDWATERPOWER);

        boolean chillerNewState = this.interdependentLastState;

        // AdChiller control (forced on/off)

        if (this.interdependentLastState) {
            if (this.currentColdWaterTemperature <= this.coldWaterStorageMinTemp) {
                chillerNewState = false;
            }
            if (this.currentHotWaterTemperature < this.hotWaterStorageMinTemp
                    || this.currentHotWaterTemperature > this.hotWaterStorageMaxTemp) {
                chillerNewState = false;
            }
        } else {
            if (this.currentColdWaterTemperature > this.coldWaterStorageMaxTemp
                    && this.currentHotWaterTemperature > this.hotWaterStorageMinTemp
                    && this.currentHotWaterTemperature < this.hotWaterStorageMaxTemp) {
                chillerNewState = true;
            }
        }

        // ### set power profiles and interdependentCervisia

//		if (chillerNewState) {
//			// the later the better AND the less the better
//			this.interdependentCervisia += 0.001 * (ab.length - i); 
//		}


        // calculate power values
        double activePower = this.typicalStandbyActivePower;
        double hotWaterPower = 0;
        double coldWaterPower = 0;

        if (chillerNewState) {
            long secondsFromYearStart = TimeConversion.getSecondsSinceYearStart(TimeConversion.convertUnixTimeToZonedDateTime(this.interdependentTime));
            double outdoorTemperature = this.temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
            activePower = this.typicalRunningActivePower;
            coldWaterPower = AdsorptionChillerModel.chilledWaterPower(this.currentHotWaterTemperature, outdoorTemperature);
        }

        if (chillerNewState || this.interdependentLastState || this.interdependentTime == this.getReferenceTime()) {


            if (this.lp != null) {
                this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, (int) activePower);
                this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, (int) hotWaterPower);
                this.lp.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, (int) coldWaterPower);
            }

            this.internalInterdependentOutputStates.setPower(
                    Commodity.ACTIVEPOWER, activePower);
            this.internalInterdependentOutputStates.setPower(
                    Commodity.HEATINGHOTWATERPOWER, hotWaterPower);
            this.internalInterdependentOutputStates.setPower(
                    Commodity.COLDWATERPOWER, coldWaterPower);

            this.setOutputStates(this.internalInterdependentOutputStates);
        }

        if (chillerNewState && !this.interdependentLastState) {
            // fixed costs per start, i.e., costs to turn on the CHP
            // (not the variable costs for letting the CHP run)
            this.interdependentCervisia += 10.0;
        }

        this.interdependentLastState = chillerNewState;
        this.interdependentTime += this.stepSize;
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.lp == null) {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
        } else {
            if (this.lp.getEndingTimeOfProfile() > 0) {
                this.lp.setLoad(Commodity.ACTIVEPOWER, this.interdependentTime, this.typicalStandbyActivePower);
                this.lp.setLoad(Commodity.HEATINGHOTWATERPOWER, this.interdependentTime, 0);
                this.lp.setLoad(Commodity.COLDWATERPOWER, this.interdependentTime, 0);
            }

            SparseLoadProfile slp = this.lp.getCompressedProfileByDiscontinuities(50);
            return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
        }
    }

    // HELPER STUFF


    // ### to string ###

    @Override
    public String problemToString() {
        return "Chiller NonControllableIPP";
    }

    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        //NOTHING
    }

}
