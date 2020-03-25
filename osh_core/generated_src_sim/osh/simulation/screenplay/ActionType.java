//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.03.26 at 12:23:09 AM CET 
//


package osh.simulation.screenplay;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ActionType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ActionType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="IDeviceAction"/&gt;
 *     &lt;enumeration value="EvAction"/&gt;
 *     &lt;enumeration value="UserAction"/&gt;
 *     &lt;enumeration value="ProviderSPSAction"/&gt;
 *     &lt;enumeration value="ProviderShortTimeAction"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ActionType")
@XmlEnum
public enum ActionType {

    @XmlEnumValue("IDeviceAction")
    I_DEVICE_ACTION("IDeviceAction"),
    @XmlEnumValue("EvAction")
    EV_ACTION("EvAction"),
    @XmlEnumValue("UserAction")
    USER_ACTION("UserAction"),
    @XmlEnumValue("ProviderSPSAction")
    PROVIDER_SPS_ACTION("ProviderSPSAction"),
    @XmlEnumValue("ProviderShortTimeAction")
    PROVIDER_SHORT_TIME_ACTION("ProviderShortTimeAction");
    private final String value;

    ActionType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static ActionType fromValue(String v) {
        for (ActionType c: ActionType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
