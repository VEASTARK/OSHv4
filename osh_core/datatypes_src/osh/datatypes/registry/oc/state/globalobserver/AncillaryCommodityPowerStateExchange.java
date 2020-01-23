package osh.datatypes.registry.oc.state.globalobserver;

import osh.datatypes.commodity.AncillaryCommodity;
import osh.datatypes.registry.StateExchange;

import java.time.ZonedDateTime;
import java.util.EnumMap;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class AncillaryCommodityPowerStateExchange extends StateExchange {

    private static final long serialVersionUID = 7876809071418906992L;
    private final EnumMap<AncillaryCommodity, Integer> map;


    /**
     * CONSTRUCTOR
     */
    public AncillaryCommodityPowerStateExchange(UUID sender, ZonedDateTime timestamp, EnumMap<AncillaryCommodity, Integer> map) {
        super(sender, timestamp);

        this.map = map;
    }


    public EnumMap<AncillaryCommodity, Integer> getMap() {
        return this.map;
    }


    @Override
    public StateExchange clone() {
        EnumMap<AncillaryCommodity, Integer> clonedMap = new EnumMap<>(AncillaryCommodity.class);

        for (Entry<AncillaryCommodity, Integer> e : this.map.entrySet()) {
            clonedMap.put(e.getKey(), e.getValue());
        }

        return new AncillaryCommodityPowerStateExchange(
                this.getSender(),
                this.getTimestamp(),
                clonedMap);
    }
}
