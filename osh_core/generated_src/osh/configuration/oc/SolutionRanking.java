//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.03.26 at 12:23:09 AM CET 
//


package osh.configuration.oc;

import osh.configuration.system.ConfigurationParameter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for SolutionRanking complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SolutionRanking"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="type" type="{http://osh/configuration/oc}rankingType"/&gt;
 *         &lt;element name="rankingParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SolutionRanking", propOrder = {
    "type",
    "rankingParameters"
})
public class SolutionRanking {

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected RankingType type;
    protected List<ConfigurationParameter> rankingParameters;

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link RankingType }
     *     
     */
    public RankingType getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link RankingType }
     *     
     */
    public void setType(RankingType value) {
        this.type = value;
    }

    /**
     * Gets the value of the rankingParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the rankingParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getRankingParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getRankingParameters() {
        if (rankingParameters == null) {
            rankingParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.rankingParameters;
    }

}