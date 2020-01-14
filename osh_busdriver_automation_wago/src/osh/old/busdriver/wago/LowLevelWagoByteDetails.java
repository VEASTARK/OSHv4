package osh.old.busdriver.wago;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * @author Till Schuberth
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "LowLevelWagoByteDetails")
@XmlType
public class LowLevelWagoByteDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = 3314846974618916971L;
    protected byte data;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public LowLevelWagoByteDetails() {
        this(null, 0);
    }

    public LowLevelWagoByteDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public byte getData() {
        return this.data;
    }

    public void setData(byte data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof LowLevelWagoByteDetails))
            return false;
        LowLevelWagoByteDetails other = (LowLevelWagoByteDetails) obj;

        return (this.data == other.data);
    }

    @Override
    public String toString() {
        return "LowLevelWagoByte: 0x" + Integer.toHexString(this.data);
    }
}
