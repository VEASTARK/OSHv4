package osh.esc.grid.carrier;

import osh.datatypes.commodity.Commodity;

import java.io.Serializable;

/**
 * @author Ingo Mauser
 */
public class ElectricalConnection extends RealConnectionType implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -8016297442193124095L;

    public ElectricalConnection(Commodity commodity) {
        super(commodity);
    }

    /**
     * only for serialisation, do not use normally
     */
    @Deprecated
    protected ElectricalConnection() {

    }

}