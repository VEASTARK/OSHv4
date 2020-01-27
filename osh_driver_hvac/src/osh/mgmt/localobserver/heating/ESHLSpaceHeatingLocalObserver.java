package osh.mgmt.localobserver.heating;

import osh.core.interfaces.IOSHOC;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.hal.exchange.IHALExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.hal.exchange.SpaceHeatingPredictionObserverExchange;
import osh.mgmt.ipp.thermal.ThermalDemandNonControllableIPP;
import osh.mgmt.localobserver.ThermalDemandLocalObserver;

import java.util.Map;

/**
 * @author Ingo Mauser, Jan Mueller
 */
public class ESHLSpaceHeatingLocalObserver
        extends ThermalDemandLocalObserver {

    private SparseLoadProfile predictedDemand;
    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * CONSTRUCTOR
     */
    public ESHLSpaceHeatingLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onDeviceStateUpdate() {
        IHALExchange ihex = this.getObserverDataObject();

        if (ihex instanceof SpaceHeatingPredictionObserverExchange) {
            SpaceHeatingPredictionObserverExchange ox = (SpaceHeatingPredictionObserverExchange) ihex;
            long now = this.getTimeDriver().getCurrentEpochSecond();

            // create SparseLoadProfile
            this.predictedDemand = new SparseLoadProfile();
            Map<Long, Double> map = ox.getPredictedHeatConsumptionMap();

            for (Long aLong : map.keySet()) {
                now = aLong;
                Double predictedDemandAtTimeStamp = map.get(now);
                this.predictedDemand.setLoad(Commodity.HEATINGHOTWATERPOWER, now,
                        (int) Math.round(predictedDemandAtTimeStamp));
            }
            this.predictedDemand.setEndingTimeOfProfile(now);

            // Send new IPP
            ThermalDemandNonControllableIPP ipp =
                    new ThermalDemandNonControllableIPP(
                            this.getUUID(),
                            this.getGlobalLogger(),
                            false,
                            now,
                            this.getDeviceType(),
                            this.predictedDemand.clone(),
                            Commodity.HEATINGHOTWATERPOWER,
                            this.compressionType,
                            this.compressionValue);
            this.getOCRegistry().publish(InterdependentProblemPart.class, this, ipp);
        } else if (ihex instanceof StaticCompressionExchange) {
            StaticCompressionExchange _stat = (StaticCompressionExchange) ihex;
            this.compressionType = _stat.getCompressionType();
            this.compressionValue = _stat.getCompressionValue();
        }
    }

}
