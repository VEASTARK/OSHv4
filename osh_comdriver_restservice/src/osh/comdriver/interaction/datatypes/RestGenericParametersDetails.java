package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@XmlType(name = "genericParametersDetails")
public class RestGenericParametersDetails extends RestStateDetail {
    protected Map<String, String> map;

    /**
     * for JAXB
     */
    @Deprecated
    public RestGenericParametersDetails() {
        this(null, 0);
    }

    public RestGenericParametersDetails(UUID sender, long timestamp) {
        super(sender, timestamp);

        this.map = new HashMap<>();
    }

    public void setParameter(String key, String value) {
        this.map.put(key, value);
    }

    public Map<String, String> getMap() {
        return this.map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
