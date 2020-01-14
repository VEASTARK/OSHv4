package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlType;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
@XmlType(name = "lightStateDetails")
public class RestLightStateDetails extends RestStateDetail {

    protected boolean state;

    /**
     * for JAXB
     */
    @Deprecated
    public RestLightStateDetails() {
        this(null, 0);
    }

    public RestLightStateDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }


    public boolean isState() {
        return this.state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

}
