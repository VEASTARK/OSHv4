package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;
import osh.registry.interfaces.IPromiseToBeImmutable;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlType
public class BusDeviceStatusDetails extends StateExchange implements IPromiseToBeImmutable {

    /**
     *
     */
    private static final long serialVersionUID = -8380215142279886946L;
    protected final ConnectionStatus state;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    protected BusDeviceStatusDetails() {
        this(null, -1L, ConnectionStatus.UNDEFINED);
    }

    public BusDeviceStatusDetails(UUID sender, long timestamp, ConnectionStatus state) {
        super(sender, timestamp);

        this.state = state;
    }

    public ConnectionStatus getState() {
        return this.state;
    }

    @Override
    public String toString() {
        return "BusDeviceStatus: " + this.state.name();
    }

    @XmlType
    public enum ConnectionStatus {
        @XmlEnumValue("ATTACHED")
        ATTACHED,
        @XmlEnumValue("DETACHED")
        DETACHED,
        @XmlEnumValue("ERROR")
        ERROR,
        @XmlEnumValue("UNDEFINED")
        UNDEFINED
    }
}
