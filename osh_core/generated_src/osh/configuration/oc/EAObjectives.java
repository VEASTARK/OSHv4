//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.12 at 10:41:12 PM CEST 
//


package osh.configuration.oc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for EAObjectives.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="EAObjectives"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="MONEY"/&gt;
 *     &lt;enumeration value="SELF_SUFFICIENCY_RATIO"/&gt;
 *     &lt;enumeration value="SELF_CONSUMPTION_RATIO"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "EAObjectives")
@XmlEnum
public enum EAObjectives {

    MONEY,
    SELF_SUFFICIENCY_RATIO,
    SELF_CONSUMPTION_RATIO;

    public String value() {
        return name();
    }

    public static EAObjectives fromValue(String v) {
        return valueOf(v);
    }

}
