//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.appliance;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for XsdLoadProfile complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="XsdLoadProfile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Phases" type="{http://osh/configuration/appliance}XsdPhases"/>
 *       &lt;/sequence>
 *       &lt;attribute name="id" use="required" type="{http://osh/configuration/appliance}nonNegativeInt" />
 *       &lt;attribute name="name" type="{http://osh/configuration/appliance}name" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "XsdLoadProfile", propOrder = {
        "phases"
})
public class XsdLoadProfile {

    @XmlElement(name = "Phases", required = true)
    protected XsdPhases phases;
    @XmlAttribute(name = "id", required = true)
    protected int id;
    @XmlAttribute(name = "name")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String name;

    /**
     * Gets the value of the phases property.
     *
     * @return possible object is
     * {@link XsdPhases }
     */
    public XsdPhases getPhases() {
        return this.phases;
    }

    /**
     * Sets the value of the phases property.
     *
     * @param value allowed object is
     *              {@link XsdPhases }
     */
    public void setPhases(XsdPhases value) {
        this.phases = value;
    }

    /**
     * Gets the value of the id property.
     */
    public int getId() {
        return this.id;
    }

    /**
     * Sets the value of the id property.
     */
    public void setId(int value) {
        this.id = value;
    }

    /**
     * Gets the value of the name property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getName() {
        return this.name;
    }

    /**
     * Sets the value of the name property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setName(String value) {
        this.name = value;
    }

}
