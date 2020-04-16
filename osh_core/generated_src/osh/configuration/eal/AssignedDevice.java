//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.16 at 05:56:33 PM CEST 
//


package osh.configuration.eal;

import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import osh.configuration.system.ConfigurationParameter;
import osh.configuration.system.DeviceClassification;
import osh.configuration.system.DeviceTypes;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for AssignedDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssignedDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="deviceType" type="{http://osh/configuration/system}DeviceTypes"/&gt;
 *         &lt;element name="deviceClassification" type="{http://osh/configuration/system}DeviceClassification"/&gt;
 *         &lt;element name="deviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="driverClassName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="driverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="controllable" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="observable" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *         &lt;element name="assignedLocalOCUnit" type="{http://osh/configuration/eal}AssignedLocalOCUnit"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="deviceID" type="{http://www.w3.org/2001/XMLSchema}string" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssignedDevice", propOrder = {
    "deviceType",
    "deviceClassification",
    "deviceDescription",
    "driverClassName",
    "driverParameters",
    "controllable",
    "observable",
    "assignedLocalOCUnit"
})
public class AssignedDevice implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DeviceTypes deviceType;
    @XmlElement(required = true, defaultValue = "N/A")
    @XmlSchemaType(name = "string")
    protected DeviceClassification deviceClassification;
    @XmlElement(required = true)
    protected String deviceDescription;
    @XmlElement(required = true)
    protected String driverClassName;
    protected List<ConfigurationParameter> driverParameters;
    protected boolean controllable;
    protected boolean observable;
    @XmlElement(required = true)
    protected AssignedLocalOCUnit assignedLocalOCUnit;
    @XmlAttribute(name = "deviceID")
    protected String deviceID;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public AssignedDevice() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a AssignedDevice copying the state of another AssignedDevice
     * 
     * @param _other
     *     The original AssignedDevice from which to copy state.
     */
    public AssignedDevice(final AssignedDevice _other) {
        this.deviceType = _other.deviceType;
        this.deviceClassification = _other.deviceClassification;
        this.deviceDescription = _other.deviceDescription;
        this.driverClassName = _other.driverClassName;
        if (_other.driverParameters == null) {
            this.driverParameters = null;
        } else {
            this.driverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: _other.driverParameters) {
                this.driverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        this.controllable = _other.controllable;
        this.observable = _other.observable;
        this.assignedLocalOCUnit = ((_other.assignedLocalOCUnit == null)?null:_other.assignedLocalOCUnit.createCopy());
        this.deviceID = _other.deviceID;
    }

    /**
     * Instantiates a AssignedDevice copying the state of another AssignedDevice
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original AssignedDevice from which to copy state.
     */
    public AssignedDevice(final AssignedDevice _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree deviceTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceTypePropertyTree!= null):((deviceTypePropertyTree == null)||(!deviceTypePropertyTree.isLeaf())))) {
            this.deviceType = _other.deviceType;
        }
        final PropertyTree deviceClassificationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceClassification"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceClassificationPropertyTree!= null):((deviceClassificationPropertyTree == null)||(!deviceClassificationPropertyTree.isLeaf())))) {
            this.deviceClassification = _other.deviceClassification;
        }
        final PropertyTree deviceDescriptionPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceDescription"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceDescriptionPropertyTree!= null):((deviceDescriptionPropertyTree == null)||(!deviceDescriptionPropertyTree.isLeaf())))) {
            this.deviceDescription = _other.deviceDescription;
        }
        final PropertyTree driverClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("driverClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(driverClassNamePropertyTree!= null):((driverClassNamePropertyTree == null)||(!driverClassNamePropertyTree.isLeaf())))) {
            this.driverClassName = _other.driverClassName;
        }
        final PropertyTree driverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("driverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(driverParametersPropertyTree!= null):((driverParametersPropertyTree == null)||(!driverParametersPropertyTree.isLeaf())))) {
            if (_other.driverParameters == null) {
                this.driverParameters = null;
            } else {
                this.driverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: _other.driverParameters) {
                    this.driverParameters.add(((_item == null)?null:_item.createCopy(driverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree controllablePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("controllable"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(controllablePropertyTree!= null):((controllablePropertyTree == null)||(!controllablePropertyTree.isLeaf())))) {
            this.controllable = _other.controllable;
        }
        final PropertyTree observablePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("observable"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(observablePropertyTree!= null):((observablePropertyTree == null)||(!observablePropertyTree.isLeaf())))) {
            this.observable = _other.observable;
        }
        final PropertyTree assignedLocalOCUnitPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("assignedLocalOCUnit"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(assignedLocalOCUnitPropertyTree!= null):((assignedLocalOCUnitPropertyTree == null)||(!assignedLocalOCUnitPropertyTree.isLeaf())))) {
            this.assignedLocalOCUnit = ((_other.assignedLocalOCUnit == null)?null:_other.assignedLocalOCUnit.createCopy(assignedLocalOCUnitPropertyTree, _propertyTreeUse));
        }
        final PropertyTree deviceIDPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceID"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceIDPropertyTree!= null):((deviceIDPropertyTree == null)||(!deviceIDPropertyTree.isLeaf())))) {
            this.deviceID = _other.deviceID;
        }
    }

    /**
     * Gets the value of the deviceType property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceTypes }
     *     
     */
    public DeviceTypes getDeviceType() {
        return deviceType;
    }

    /**
     * Sets the value of the deviceType property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceTypes }
     *     
     */
    public void setDeviceType(DeviceTypes value) {
        this.deviceType = value;
    }

    /**
     * Gets the value of the deviceClassification property.
     * 
     * @return
     *     possible object is
     *     {@link DeviceClassification }
     *     
     */
    public DeviceClassification getDeviceClassification() {
        return deviceClassification;
    }

    /**
     * Sets the value of the deviceClassification property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeviceClassification }
     *     
     */
    public void setDeviceClassification(DeviceClassification value) {
        this.deviceClassification = value;
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
     * Gets the value of the driverClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDriverClassName() {
        return driverClassName;
    }

    /**
     * Sets the value of the driverClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDriverClassName(String value) {
        this.driverClassName = value;
    }

    /**
     * Gets the value of the driverParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the driverParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDriverParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getDriverParameters() {
        if (driverParameters == null) {
            driverParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.driverParameters;
    }

    /**
     * Gets the value of the controllable property.
     * 
     */
    public boolean isControllable() {
        return controllable;
    }

    /**
     * Sets the value of the controllable property.
     * 
     */
    public void setControllable(boolean value) {
        this.controllable = value;
    }

    /**
     * Gets the value of the observable property.
     * 
     */
    public boolean isObservable() {
        return observable;
    }

    /**
     * Sets the value of the observable property.
     * 
     */
    public void setObservable(boolean value) {
        this.observable = value;
    }

    /**
     * Gets the value of the assignedLocalOCUnit property.
     * 
     * @return
     *     possible object is
     *     {@link AssignedLocalOCUnit }
     *     
     */
    public AssignedLocalOCUnit getAssignedLocalOCUnit() {
        return assignedLocalOCUnit;
    }

    /**
     * Sets the value of the assignedLocalOCUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link AssignedLocalOCUnit }
     *     
     */
    public void setAssignedLocalOCUnit(AssignedLocalOCUnit value) {
        this.assignedLocalOCUnit = value;
    }

    /**
     * Gets the value of the deviceID property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets the value of the deviceID property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeviceID(String value) {
        this.deviceID = value;
    }

    @Override
    public AssignedDevice createCopy() {
        final AssignedDevice _newObject;
        try {
            _newObject = ((AssignedDevice) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.deviceType = this.deviceType;
        _newObject.deviceClassification = this.deviceClassification;
        _newObject.deviceDescription = this.deviceDescription;
        _newObject.driverClassName = this.driverClassName;
        if (this.driverParameters == null) {
            _newObject.driverParameters = null;
        } else {
            _newObject.driverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: this.driverParameters) {
                _newObject.driverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        _newObject.controllable = this.controllable;
        _newObject.observable = this.observable;
        _newObject.assignedLocalOCUnit = ((this.assignedLocalOCUnit == null)?null:this.assignedLocalOCUnit.createCopy());
        _newObject.deviceID = this.deviceID;
        return _newObject;
    }

    @Override
    public AssignedDevice createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final AssignedDevice _newObject;
        try {
            _newObject = ((AssignedDevice) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree deviceTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceTypePropertyTree!= null):((deviceTypePropertyTree == null)||(!deviceTypePropertyTree.isLeaf())))) {
            _newObject.deviceType = this.deviceType;
        }
        final PropertyTree deviceClassificationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceClassification"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceClassificationPropertyTree!= null):((deviceClassificationPropertyTree == null)||(!deviceClassificationPropertyTree.isLeaf())))) {
            _newObject.deviceClassification = this.deviceClassification;
        }
        final PropertyTree deviceDescriptionPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceDescription"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceDescriptionPropertyTree!= null):((deviceDescriptionPropertyTree == null)||(!deviceDescriptionPropertyTree.isLeaf())))) {
            _newObject.deviceDescription = this.deviceDescription;
        }
        final PropertyTree driverClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("driverClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(driverClassNamePropertyTree!= null):((driverClassNamePropertyTree == null)||(!driverClassNamePropertyTree.isLeaf())))) {
            _newObject.driverClassName = this.driverClassName;
        }
        final PropertyTree driverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("driverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(driverParametersPropertyTree!= null):((driverParametersPropertyTree == null)||(!driverParametersPropertyTree.isLeaf())))) {
            if (this.driverParameters == null) {
                _newObject.driverParameters = null;
            } else {
                _newObject.driverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: this.driverParameters) {
                    _newObject.driverParameters.add(((_item == null)?null:_item.createCopy(driverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree controllablePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("controllable"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(controllablePropertyTree!= null):((controllablePropertyTree == null)||(!controllablePropertyTree.isLeaf())))) {
            _newObject.controllable = this.controllable;
        }
        final PropertyTree observablePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("observable"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(observablePropertyTree!= null):((observablePropertyTree == null)||(!observablePropertyTree.isLeaf())))) {
            _newObject.observable = this.observable;
        }
        final PropertyTree assignedLocalOCUnitPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("assignedLocalOCUnit"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(assignedLocalOCUnitPropertyTree!= null):((assignedLocalOCUnitPropertyTree == null)||(!assignedLocalOCUnitPropertyTree.isLeaf())))) {
            _newObject.assignedLocalOCUnit = ((this.assignedLocalOCUnit == null)?null:this.assignedLocalOCUnit.createCopy(assignedLocalOCUnitPropertyTree, _propertyTreeUse));
        }
        final PropertyTree deviceIDPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("deviceID"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(deviceIDPropertyTree!= null):((deviceIDPropertyTree == null)||(!deviceIDPropertyTree.isLeaf())))) {
            _newObject.deviceID = this.deviceID;
        }
        return _newObject;
    }

    @Override
    public AssignedDevice copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public AssignedDevice copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends AssignedDevice.Selector<AssignedDevice.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static AssignedDevice.Select _root() {
            return new AssignedDevice.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceType = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceClassification = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceDescription = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> driverClassName = null;
        private ConfigurationParameter.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> driverParameters = null;
        private AssignedLocalOCUnit.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> assignedLocalOCUnit = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceID = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.deviceType!= null) {
                products.put("deviceType", this.deviceType.init());
            }
            if (this.deviceClassification!= null) {
                products.put("deviceClassification", this.deviceClassification.init());
            }
            if (this.deviceDescription!= null) {
                products.put("deviceDescription", this.deviceDescription.init());
            }
            if (this.driverClassName!= null) {
                products.put("driverClassName", this.driverClassName.init());
            }
            if (this.driverParameters!= null) {
                products.put("driverParameters", this.driverParameters.init());
            }
            if (this.assignedLocalOCUnit!= null) {
                products.put("assignedLocalOCUnit", this.assignedLocalOCUnit.init());
            }
            if (this.deviceID!= null) {
                products.put("deviceID", this.deviceID.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceType() {
            return ((this.deviceType == null)?this.deviceType = new com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "deviceType"):this.deviceType);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceClassification() {
            return ((this.deviceClassification == null)?this.deviceClassification = new com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "deviceClassification"):this.deviceClassification);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceDescription() {
            return ((this.deviceDescription == null)?this.deviceDescription = new com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "deviceDescription"):this.deviceDescription);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> driverClassName() {
            return ((this.driverClassName == null)?this.driverClassName = new com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "driverClassName"):this.driverClassName);
        }

        public ConfigurationParameter.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> driverParameters() {
            return ((this.driverParameters == null)?this.driverParameters = new ConfigurationParameter.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "driverParameters"):this.driverParameters);
        }

        public AssignedLocalOCUnit.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> assignedLocalOCUnit() {
            return ((this.assignedLocalOCUnit == null)?this.assignedLocalOCUnit = new AssignedLocalOCUnit.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "assignedLocalOCUnit"):this.assignedLocalOCUnit);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>> deviceID() {
            return ((this.deviceID == null)?this.deviceID = new com.kscs.util.jaxb.Selector<TRoot, AssignedDevice.Selector<TRoot, TParent>>(this._root, this, "deviceID"):this.deviceID);
        }

    }

}
