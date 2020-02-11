package osh.esc.grid.carrier;

import osh.datatypes.commodity.Commodity;

import java.io.Serializable;

/**
 * @author Ingo Mauser
 */
public class ThermalConnection extends RealConnectionType implements Serializable {

    /**
     * Serial ID
     */
    private static final long serialVersionUID = -5019503350463452048L;

    public ThermalConnection(Commodity commodity) {
        super(commodity);
    }

    /**
     * only for serialization, do not use
     */
    @Deprecated
    protected ThermalConnection() {
    }

}
