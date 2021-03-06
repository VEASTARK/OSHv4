package osh.simulation.energy;

import osh.configuration.system.GridConfig;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.EnergySimulationCore;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.grid.EnergySimulationTypes;
import osh.esc.grid.IEnergyGrid;

import java.util.*;
import java.util.Map.Entry;

/**
 * EnergySimulationCore<br>
 * Similar to an multi-agent simulation (MAS)
 *
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SimEnergySimulationCore extends EnergySimulationCore {

    /**
     * CONSTRUCTOR
     */
    public SimEnergySimulationCore(
            List<GridConfig> grids,
            String meterUUID) throws HALManagerException {

        super(grids, meterUUID);
    }

    /**
     * CONSTRUCTOR
     */
    public SimEnergySimulationCore(
            Map<EnergySimulationTypes, IEnergyGrid> grids,
            UUID meterUUID) {
        super(grids, meterUUID);
    }

    public AncillaryMeterState doNextEnergySimulation(
            ArrayList<IDeviceEnergySubject> energySimulationSubjects) {

        // Get output states
        Map<UUID, LimitedCommodityStateMap> simSubjCommodityStates = new HashMap<>();
        for (IDeviceEnergySubject _simSubject : energySimulationSubjects) {

            // get Commodity states of all subjects
            // i.e. electrical power, heating power, ...
//				EnumMap<Commodity,RealCommodityState> commodityList = _simSubject.getCommodityOutputStates();
            LimitedCommodityStateMap commodityList = _simSubject.getCommodityOutputStates();

            UUID simSubjID = _simSubject.getUUID();
            simSubjCommodityStates.put(simSubjID, commodityList);

            //for SimulationDevices

            //looking for some special devices
            //currently NONE
        }

        // Do grid calculations
//		Map<UUID, EnumMap<Commodity,RealCommodityState>> totalInputStates = new HashMap<>();
        Map<UUID, LimitedCommodityStateMap> totalInputStates = new HashMap<>();
//		EnumMap<AncillaryCommodity,AncillaryCommodityState> ancillaryMeterState = new EnumMap<>(AncillaryCommodity.class);
        AncillaryMeterState ancillaryMeterState = new AncillaryMeterState();
        for (Entry<EnergySimulationTypes, IEnergyGrid> grid : this.grids.entrySet()) {
            grid.getValue().doCalculation(simSubjCommodityStates, totalInputStates, ancillaryMeterState);
        }

        // Get AncillaryState of Meter (grid connections)

        // Inform subjects about input states (total flow)
            for (IEnergySubject _simSubject : energySimulationSubjects) {
                UUID simSubjID = _simSubject.getUUID();
//				EnumMap<Commodity,RealCommodityState> simSubjState = totalInputStates.get(simSubjID);
                LimitedCommodityStateMap simSubjState = totalInputStates.get(simSubjID);

                // clone AncillaryMeter AncillaryCommodities
//				EnumMap<AncillaryCommodity,AncillaryCommodityState> clonedAncillaryMeterState = null;
                AncillaryMeterState clonedAncillaryMeterState = ancillaryMeterState.clone();
                //					clonedAncillaryMeterState = new EnumMap<AncillaryCommodity, AncillaryCommodityState>(AncillaryCommodity.class);

//					for (Entry<AncillaryCommodity, AncillaryCommodityState> e : ancillaryMeterState.entrySet()) {
//						try {
//							clonedAncillaryMeterState.put(e.getKey(), e.getValue().clone());
//						} catch (CloneNotSupportedException e1) {
//							e1.printStackTrace();
//						}
//					}

                _simSubject.setCommodityInputStates(simSubjState, clonedAncillaryMeterState);
            }

        return ancillaryMeterState;
    }

}
