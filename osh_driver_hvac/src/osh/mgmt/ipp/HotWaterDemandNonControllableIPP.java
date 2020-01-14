package osh.mgmt.ipp;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import osh.configuration.system.DeviceTypes;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.esc.LimitedCommodityStateMap;

import java.util.BitSet;
import java.util.UUID;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class HotWaterDemandNonControllableIPP
        extends ThermalDemandNonControllableIPP {

    private static final long serialVersionUID = -1011574853269626608L;

    private SparseLoadProfile powerPrediction;
    private Commodity usedCommodity;


    /**
     * CONSTRUCTOR
     */
    public HotWaterDemandNonControllableIPP(
            UUID deviceId,
            DeviceTypes deviceType,
            IGlobalLogger logger,
            long now,
            boolean toBeScheduled,
            SparseLoadProfile powerPrediction,
            Commodity usedCommodity,
            LoadProfileCompressionTypes compressionType,
            int compressionValue) {
        super(
                deviceId,
                logger,
                toBeScheduled,
                false, //does not need ancillary meter state as Input State
                false, //does not react to input states
                now,
                deviceType,
                new Commodity[]{
                        usedCommodity
                },
                compressionType,
                compressionValue);

        this.powerPrediction = powerPrediction.getCompressedProfile(
                this.compressionType,
                this.compressionValue,
                this.compressionValue);
        this.usedCommodity = usedCommodity;
    }

    /**
     * CONSTRUCTOR
     * for serialization only, do NOT use
     */
    @Deprecated
    protected HotWaterDemandNonControllableIPP() {
        super();
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
            this.lp = this.powerPrediction.cloneAfter(maxReferenceTime);
        else
            this.lp = null;

        if (this.outputStatesCalculatedFor != maxReferenceTime) {
            long time = maxReferenceTime;
//			ObjectArrayList<EnumMap<Commodity, RealCommodityState>> tempAllOutputStates = new ObjectArrayList<EnumMap<Commodity, RealCommodityState>>();
            ObjectArrayList<LimitedCommodityStateMap> tempAllOutputStates = new ObjectArrayList<>();

            Commodity[] usedCommodities = {this.usedCommodity};

            double lastPower = -1;

            while (time < this.maxHorizon) {
//				EnumMap<Commodity, RealCommodityState> output = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
                LimitedCommodityStateMap output = null;
                double power = this.powerPrediction.getAverageLoadFromTill(this.usedCommodity, time, time + stepSize);

                if (power != 0 || lastPower != 0) {
                    output = new LimitedCommodityStateMap(usedCommodities);
                    output.setPower(this.usedCommodity, power);

                    lastPower = power;
                }

//				output.put(usedCommodity, new RealThermalCommodityState(usedCommodity, 
//						, 0.0, null));
                tempAllOutputStates.add(output);

                time += stepSize;
            }
            //add zero if optimization goes longer then the profile
//			EnumMap<Commodity, RealCommodityState> output = new EnumMap<Commodity, RealCommodityState>(Commodity.class);				
//			output.put(usedCommodity, new RealThermalCommodityState(usedCommodity, 0.0, 0.0, null));
//			tempAllOutputStates.add(output);
            LimitedCommodityStateMap output = new LimitedCommodityStateMap(usedCommodities);
            output.setPower(this.usedCommodity, 0);
            tempAllOutputStates.add(output);

//			allOutputStates = (EnumMap<Commodity, RealCommodityState>[]) new EnumMap<?, ?>[tempAllOutputStates.size()];
            this.allOutputStates = new LimitedCommodityStateMap[tempAllOutputStates.size()];
            this.allOutputStates = tempAllOutputStates.toArray(this.allOutputStates);
            this.outputStatesCalculatedFor = maxReferenceTime;
        }

//		interdependentOutputStates = new EnumMap<Commodity, RealCommodityState>(Commodity.class);
        this.setOutputStates(null);
    }


    @Override
    public Schedule getFinalInterdependentSchedule() {
        if (this.lp != null) {
            if (this.lp.getEndingTimeOfProfile() > this.interdependentTime) {
                this.lp.setLoad(
                        this.usedCommodity,
                        this.interdependentTime,
                        0);
            }
            return new Schedule(this.lp, this.interdependentCervisia, this.getDeviceType().toString());
        } else {
            return new Schedule(new SparseLoadProfile(), this.interdependentCervisia, this.getDeviceType().toString());
        }
    }

    // ### to string ###

    @Override
    public String problemToString() {
        return "[" + this.getTimestamp() + "] HotWaterDemandNonControllableIPP";
    }

}
