//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.12 at 10:41:12 PM CEST 
//


package osh.configuration.appliance;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="ApplianceProgramConfiguration" type="{http://osh/configuration/appliance}xsd.ApplianceProgramConfiguration" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "applianceProgramConfiguration"
})
@XmlRootElement(name = "xsd.ApplianceProgramConfigurations")
public class XsdApplianceProgramConfigurations {

    @XmlElement(name = "ApplianceProgramConfiguration", required = true)
    protected List<XsdApplianceProgramConfiguration> applianceProgramConfiguration;

    /**
     * Gets the value of the applianceProgramConfiguration property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the applianceProgramConfiguration property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getApplianceProgramConfiguration().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XsdApplianceProgramConfiguration }
     * 
     * 
     */
    public List<XsdApplianceProgramConfiguration> getApplianceProgramConfiguration() {
        if (applianceProgramConfiguration == null) {
            applianceProgramConfiguration = new ArrayList<XsdApplianceProgramConfiguration>();
        }
        return this.applianceProgramConfiguration;
    }

}
