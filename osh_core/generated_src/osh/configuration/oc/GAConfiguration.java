//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.oc;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import osh.configuration.system.ConfigurationParameter;


/**
 * <p>Java class for GAConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GAConfiguration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="numEvaluations" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="popSize" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="crossoverOperator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="mutationOperator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="selectionOperator" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="crossoverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="mutationParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="selectionParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="stoppingRules" type="{http://osh/configuration/oc}StoppingRule" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GAConfiguration", propOrder = {
    "numEvaluations",
    "popSize",
    "crossoverOperator",
    "mutationOperator",
    "selectionOperator",
    "crossoverParameters",
    "mutationParameters",
    "selectionParameters",
    "stoppingRules"
})
public class GAConfiguration {

    protected int numEvaluations;
    protected int popSize;
    @XmlElement(required = true)
    protected String crossoverOperator;
    @XmlElement(required = true)
    protected String mutationOperator;
    @XmlElement(required = true)
    protected String selectionOperator;
    protected List<ConfigurationParameter> crossoverParameters;
    protected List<ConfigurationParameter> mutationParameters;
    protected List<ConfigurationParameter> selectionParameters;
    protected List<StoppingRule> stoppingRules;

    /**
     * Gets the value of the numEvaluations property.
     * 
     */
    public int getNumEvaluations() {
        return numEvaluations;
    }

    /**
     * Sets the value of the numEvaluations property.
     * 
     */
    public void setNumEvaluations(int value) {
        this.numEvaluations = value;
    }

    /**
     * Gets the value of the popSize property.
     * 
     */
    public int getPopSize() {
        return popSize;
    }

    /**
     * Sets the value of the popSize property.
     * 
     */
    public void setPopSize(int value) {
        this.popSize = value;
    }

    /**
     * Gets the value of the crossoverOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCrossoverOperator() {
        return crossoverOperator;
    }

    /**
     * Sets the value of the crossoverOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCrossoverOperator(String value) {
        this.crossoverOperator = value;
    }

    /**
     * Gets the value of the mutationOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMutationOperator() {
        return mutationOperator;
    }

    /**
     * Sets the value of the mutationOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMutationOperator(String value) {
        this.mutationOperator = value;
    }

    /**
     * Gets the value of the selectionOperator property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSelectionOperator() {
        return selectionOperator;
    }

    /**
     * Sets the value of the selectionOperator property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSelectionOperator(String value) {
        this.selectionOperator = value;
    }

    /**
     * Gets the value of the crossoverParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the crossoverParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCrossoverParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getCrossoverParameters() {
        if (crossoverParameters == null) {
            crossoverParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.crossoverParameters;
    }

    /**
     * Gets the value of the mutationParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the mutationParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getMutationParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getMutationParameters() {
        if (mutationParameters == null) {
            mutationParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.mutationParameters;
    }

    /**
     * Gets the value of the selectionParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the selectionParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSelectionParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getSelectionParameters() {
        if (selectionParameters == null) {
            selectionParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.selectionParameters;
    }

    /**
     * Gets the value of the stoppingRules property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the stoppingRules property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getStoppingRules().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link StoppingRule }
     * 
     * 
     */
    public List<StoppingRule> getStoppingRules() {
        if (stoppingRules == null) {
            stoppingRules = new ArrayList<StoppingRule>();
        }
        return this.stoppingRules;
    }

}
