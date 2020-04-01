//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.01 at 07:36:51 PM CEST 
//


package osh.configuration.grid;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LayoutConnection complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="LayoutConnection"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="activeEntityUUID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="passiveEntityUUID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="activeToPassiveCommodity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="passiveToActiveCommodity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LayoutConnection", propOrder = {
    "activeEntityUUID",
    "passiveEntityUUID",
    "activeToPassiveCommodity",
    "passiveToActiveCommodity"
})
public class LayoutConnection {

    @XmlElement(required = true)
    protected String activeEntityUUID;
    @XmlElement(required = true)
    protected String passiveEntityUUID;
    @XmlElement(required = true)
    protected String activeToPassiveCommodity;
    @XmlElement(required = true)
    protected String passiveToActiveCommodity;

    /**
     * Gets the value of the activeEntityUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiveEntityUUID() {
        return activeEntityUUID;
    }

    /**
     * Sets the value of the activeEntityUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiveEntityUUID(String value) {
        this.activeEntityUUID = value;
    }

    /**
     * Gets the value of the passiveEntityUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassiveEntityUUID() {
        return passiveEntityUUID;
    }

    /**
     * Sets the value of the passiveEntityUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassiveEntityUUID(String value) {
        this.passiveEntityUUID = value;
    }

    /**
     * Gets the value of the activeToPassiveCommodity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActiveToPassiveCommodity() {
        return activeToPassiveCommodity;
    }

    /**
     * Sets the value of the activeToPassiveCommodity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActiveToPassiveCommodity(String value) {
        this.activeToPassiveCommodity = value;
    }

    /**
     * Gets the value of the passiveToActiveCommodity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPassiveToActiveCommodity() {
        return passiveToActiveCommodity;
    }

    /**
     * Sets the value of the passiveToActiveCommodity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPassiveToActiveCommodity(String value) {
        this.passiveToActiveCommodity = value;
    }

}
