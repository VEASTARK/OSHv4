//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.03.31 at 07:51:52 PM CEST 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for BusDeviceTypes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BusDeviceTypes"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="CHILLIICONTROLLER"/&gt;
 *     &lt;enumeration value="EEBUSGATEWAY"/&gt;
 *     &lt;enumeration value="HABITEQGATEWAY"/&gt;
 *     &lt;enumeration value="HAGERGATEWAY"/&gt;
 *     &lt;enumeration value="MIELEGATEWAY"/&gt;
 *     &lt;enumeration value="PLUGWISEGATEWAY"/&gt;
 *     &lt;enumeration value="WAGO750-860"/&gt;
 *     &lt;enumeration value="FILELOGGER"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "BusDeviceTypes")
@XmlEnum
public enum BusDeviceTypes {

    CHILLIICONTROLLER("CHILLIICONTROLLER"),
    EEBUSGATEWAY("EEBUSGATEWAY"),
    HABITEQGATEWAY("HABITEQGATEWAY"),
    HAGERGATEWAY("HAGERGATEWAY"),
    MIELEGATEWAY("MIELEGATEWAY"),
    PLUGWISEGATEWAY("PLUGWISEGATEWAY"),
    @XmlEnumValue("WAGO750-860")
    WAGO_750_860("WAGO750-860"),
    FILELOGGER("FILELOGGER"),
    OTHER("OTHER");
    private final String value;

    BusDeviceTypes(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static BusDeviceTypes fromValue(String v) {
        for (BusDeviceTypes c: BusDeviceTypes.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
