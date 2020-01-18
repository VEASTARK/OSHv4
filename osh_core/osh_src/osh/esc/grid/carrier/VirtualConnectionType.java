package osh.esc.grid.carrier;

import osh.datatypes.commodity.AncillaryCommodity;

/**
 * @author Ingo Mauser
 */
public abstract class VirtualConnectionType extends ConnectionType {

    private final AncillaryCommodity commodity;

    public VirtualConnectionType(AncillaryCommodity commodity) {
        super();
        this.commodity = commodity;
    }

    public AncillaryCommodity getAncillaryCommodity() {
        return this.commodity;
    }
}
