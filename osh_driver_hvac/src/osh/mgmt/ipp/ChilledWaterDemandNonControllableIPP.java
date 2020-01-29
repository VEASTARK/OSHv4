package osh.mgmt.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.datatypes.cooling.ChillerCalendarDate;
import osh.esc.LimitedCommodityStateMap;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * @author Ingo Mauser, Florian Allerding, Till Schuberth, Julian Feder
 */
public class ChilledWaterDemandNonControllableIPP
        extends ThermalDemandNonControllableIPP {

    private static final long serialVersionUID = 3835919942638394624L;

    private final ArrayList<ChillerCalendarDate> dates;
    private final Map<Long, Double> temperaturePrediction;
    private ArrayList<ChillerCalendarDate> datesForEvaluation;
    private double coldWaterPower;


    /**
     * CONSTRUCTOR
     */
    public ChilledWaterDemandNonControllableIPP(
            UUID deviceId,
            IGlobalLogger logger,
            ZonedDateTime timeStamp,
            boolean toBeScheduled,
            ArrayList<ChillerCalendarDate> dates,
            Map<Long, Double> temperaturePrediction,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                toBeScheduled,
                false,    //needsAncillaryMeterstate
                false,    //reactsToInputStates
                timeStamp,
                DeviceTypes.SPACECOOLING,
                new Commodity[]{
                        Commodity.COLDWATERPOWER,
                },
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

        this.temperaturePrediction = temperaturePrediction;

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


    // ### interdependent problem part stuff ###

    @Override
    public void initializeInterdependentCalculation(
            long maxReferenceTime,
            BitSet solution,
            int stepSize,
            boolean calculateLoadProfile,
            boolean keepPrediction) {

        if (maxReferenceTime != this.getReferenceTime())
            this.interdependentTime = maxReferenceTime;
        else
            this.interdependentTime = this.getReferenceTime();

        this.stepSize = stepSize;

        if (calculateLoadProfile)
            this.lp = new SparseLoadProfile();
        else
            this.lp = null;

        this.interdependentCervisia = 0.0;

        this.coldWaterPower = 0;
        this.datesForEvaluation = new ArrayList<>();
        for (ChillerCalendarDate chillerCalendarDate : this.dates) {
            ChillerCalendarDate date = new ChillerCalendarDate(
                    chillerCalendarDate.getStartTimestamp(),
                    chillerCalendarDate.getLength(),
                    chillerCalendarDate.getAmountOfPerson(),
                    chillerCalendarDate.getSetTemperature(),
                    chillerCalendarDate.getKnownPower());
            this.datesForEvaluation.add(date);
        }

        if (this.outputStatesCalculatedFor != maxReferenceTime) {
            long time = maxReferenceTime;
            ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<>();

            while (time < this.maxHorizon) {
                LimitedCommodityStateMap output = null;

                this.coldWaterPower = 0;
                // Date active?
                if (!this.datesForEvaluation.isEmpty()) {

                    ChillerCalendarDate date = this.datesForEvaluation.get(0);

                    if (date.getStartTimestamp() <= time
                            && date.getStartTimestamp() + date.getLength() >= time) {

                        long secondsFromYearStart = TimeConversion.convertUnixTime2SecondsFromYearStart(time);

                        double outdoorTemperature = this.temperaturePrediction.get((secondsFromYearStart / 300) * 300); // keep it!!
                        this.coldWaterPower = Math.max(0, ((0.4415 * outdoorTemperature) - 9.6614) * 1000);

//						if (demand < 0) {
//							System.out.println("Demand:" + demand + "outdoor: " + currentOutdoorTemperature);
//						}
                    } else if (date.getStartTimestamp() + date.getLength() < time) {
                        this.datesForEvaluation.remove(0);
                    }

                    output = new LimitedCommodityStateMap(this.allOutputCommodities);
                    output.setPower(Commodity.COLDWATERPOWER, this.coldWaterPower);
                }
                tempAllOutputStates.add(output);

                if (this.lp != null)
                    this.lp.setLoad(Commodity.COLDWATERPOWER, time, (int) this.coldWaterPower);

                time += stepSize;
            }
            //add zero if optimisation goes longer then the profile
            LimitedCommodityStateMap output = new LimitedCommodityStateMap(this.allOutputCommodities);
            output.setPower(Commodity.COLDWATERPOWER, 0.0);
            tempAllOutputStates.add(output);

            this.allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
            this.allOutputStates = tempAllOutputStates.toArray(this.allOutputStates);
            if (this.lp != null)
                this.lp.setLoad(Commodity.COLDWATERPOWER, time, 0);
            this.outputStatesCalculatedFor = maxReferenceTime;
        }
        this.setOutputStates(null);

    }


    @Override
    public Schedule getFinalInterdependentSchedule() {

        if (this.lp == null) {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
        } else {
            SparseLoadProfile slp = this.lp.getCompressedProfile(this.compressionType, this.compressionValue, this.compressionValue);
            return new Schedule(slp, this.interdependentCervisia, this.getDeviceType().toString());
        }
    }

    @Override
    public void setCommodityInputStates(
            LimitedCommodityStateMap inputStates,
//			EnumMap<AncillaryCommodity, AncillaryCommodityState> ancillaryInputStates)
            AncillaryMeterState ancillaryMeterState) {
        //Do Nothing
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "SpaceCooling NonControllableIPP";
    }

}
