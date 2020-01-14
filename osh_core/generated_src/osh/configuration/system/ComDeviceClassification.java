//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComDeviceClassification.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ComDeviceClassification">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="USERINTERACTION"/>
 *     &lt;enumeration value="UTILITY"/>
 *     &lt;enumeration value="LOGGER"/>
 *     &lt;enumeration value="OTHER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "ComDeviceClassification")
@XmlEnum
public enum ComDeviceClassification {

    USERINTERACTION,
    UTILITY,
    LOGGER,
    OTHER;

    public static ComDeviceClassification fromValue(String v) {
        return valueOf(v);
    }

    public String value() {
        return this.name();
    }

}
