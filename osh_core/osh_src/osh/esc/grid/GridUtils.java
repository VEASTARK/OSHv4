package osh.esc.grid;

import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.commodity.Commodity;
import osh.esc.LimitedCommodityStateMap;
import osh.esc.grid.carrier.RealConnectionType;
import osh.utils.functions.TriConsumer;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class GridUtils {

    public static <T extends RealConnectionType> void doCalculation(
            Map<UUID, LimitedCommodityStateMap> localCommodityStates,
            Map<UUID, LimitedCommodityStateMap> totalInputStates,
            AncillaryMeterState ancillaryMeterState,
            List<EnergyRelation<T>> relationList,
            TriConsumer<LimitedCommodityStateMap, LimitedCommodityStateMap, Commodity> updateActivePart,
            TriConsumer<LimitedCommodityStateMap, LimitedCommodityStateMap, Commodity> updatePassivePart,
            TriConsumer<Map<UUID, LimitedCommodityStateMap>, Map<UUID, LimitedCommodityStateMap>, AncillaryMeterState> calculateMeter) {

        for (EnergyRelation<T> rel : relationList) {

            UUID activeId = rel.getActiveEntity().getDeviceUuid();
            UUID passiveId = rel.getPassiveEntity().getDeviceUuid();

            if (localCommodityStates.containsKey(activeId)
                    || localCommodityStates.containsKey(passiveId)) {

                Commodity activeCommodity = rel.getActiveToPassive().getCommodity();
                Commodity passiveCommodity = rel.getPassiveToActive().getCommodity();

                LimitedCommodityStateMap activeLocalCommodities = localCommodityStates.get(activeId);
                LimitedCommodityStateMap passiveLocalCommodities = localCommodityStates.get(passiveId);

                boolean hasActive = activeLocalCommodities != null && activeLocalCommodities.containsCommodity(activeCommodity);
                boolean hasPassive = passiveLocalCommodities != null && passiveLocalCommodities.containsCommodity(passiveCommodity);

                if (!hasActive && !hasPassive) {
                    continue;
                }

                // update active part...
                if (hasPassive) {
                    // Active Part has no input state power

                    LimitedCommodityStateMap activeMap = totalInputStates.get(activeId);
                    if (activeMap == null) {
                        activeMap = new LimitedCommodityStateMap();
                        totalInputStates.put(activeId, activeMap);
                    }

                    updateActivePart.accept(activeMap, passiveLocalCommodities, passiveCommodity);

                    // do not consider power: active part determines it's own power
                }

                // update passive part...
                if (hasActive) {
                    LimitedCommodityStateMap passiveMap = totalInputStates.get(passiveId);
                    if (passiveMap == null) {
                        passiveMap = new LimitedCommodityStateMap();
                        totalInputStates.put(passiveId, passiveMap);
                    }

                    updatePassivePart.accept(passiveMap, activeLocalCommodities, activeCommodity);
                }
            }
        }

        calculateMeter.accept(localCommodityStates, totalInputStates, ancillaryMeterState);
    }
}
