//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.eal;

import osh.configuration.system.BusDeviceClassification;
import osh.configuration.system.BusDeviceTypes;
import osh.configuration.system.ConfigurationParameter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for AssignedBusDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssignedBusDevice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="busDeviceID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="busDeviceType" type="{http://osh/configuration/system}BusDeviceTypes"/>
 *         &lt;element name="busDeviceClassification" type="{http://osh/configuration/system}BusDeviceClassification"/>
 *         &lt;element name="busDeviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="busManagerClassName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="busDriverClassName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="busDriverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssignedBusDevice", propOrder = {
    "busDeviceID",
    "busDeviceType",
    "busDeviceClassification",
    "busDeviceDescription",
    "busManagerClassName",
    "busDriverClassName",
    "busDriverParameters"
})
public class AssignedBusDevice {

    @XmlElement(required = true)
    protected String busDeviceID;
    @XmlElement(required = true)
    protected BusDeviceTypes busDeviceType;
    @XmlElement(required = true)
    protected BusDeviceClassification busDeviceClassification;
    @XmlElement(required = true)
    protected String busDeviceDescription;
    @XmlElement(required = true)
    protected String busManagerClassName;
    @XmlElement(required = true)
    protected String busDriverClassName;
    protected List<ConfigurationParameter> busDriverParameters;

    /**
     * Gets the value of the busDeviceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusDeviceID() {
        return busDeviceID;
    }

    /**
     * Sets the value of the busDeviceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusDeviceID(String value) {
        this.busDeviceID = value;
    }

    /**
     * Gets the value of the busDeviceType property.
     * 
     * @return
     *     possible object is
     *     {@link BusDeviceTypes }
     *     
     */
    public BusDeviceTypes getBusDeviceType() {
        return busDeviceType;
    }

    /**
     * Sets the value of the busDeviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusDeviceTypes }
     *     
     */
    public void setBusDeviceType(BusDeviceTypes value) {
        this.busDeviceType = value;
    }

    /**
     * Gets the value of the busDeviceClassification property.
     * 
     * @return
     *     possible object is
     *     {@link BusDeviceClassification }
     *     
     */
    public BusDeviceClassification getBusDeviceClassification() {
        return busDeviceClassification;
    }

    /**
     * Sets the value of the busDeviceClassification property.
     * 
     * @param value
     *     allowed object is
     *     {@link BusDeviceClassification }
     *     
     */
    public void setBusDeviceClassification(BusDeviceClassification value) {
        this.busDeviceClassification = value;
    }

    /**
     * Gets the value of the busDeviceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusDeviceDescription() {
        return busDeviceDescription;
    }

    /**
     * Sets the value of the busDeviceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusDeviceDescription(String value) {
        this.busDeviceDescription = value;
    }

    /**
     * Gets the value of the busManagerClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusManagerClassName() {
        return busManagerClassName;
    }

    /**
     * Sets the value of the busManagerClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusManagerClassName(String value) {
        this.busManagerClassName = value;
    }

    /**
     * Gets the value of the busDriverClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBusDriverClassName() {
        return busDriverClassName;
    }

    /**
     * Sets the value of the busDriverClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBusDriverClassName(String value) {
        this.busDriverClassName = value;
    }

    /**
     * Gets the value of the busDriverParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the busDriverParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBusDriverParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getBusDriverParameters() {
        if (busDriverParameters == null) {
            busDriverParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.busDriverParameters;
    }

}
