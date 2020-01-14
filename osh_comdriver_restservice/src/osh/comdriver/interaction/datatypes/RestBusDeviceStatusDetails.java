package osh.comdriver.interaction.datatypes;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlType(name = "busDeviceStatusDetails")
public class RestBusDeviceStatusDetails extends RestStateDetail {

    @Enumerated(value = EnumType.STRING)
    protected ConnectionStatus state;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public RestBusDeviceStatusDetails() {
        this(null, 0);
    }

    public RestBusDeviceStatusDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public ConnectionStatus getState() {
        return this.state;
    }

    public void setState(ConnectionStatus state) {
        this.state = state;
    }

    @XmlType
    public enum ConnectionStatus {
        @XmlEnumValue("ATTACHED")
        ATTACHED,
        @XmlEnumValue("DETACHED")
        DETACHED,
        @XmlEnumValue("ERROR")
        ERROR
    }
}
