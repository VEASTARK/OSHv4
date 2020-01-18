package osh.comdriver.interaction.datatypes;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import java.util.UUID;


/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlType
public class RestConfigurationDetails extends RestStateDetail {

    @Enumerated(value = EnumType.STRING)
    protected ConfigurationStatus configurationStatus;
    protected UUID usedBy;

    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    public RestConfigurationDetails() {
        this(null, 0);
    }

    public RestConfigurationDetails(UUID sender, long timestamp) {
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
