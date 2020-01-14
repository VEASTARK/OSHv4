package osh.esc.grid.carrier;

import osh.datatypes.commodity.Commodity;

import java.io.Serializable;

/**
 * @author Ingo Mauser
 */
public abstract class RealConnectionType extends ConnectionType implements Serializable {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = 2077136216258497838L;
    private Commodity commodity;

    public RealConnectionType(Commodity commodity) {
        super();
        this.commodity = commodity;
    }

    /**
     * only for serialisation, do not use normally
     */
    @Deprecated
    protected RealConnectionType() {

    }

    public Commodity getCommodity() {
        return this.commodity;
    }
}
