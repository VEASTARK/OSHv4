//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.10 at 03:54:47 PM CEST 
//


package osh.configuration.system;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ComDeviceTypes.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ComDeviceTypes"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="USERINTERACTIONDEVICE"/&gt;
 *     &lt;enumeration value="GUI"/&gt;
 *     &lt;enumeration value="MULTI_COMMODITY"/&gt;
 *     &lt;enumeration value="ELECTRICITY"/&gt;
 *     &lt;enumeration value="GAS"/&gt;
 *     &lt;enumeration value="WATER"/&gt;
 *     &lt;enumeration value="SEWAGE"/&gt;
 *     &lt;enumeration value="GENERALLOGGER"/&gt;
 *     &lt;enumeration value="FILELOGGER"/&gt;
 *     &lt;enumeration value="DATABASELOGGER"/&gt;
 *     &lt;enumeration value="OTHER"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ComDeviceTypes")
@XmlEnum
public enum ComDeviceTypes {

    USERINTERACTIONDEVICE,
    GUI,
    MULTI_COMMODITY,
    ELECTRICITY,
    GAS,
    WATER,
    SEWAGE,
    GENERALLOGGER,
    FILELOGGER,
    DATABASELOGGER,
    OTHER;

    public String value() {
        return name();
    }

    public static ComDeviceTypes fromValue(String v) {
        return valueOf(v);
    }

}
