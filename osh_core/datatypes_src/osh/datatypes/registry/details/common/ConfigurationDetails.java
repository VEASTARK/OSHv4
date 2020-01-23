package osh.datatypes.registry.details.common;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * @author Kaibin Bao, Ingo Mauser
 */

public class ConfigurationDetails extends StateExchange {

    /**
     *
     */
    private static final long serialVersionUID = -8016555994484536360L;
    private ConfigurationStatus configurationStatus;
    private UUID usedBy;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    private ConfigurationDetails() {
        this(null, 0);
    }

    public ConfigurationDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    public ConfigurationStatus getConfigurationStatus() {
        return this.configurationStatus;
    }

    public void setConfigurationStatus(ConfigurationStatus configurationStatus) {
        this.configurationStatus = configurationStatus;
    }

    public UUID getUsedBy() {
        return this.usedBy;
    }

    public void setUsedBy(UUID usedBy) {
        this.usedBy = usedBy;
    }

    @Override
    public String toString() {
        return "Configuration status: " + this.configurationStatus.name();
    }

    @XmlType
    public enum ConfigurationStatus {
        @XmlEnumValue("UNCONFIGURED")
        UNCONFIGURED,
        @XmlEnumValue("CONFIGURED")
        CONFIGURED,
        @XmlEnumValue("USED")
        USED,
        @XmlEnumValue("ERROR")
        ERROR
    }
}
