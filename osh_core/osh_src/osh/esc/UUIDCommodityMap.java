package osh.esc;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import osh.datatypes.commodity.Commodity;

import java.io.Serializable;
import java.util.*;

public class UUIDCommodityMap implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2823226803772827321L;

    private Object2IntOpenHashMap<UUID> keyMap;

    private LimitedCommodityStateMap[] innerValues;

    private int[] partIdToArrayIdMap;


    public UUIDCommodityMap(UUIDCommodityMap other) {
        this.keyMap = other.keyMap;
        this.partIdToArrayIdMap = Arrays.copyOf(other.partIdToArrayIdMap, other.partIdToArrayIdMap.length);
        this.innerValues = new LimitedCommodityStateMap[other.innerValues.length];
        for (int i = 0; i < this.innerValues.length; i++) {
            this.innerValues[i] = new LimitedCommodityStateMap(other.innerValues[i]);
        }
    }

    public UUIDCommodityMap(Set<UUID> allUUIDs,
                            Object2IntOpenHashMap<UUID> uuidIntMap,
                            Object2ObjectOpenHashMap<UUID, EnumSet<Commodity>> uuidInputMap,
                            boolean makeNoMap) {
        this.keyMap = null;
        this.partIdToArrayIdMap = new int[uuidIntMap.size()];
        this.innerValues = new LimitedCommodityStateMap[allUUIDs.size()];

        Arrays.fill(this.partIdToArrayIdMap, -1);

        Iterator<UUID> it = allUUIDs.iterator();
        for (int i = 0; i < allUUIDs.size(); i++) {
            UUID curr = it.next();
            if (uuidIntMap.containsKey(curr)) {
                this.partIdToArrayIdMap[uuidIntMap.getInt(curr)] = i;
                this.innerValues[i] = new LimitedCommodityStateMap(uuidInputMap.get(curr));
            } else {
                throw new IllegalArgumentException("no mapping for specified key");
            }
        }
    }

    public UUIDCommodityMap(Set<UUID> allUUIDs, Object2IntOpenHashMap<UUID> uuidIntMap) {
        this(allUUIDs, uuidIntMap, false);
    }

    public UUIDCommodityMap(Set<UUID> allUUIDs, Object2IntOpenHashMap<UUID> uuidIntMap, boolean makeNoMap) {
        if (makeNoMap) this.keyMap = null;
        else this.keyMap = new Object2IntOpenHashMap<>(allUUIDs.size());
        this.partIdToArrayIdMap = new int[uuidIntMap.size()];
        this.innerValues = new LimitedCommodityStateMap[allUUIDs.size()];

        if (!makeNoMap) this.keyMap.defaultReturnValue(-1);
        Arrays.fill(this.partIdToArrayIdMap, -1);

        Iterator<UUID> it = allUUIDs.iterator();
        for (int i = 0; i < allUUIDs.size(); i++) {
            UUID curr = it.next();
            if (uuidIntMap.containsKey(curr)) {
                if (!makeNoMap) this.keyMap.put(curr, i);
                this.partIdToArrayIdMap[uuidIntMap.getInt(curr)] = i;
            } else {
                throw new IllegalArgumentException("no mapping for specified key");
            }
        }

        for (int i = 0; i < this.innerValues.length; i++) {
            this.innerValues[i] = new LimitedCommodityStateMap();
        }
    }

    /**
     * for serialisation only - do not use
     */
    @Deprecated
    protected UUIDCommodityMap() {

    }

    public void put(int id, LimitedCommodityStateMap stateMap) {
        this.innerValues[this.partIdToArrayIdMap[id]] = stateMap;
    }

    public void put(UUID uuid, LimitedCommodityStateMap stateMap) {
        this.innerValues[this.keyMap.getInt(uuid)] = stateMap;
    }

    public LimitedCommodityStateMap get(int id) {
        return this.innerValues[this.partIdToArrayIdMap[id]];
    }

    public LimitedCommodityStateMap get(UUID uuid) {
        return this.innerValues[this.keyMap.getInt(uuid)];
    }

    public double getPower(int id, Commodity commodity) {
        return this.innerValues[this.partIdToArrayIdMap[id]].getPower(commodity);
    }

    public void setPower(int id, Commodity commodity, double power) {
        this.innerValues[this.partIdToArrayIdMap[id]].setPower(commodity, power);
    }

    public double getTemperature(int id, Commodity commodity) {
        return this.innerValues[this.partIdToArrayIdMap[id]].getTemperature(commodity);
    }

    public void setTemperature(int id, Commodity commodity, double temperature) {
        this.innerValues[this.partIdToArrayIdMap[id]].setTemperature(commodity, temperature);
    }

    public void clearInnerStates() {
        for (LimitedCommodityStateMap map : this.innerValues) {
            if (map != null) {
                map.clear();
            }
        }
    }

    public boolean containsKey(UUID uuid) {
        return this.keyMap.containsKey(uuid);
    }
}
