package osh.mgmt.ipp;

import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.ea.interfaces.IPrediction;
import osh.datatypes.ea.interfaces.ISolution;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.NonControllableIPP;
import osh.driver.ihe.SmartHeaterModel;

import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.UUID;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SmartHeaterNonControllableIPP
        extends NonControllableIPP<ISolution, IPrediction> {

    private static final long serialVersionUID = -7540136211941577232L;
    private final int temperatureSetting;
    private final int initialState;
    private final long[] timestampOfLastChangePerSubElement;
    private SmartHeaterModel model;


    /**
     * CONSTRUCTOR
     *
     * @param deviceId
     * @param timeStamp
     */
    public SmartHeaterNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
            int temperatureSetting,
            int initialState,
            long[] timestampOfLastChangePerSubElement,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {

        super(
                deviceId,
                logger,
                false, //does not cause scheduling
                true, //needs ancillary meter state as Input State
                true, //reacts to input states
                false, //is not static
                timeStamp,
                DeviceTypes.INSERTHEATINGELEMENT,
                EnumSet.of(Commodity.ACTIVEPOWER, Commodity.REACTIVEPOWER),
                compressionType,
                compressionValue);

        this.temperatureSetting = temperatureSetting;
        this.initialState = initialState;
        this.timestampOfLastChangePerSubElement = timestampOfLastChangePerSubElement;

        this.model = new SmartHeaterModel(
                temperatureSetting,
                initialState,
                timestampOfLastChangePerSubElement);

        this.setAllInputCommodities(EnumSet.of(Commodity.HEATINGHOTWATERPOWER));
    }


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected SmartHeaterNonControllableIPP() {
        super();
        this.temperatureSetting = 0;
        this.initialState = 0;
        this.timestampOfLastChangePerSubElement = null;
    }


    @Override
    public void recalculateEncoding(long currentTime, long maxHorizon) {
        this.setReferenceTime(currentTime);
        // get new temperature of tank
        //  better not...new IPP instead
    }


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean createLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, createLoadProfile, keepPrediction);

        this.model = new SmartHeaterModel(
                this.temperatureSetting,
                this.initialState,
                this.timestampOfLastChangePerSubElement);
    }

    @Override
    public void calculateNextStep() {

        if (this.interdependentInputStates != null) {

            double availablePower = 0;

            if (this.ancillaryMeterState != null) {

                // #1
//				double chpFeedIn = 0;
//				double pvFeedIn = 0;
//				if (ancillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN) != null) {
//					chpFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.CHPACTIVEPOWERFEEDIN).getPower());
//				}
//				if (ancillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN) != null) {
//					pvFeedIn = Math.abs(AncillaryInputStates.get(AncillaryCommodity.PVACTIVEPOWERFEEDIN).getPower());
//				}
//				availablePower = (int) (chpFeedIn + pvFeedIn);

                // #2
                availablePower = (int) this.ancillaryMeterState.getPower(AncillaryCommodity.ACTIVEPOWEREXTERNAL);

//					if (availablePower > 1500) {
//						@SuppressWarnings("unused")
//						int xxx = 0;
//					}
            }

            // get temperature of tank
            double temperature = this.interdependentInputStates.getTemperature(Commodity.HEATINGHOTWATERPOWER);

            // update state
            this.model.updateAvailablePower(this.getInterdependentTime(), availablePower, temperature);

            // update interdependentOutputStates
            this.internalInterdependentOutputStates.setPower(
                    Commodity.ACTIVEPOWER, this.model.getPower());
            this.internalInterdependentOutputStates.setPower(
                    Commodity.HEATINGHOTWATERPOWER, -this.model.getPower());
            this.setOutputStates(this.internalInterdependentOutputStates);

            if (this.getLoadProfile() != null) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), (int) this.model.getPower());
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), (int) -this.model.getPower());
            }
        } else {
            this.getGlobalLogger().logDebug("interdependentInputStates == null");
        }

        this.incrementInterdependentTime();
    }

    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.getLoadProfile() != null) {
            if (this.getLoadProfile().getEndingTimeOfProfile() > 0) {
                this.getLoadProfile().setLoad(Commodity.ACTIVEPOWER, this.getInterdependentTime(), 0);
                this.getLoadProfile().setLoad(Commodity.HEATINGHOTWATERPOWER, this.getInterdependentTime(), 0);
            }
            return new Schedule(this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue), this.getInterdependentCervisia(), this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getReferenceTime() + "] SmartHeaterNonControllableIPP setTemperature=" + this.temperatureSetting + " initialState=" + this.initialState;
    }
}