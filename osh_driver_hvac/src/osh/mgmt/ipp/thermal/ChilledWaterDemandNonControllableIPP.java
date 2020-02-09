package osh.mgmt.ipp.thermal;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.PreCalculatedNonControllableIPP;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder
 */
public class ChilledWaterDemandNonControllableIPP
        extends PreCalculatedNonControllableIPP {

    private static final long serialVersionUID = 3835919942638394624L;

    private final ArrayList<ChillerCalendarDate> dates;
    private final Map<Long, Double> temperaturePrediction;


    /**
     * CONSTRUCTOR
     */
    public ChilledWaterDemandNonControllableIPP(
            UUID deviceId,
            ZonedDateTime timeStamp,
            boolean toBeScheduled,
            ArrayList<ChillerCalendarDate> dates,
            Map<Long, Double> temperaturePrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                toBeScheduled,
                timeStamp,
                DeviceTypes.SPACECOOLING,
               EnumSet.of(Commodity.COLDWATERPOWER),
                compressionType,
                compressionValue
        );

        this.dates = new ArrayList<>();
        for (ChillerCalendarDate chillerCalendarDate : dates) {
            ChillerCalendarDate date = new ChillerCalendarDate(
                    chillerCalendarDate.getStartTimestamp(),
                    chillerCalendarDate.getLength(),
                    chillerCalendarDate.getAmountOfPerson(),
                    chillerCalendarDate.getSetTemperature(),
                    chillerCalendarDate.getKnownPower());
            this.dates.add(date);
        }

        this.temperaturePrediction = Collections.unmodifiableMap(temperaturePrediction);
    }

    public ChilledWaterDemandNonControllableIPP(ChilledWaterDemandNonControllableIPP other) {
        super(other);
        this.dates = other.dates;
        this.temperaturePrediction = other.temperaturePrediction;
    }

    @Override
    public ChilledWaterDemandNonControllableIPP getClone() {
        return new ChilledWaterDemandNonControllableIPP(this);
    }


    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected ChilledWaterDemandNonControllableIPP() {
        super();
        this.temperaturePrediction = new HashMap<>();
        this.dates = new ArrayList<>();
    }


    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            int stepSize,
            boolean calculateLoadProfile,
            boolean keepPrediction) {

        super.initializeInterdependentCalculation(maxReferenceTime, stepSize, calculateLoadProfile, keepPrediction);

        ArrayList<ChillerCalendarDate> datesForEvaluation = new ArrayList<>();
        for (ChillerCalendarDate chillerCalendarDate : this.dates) {
            ChillerCalendarDate date = new ChillerCalendarDate(
                    chillerCalendarDate.getStartTimestamp(),
                    chillerCalendarDate.getLength(),
                    chillerCalendarDate.getAmountOfPerson(),
                    chillerCalendarDate.getSetTemperature(),
                    chillerCalendarDate.getKnownPower());
            datesForEvaluation.add(date);
        }

        if (this.outputStatesCalculatedFor != maxReferenceTime) {
            long time = maxReferenceTime;
            ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<>();
            double coldWaterPower;

            while (time < this.maxHorizon) {
                LimitedCommodityStateMap output = null;

                coldWaterPower = 0;
                // Date active?
                if (!datesForEvaluation.isEmpty()) {

                    ChillerCalendarDate date = datesForEvaluation.get(0);

                    if (date.getStartTimestamp() <= time
                            && date.getStartTimestamp() + date.getLength() >= time) {

                        long secondsFromYearStart =
                                TimeConversion.getSecondsSinceYearStart(TimeConversion.convertUnixTimeToZonedDateTime(time));

                        double outdoorTemperature = this.temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
                        coldWaterPower = Math.max(0, ((0.4415 * outdoorTemperature) - 9.6614) * 1000);

                    } else if (date.getStartTimestamp() + date.getLength() < time) {
                        datesForEvaluation.remove(0);
                    }

                    if (coldWaterPower > 0) {
                        output = new LimitedCommodityStateMap(this.allOutputCommodities);
                        output.setPower(Commodity.COLDWATERPOWER, coldWaterPower);
                    }
                }
                tempAllOutputStates.add(output);

                if (this.getLoadProfile() != null)
                    this.getLoadProfile().setLoad(Commodity.COLDWATERPOWER, time, (int) coldWaterPower);

                time += stepSize;
            }
            //add zero if optimisation goes longer then the profile
            LimitedCommodityStateMap output = new LimitedCommodityStateMap(this.allOutputCommodities);
            output.setPower(Commodity.COLDWATERPOWER, 0.0);
            tempAllOutputStates.add(output);

            this.allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
            this.allOutputStates = tempAllOutputStates.toArray(this.allOutputStates);
            if (this.getLoadProfile() != null)
                this.getLoadProfile().setLoad(Commodity.COLDWATERPOWER, time, 0);
            this.outputStatesCalculatedFor = maxReferenceTime;
        }
        this.setOutputStates(null);

    }

    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.getLoadProfile() == null) {
            return new Schedule(new SparseLoadProfile(), this.getInterdependentCervisia(),
                    this.getDeviceType().toString());
        } else {
            SparseLoadProfile slp = this.getLoadProfile().getCompressedProfile(this.compressionType,
                    this.compressionValue, this.compressionValue);
            return new Schedule(slp, this.getInterdependentCervisia(), this.getDeviceType().toString());
        }
    }

    @Override
    public String problemToString() {
        return "SpaceCooling NonControllableIPP";
    }
}
