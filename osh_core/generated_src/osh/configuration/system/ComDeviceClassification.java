//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.10 at 05:32:57 PM CEST 
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
 * &lt;simpleType name="ComDeviceClassification"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="USERINTERACTION"/&gt;
 *     &lt;enumeration value="UTILITY"/&gt;
 *     &lt;enumeration value="LOGGER"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ComDeviceClassification")
@XmlEnum
public enum ComDeviceClassification {

    USERINTERACTION,
    UTILITY,
    LOGGER,
    OTHER;

    public String value() {
        return name();
    }

    public static ComDeviceClassification fromValue(String v) {
        return valueOf(v);
    }

}
