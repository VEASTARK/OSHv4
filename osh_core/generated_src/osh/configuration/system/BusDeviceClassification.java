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
 * <p>Java class for BusDeviceClassification.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="BusDeviceClassification">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="GATEWAY"/>
 *     &lt;enumeration value="BUSCONTROLLER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 */
@XmlType(name = "BusDeviceClassification")
@XmlEnum
public enum BusDeviceClassification {

    GATEWAY,
    BUSCONTROLLER;

    public static BusDeviceClassification fromValue(String v) {
        return valueOf(v);
    }

    public String value() {
        return this.name();
    }

}
