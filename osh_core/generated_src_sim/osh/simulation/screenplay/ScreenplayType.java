//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.03.31 at 07:51:52 PM CEST 
//


package osh.simulation.screenplay;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for ScreenplayType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="ScreenplayType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="STATIC"/&gt;
 *     &lt;enumeration value="DYNAMIC"/&gt;
 *     &lt;enumeration value="GUI"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "ScreenplayType")
@XmlEnum
public enum ScreenplayType {

    STATIC,
    DYNAMIC,
    GUI;

    public String value() {
        return name();
    }

    public static ScreenplayType fromValue(String v) {
        return valueOf(v);
    }

}
