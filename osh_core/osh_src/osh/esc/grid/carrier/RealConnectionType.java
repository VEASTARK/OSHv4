package osh.esc.grid.carrier;

import osh.datatypes.commodity.Commodity;

/**
 * @author Ingo Mauser
 */
public abstract class RealConnectionType extends ConnectionType {

    private final Commodity commodity;

    public RealConnectionType(Commodity commodity) {
        super();
        this.commodity = commodity;
    }

    public Commodity getCommodity() {
        return this.commodity;
    }
}
