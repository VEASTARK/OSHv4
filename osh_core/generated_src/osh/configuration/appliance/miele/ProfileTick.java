//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.03 at 04:51:19 PM CEST 
//


package osh.configuration.appliance.miele;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for ProfileTick complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ProfileTick"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="load" maxOccurs="unbounded"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="commodity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *                   &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="deviceStateName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="parameters" maxOccurs="unbounded" minOccurs="0"&gt;
 *           &lt;complexType&gt;
 *             &lt;complexContent&gt;
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *                 &lt;sequence&gt;
 *                   &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *                   &lt;element name="parameterValue" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
 *                 &lt;/sequence&gt;
 *               &lt;/restriction&gt;
 *             &lt;/complexContent&gt;
 *           &lt;/complexType&gt;
 *         &lt;/element&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProfileTick", propOrder = {
    "load",
    "deviceStateName",
    "parameters"
})
public class ProfileTick {

    @XmlElement(required = true)
    protected List<ProfileTick.Load> load;
    @XmlElement(required = true)
    protected String deviceStateName;
    protected List<ProfileTick.Parameters> parameters;

    /**
     * Gets the value of the load property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the load property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getLoad().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfileTick.Load }
     * 
     * 
     */
    public List<ProfileTick.Load> getLoad() {
        if (load == null) {
            load = new ArrayList<ProfileTick.Load>();
        }
        return this.load;
    }

    /**
     * Gets the value of the deviceStateName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceStateName() {
        return deviceStateName;
    }

    /**
     * Sets the value of the deviceStateName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceStateName(String value) {
        this.deviceStateName = value;
    }

    /**
     * Gets the value of the parameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the parameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ProfileTick.Parameters }
     * 
     * 
     */
    public List<ProfileTick.Parameters> getParameters() {
        if (parameters == null) {
            parameters = new ArrayList<ProfileTick.Parameters>();
        }
        return this.parameters;
    }


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
     *         &lt;element name="commodity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
     *         &lt;element name="value" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
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
        "commodity",
        "value"
    })
    public static class Load {

        @XmlElement(required = true)
        protected String commodity;
        protected int value;

        /**
         * Gets the value of the commodity property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getCommodity() {
            return commodity;
        }

        /**
         * Sets the value of the commodity property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setCommodity(String value) {
            this.commodity = value;
        }

        /**
         * Gets the value of the value property.
         * 
         */
        public int getValue() {
            return value;
        }

        /**
         * Sets the value of the value property.
         * 
         */
        public void setValue(int value) {
            this.value = value;
        }

    }


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
     *         &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
     *         &lt;element name="parameterValue" type="{http://www.w3.org/2001/XMLSchema}anyType"/&gt;
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
        "parameterName",
        "parameterValue"
    })
    public static class Parameters {

        @XmlElement(required = true)
        protected Object parameterName;
        @XmlElement(required = true)
        protected Object parameterValue;

        /**
         * Gets the value of the parameterName property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getParameterName() {
            return parameterName;
        }

        /**
         * Sets the value of the parameterName property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setParameterName(Object value) {
            this.parameterName = value;
        }

        /**
         * Gets the value of the parameterValue property.
         * 
         * @return
         *     possible object is
         *     {@link Object }
         *     
         */
        public Object getParameterValue() {
            return parameterValue;
        }

        /**
         * Sets the value of the parameterValue property.
         * 
         * @param value
         *     allowed object is
         *     {@link Object }
         *     
         */
        public void setParameterValue(Object value) {
            this.parameterValue = value;
        }

    }

}
