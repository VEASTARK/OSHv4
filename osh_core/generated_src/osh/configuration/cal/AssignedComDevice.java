//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:09 PM CET 
//


package osh.configuration.cal;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import osh.configuration.system.ComDeviceClassification;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;


/**
 * <p>Java class for AssignedComDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssignedComDevice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="comDeviceID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comDeviceType" type="{http://osh/configuration/system}ComDeviceTypes"/>
 *         &lt;element name="comDeviceClassification" type="{http://osh/configuration/system}ComDeviceClassification"/>
 *         &lt;element name="comDeviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comManagerClassName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comDriverClassName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="comDriverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssignedComDevice", propOrder = {
    "comDeviceID",
    "comDeviceType",
    "comDeviceClassification",
    "comDeviceDescription",
    "comManagerClassName",
    "comDriverClassName",
    "comDriverParameters"
})
public class AssignedComDevice {

    @XmlElement(required = true)
    protected String comDeviceID;
    @XmlElement(required = true)
    protected ComDeviceTypes comDeviceType;
    @XmlElement(required = true)
    protected ComDeviceClassification comDeviceClassification;
    @XmlElement(required = true)
    protected String comDeviceDescription;
    @XmlElement(required = true)
    protected String comManagerClassName;
    @XmlElement(required = true)
    protected String comDriverClassName;
    protected List<ConfigurationParameter> comDriverParameters;

    /**
     * Gets the value of the comDeviceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComDeviceID() {
        return comDeviceID;
    }

    /**
     * Sets the value of the comDeviceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComDeviceID(String value) {
        this.comDeviceID = value;
    }

    /**
     * Gets the value of the comDeviceType property.
     * 
     * @return
     *     possible object is
     *     {@link ComDeviceTypes }
     *     
     */
    public ComDeviceTypes getComDeviceType() {
        return comDeviceType;
    }

    /**
     * Sets the value of the comDeviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComDeviceTypes }
     *     
     */
    public void setComDeviceType(ComDeviceTypes value) {
        this.comDeviceType = value;
    }

    /**
     * Gets the value of the comDeviceClassification property.
     * 
     * @return
     *     possible object is
     *     {@link ComDeviceClassification }
     *     
     */
    public ComDeviceClassification getComDeviceClassification() {
        return comDeviceClassification;
    }

    /**
     * Sets the value of the comDeviceClassification property.
     * 
     * @param value
     *     allowed object is
     *     {@link ComDeviceClassification }
     *     
     */
    public void setComDeviceClassification(ComDeviceClassification value) {
        this.comDeviceClassification = value;
    }

    /**
     * Gets the value of the comDeviceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComDeviceDescription() {
        return comDeviceDescription;
    }

    /**
     * Sets the value of the comDeviceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComDeviceDescription(String value) {
        this.comDeviceDescription = value;
    }

    /**
     * Gets the value of the comManagerClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComManagerClassName() {
        return comManagerClassName;
    }

    /**
     * Sets the value of the comManagerClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComManagerClassName(String value) {
        this.comManagerClassName = value;
    }

    /**
     * Gets the value of the comDriverClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getComDriverClassName() {
        return comDriverClassName;
    }

    /**
     * Sets the value of the comDriverClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setComDriverClassName(String value) {
        this.comDriverClassName = value;
    }

    /**
     * Gets the value of the comDriverParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the comDriverParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComDriverParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getComDriverParameters() {
        if (comDriverParameters == null) {
            comDriverParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.comDriverParameters;
    }

}
