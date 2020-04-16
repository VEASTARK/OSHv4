//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.16 at 05:56:33 PM CEST 
//


package osh.configuration.oc;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for rankingType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="rankingType"&gt;
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *     &lt;enumeration value="DOMINANCE"/&gt;
 *     &lt;enumeration value="OBJECTIVE"/&gt;
 *     &lt;enumeration value="WEIGHTED_OBJECTIVE"/&gt;
 *     &lt;enumeration value="CHEBYSHEV"/&gt;
 *     &lt;enumeration value="NASH_BARGAINING"/&gt;
 *   &lt;/restriction&gt;
 * &lt;/simpleType&gt;
 * </pre>
 * 
 */
@XmlType(name = "rankingType")
@XmlEnum
public enum RankingType {

    DOMINANCE,
    OBJECTIVE,
    WEIGHTED_OBJECTIVE,
    CHEBYSHEV,
    NASH_BARGAINING;

    public String value() {
        return name();
    }

    public static RankingType fromValue(String v) {
        return valueOf(v);
    }

}
