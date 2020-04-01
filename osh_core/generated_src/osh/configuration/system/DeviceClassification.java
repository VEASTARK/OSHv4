//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.01 at 07:36:51 PM CEST 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for DeviceClassification.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="DeviceClassification"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="HVAC"/&gt;
 *     &lt;enumeration value="APPLIANCE"/&gt;
 *     &lt;enumeration value="LUMINAIRE"/&gt;
 *     &lt;enumeration value="ELECTRICVEHICLE"/&gt;
 *     &lt;enumeration value="METERING"/&gt;
 *     &lt;enumeration value="SENSOR"/&gt;
 *     &lt;enumeration value="SMARTPLUG"/&gt;
 *     &lt;enumeration value="VIRTUALSWITCH"/&gt;
 *     &lt;enumeration value="CHPPLANT"/&gt;
 *     &lt;enumeration value="PVSYSTEM"/&gt;
 *     &lt;enumeration value="HEATPUMP"/&gt;
 *     &lt;enumeration value="BASELOAD"/&gt;
 *     &lt;enumeration value="REMOTECONTROL"/&gt;
 *     &lt;enumeration value="CUSTOMER"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *     &lt;enumeration value="BATTERYSTORAGE"/&gt;
 *     &lt;enumeration value="N/A"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "DeviceClassification")
@XmlEnum
public enum DeviceClassification {

    HVAC("HVAC"),
    APPLIANCE("APPLIANCE"),
    LUMINAIRE("LUMINAIRE"),
    ELECTRICVEHICLE("ELECTRICVEHICLE"),
    METERING("METERING"),
    SENSOR("SENSOR"),
    SMARTPLUG("SMARTPLUG"),
    VIRTUALSWITCH("VIRTUALSWITCH"),
    CHPPLANT("CHPPLANT"),
    PVSYSTEM("PVSYSTEM"),
    HEATPUMP("HEATPUMP"),
    BASELOAD("BASELOAD"),
    REMOTECONTROL("REMOTECONTROL"),
    CUSTOMER("CUSTOMER"),
    OTHER("OTHER"),
    BATTERYSTORAGE("BATTERYSTORAGE"),
    @XmlEnumValue("N/A")
    N_A("N/A");
    private final String value;

    DeviceClassification(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static DeviceClassification fromValue(String v) {
        for (DeviceClassification c: DeviceClassification.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
