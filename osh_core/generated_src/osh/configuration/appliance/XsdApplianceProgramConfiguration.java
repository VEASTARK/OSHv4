//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.appliance;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for xsd.ApplianceProgramConfiguration complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="xsd.ApplianceProgramConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ConfigurationID" type="{http://osh/configuration/appliance}nonNegativeInt"/>
 *         &lt;element name="ConfigurationName" type="{http://osh/configuration/appliance}name" minOccurs="0"/>
 *         &lt;element name="Program" type="{http://osh/configuration/appliance}xsd.Program"/>
 *         &lt;element name="Parameters" type="{http://osh/configuration/appliance}xsd.ConfigurationParameters" minOccurs="0"/>
 *         &lt;element name="LoadProfiles" type="{http://osh/configuration/appliance}XsdLoadProfiles"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "xsd.ApplianceProgramConfiguration", propOrder = {
        "configurationID",
        "configurationName",
        "program",
        "parameters",
        "loadProfiles"
})
public class XsdApplianceProgramConfiguration {

    @XmlElement(name = "ConfigurationID")
    protected int configurationID;
    @XmlElement(name = "ConfigurationName")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String configurationName;
    @XmlElement(name = "Program", required = true)
    protected XsdProgram program;
    @XmlElement(name = "Parameters")
    protected XsdConfigurationParameters parameters;
    @XmlElement(name = "LoadProfiles", required = true)
    protected XsdLoadProfiles loadProfiles;

    /**
     * Gets the value of the configurationID property.
     */
    public int getConfigurationID() {
        return this.configurationID;
    }

    /**
     * Sets the value of the configurationID property.
     */
    public void setConfigurationID(int value) {
        this.configurationID = value;
    }

    /**
     * Gets the value of the configurationName property.
     *
     * @return possible object is
     * {@link String }
     */
    public String getConfigurationName() {
        return this.configurationName;
    }

    /**
     * Sets the value of the configurationName property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setConfigurationName(String value) {
        this.configurationName = value;
    }

    /**
     * Gets the value of the program property.
     *
     * @return possible object is
     * {@link XsdProgram }
     */
    public XsdProgram getProgram() {
        return this.program;
    }

    /**
     * Sets the value of the program property.
     *
     * @param value allowed object is
     *              {@link XsdProgram }
     */
    public void setProgram(XsdProgram value) {
        this.program = value;
    }

    /**
     * Gets the value of the parameters property.
     *
     * @return possible object is
     * {@link XsdConfigurationParameters }
     */
    public XsdConfigurationParameters getParameters() {
        return this.parameters;
    }

    /**
     * Sets the value of the parameters property.
     *
     * @param value allowed object is
     *              {@link XsdConfigurationParameters }
     */
    public void setParameters(XsdConfigurationParameters value) {
        this.parameters = value;
    }

    /**
     * Gets the value of the loadProfiles property.
     *
     * @return possible object is
     * {@link XsdLoadProfiles }
     */
    public XsdLoadProfiles getLoadProfiles() {
        return this.loadProfiles;
    }

    /**
     * Sets the value of the loadProfiles property.
     *
     * @param value allowed object is
     *              {@link XsdLoadProfiles }
     */
    public void setLoadProfiles(XsdLoadProfiles value) {
        this.loadProfiles = value;
    }

}
