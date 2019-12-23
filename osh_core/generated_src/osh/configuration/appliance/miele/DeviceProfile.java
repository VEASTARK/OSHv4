//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2019.12.23 at 08:52:10 PM CET 
//


package osh.configuration.appliance.miele;

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
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deviceUUID" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deviceType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="deviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="hasProfile" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="Intelligent" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="profileTicks" type="{http://osh/configuration/appliance/miele}ProfileTicks"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "deviceUUID",
    "deviceType",
    "deviceDescription",
    "hasProfile",
    "intelligent",
    "profileTicks"
})
@XmlRootElement(name = "DeviceProfile")
public class DeviceProfile {

    @XmlElement(required = true)
    protected String deviceUUID;
    @XmlElement(required = true)
    protected String deviceType;
    @XmlElement(required = true)
    protected String deviceDescription;
    protected boolean hasProfile;
    @XmlElement(name = "Intelligent")
    protected boolean intelligent;
    @XmlElement(required = true)
    protected ProfileTicks profileTicks;

    /**
     * Gets the value of the deviceUUID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceUUID() {
        return deviceUUID;
    }

    /**
     * Sets the value of the deviceUUID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceUUID(String value) {
        this.deviceUUID = value;
    }

    /**
     * Gets the value of the deviceType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the value of the deviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceType(String value) {
        this.deviceType = value;
    }

    /**
     * Gets the value of the deviceDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceDescription() {
        return deviceDescription;
    }

    /**
     * Sets the value of the deviceDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceDescription(String value) {
        this.deviceDescription = value;
    }

    /**
     * Gets the value of the hasProfile property.
     * 
     */
    public boolean isHasProfile() {
        return hasProfile;
    }

    /**
     * Sets the value of the hasProfile property.
     * 
     */
    public void setHasProfile(boolean value) {
        this.hasProfile = value;
    }

    /**
     * Gets the value of the intelligent property.
     * 
     */
    public boolean isIntelligent() {
        return intelligent;
    }

    /**
     * Sets the value of the intelligent property.
     * 
     */
    public void setIntelligent(boolean value) {
        this.intelligent = value;
    }

    /**
     * Gets the value of the profileTicks property.
     * 
     * @return
     *     possible object is
     *     {@link ProfileTicks }
     *     
     */
    public ProfileTicks getProfileTicks() {
        return profileTicks;
    }

    /**
     * Sets the value of the profileTicks property.
     * 
     * @param value
     *     allowed object is
     *     {@link ProfileTicks }
     *     
     */
    public void setProfileTicks(ProfileTicks value) {
        this.profileTicks = value;
    }

}
