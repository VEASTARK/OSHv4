//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.16 at 05:56:33 PM CEST 
//


package osh.configuration.cal;

import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import osh.configuration.system.ComDeviceClassification;
import osh.configuration.system.ComDeviceTypes;
import osh.configuration.system.ConfigurationParameter;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * <p>Java class for AssignedComDevice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AssignedComDevice"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="comDeviceID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comDeviceType" type="{http://osh/configuration/system}ComDeviceTypes"/&gt;
 *         &lt;element name="comDeviceClassification" type="{http://osh/configuration/system}ComDeviceClassification"/&gt;
 *         &lt;element name="comDeviceDescription" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comManagerClassName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comDriverClassName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comDriverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
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
public class AssignedComDevice implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    protected String comDeviceID;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ComDeviceTypes comDeviceType;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected ComDeviceClassification comDeviceClassification;
    @XmlElement(required = true)
    protected String comDeviceDescription;
    @XmlElement(required = true)
    protected String comManagerClassName;
    @XmlElement(required = true)
    protected String comDriverClassName;
    protected List<ConfigurationParameter> comDriverParameters;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public AssignedComDevice() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a AssignedComDevice copying the state of another AssignedComDevice
     * 
     * @param _other
     *     The original AssignedComDevice from which to copy state.
     */
    public AssignedComDevice(final AssignedComDevice _other) {
        this.comDeviceID = _other.comDeviceID;
        this.comDeviceType = _other.comDeviceType;
        this.comDeviceClassification = _other.comDeviceClassification;
        this.comDeviceDescription = _other.comDeviceDescription;
        this.comManagerClassName = _other.comManagerClassName;
        this.comDriverClassName = _other.comDriverClassName;
        if (_other.comDriverParameters == null) {
            this.comDriverParameters = null;
        } else {
            this.comDriverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: _other.comDriverParameters) {
                this.comDriverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
    }

    /**
     * Instantiates a AssignedComDevice copying the state of another AssignedComDevice
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original AssignedComDevice from which to copy state.
     */
    public AssignedComDevice(final AssignedComDevice _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree comDeviceIDPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceID"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceIDPropertyTree!= null):((comDeviceIDPropertyTree == null)||(!comDeviceIDPropertyTree.isLeaf())))) {
            this.comDeviceID = _other.comDeviceID;
        }
        final PropertyTree comDeviceTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceTypePropertyTree!= null):((comDeviceTypePropertyTree == null)||(!comDeviceTypePropertyTree.isLeaf())))) {
            this.comDeviceType = _other.comDeviceType;
        }
        final PropertyTree comDeviceClassificationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceClassification"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceClassificationPropertyTree!= null):((comDeviceClassificationPropertyTree == null)||(!comDeviceClassificationPropertyTree.isLeaf())))) {
            this.comDeviceClassification = _other.comDeviceClassification;
        }
        final PropertyTree comDeviceDescriptionPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceDescription"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceDescriptionPropertyTree!= null):((comDeviceDescriptionPropertyTree == null)||(!comDeviceDescriptionPropertyTree.isLeaf())))) {
            this.comDeviceDescription = _other.comDeviceDescription;
        }
        final PropertyTree comManagerClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comManagerClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comManagerClassNamePropertyTree!= null):((comManagerClassNamePropertyTree == null)||(!comManagerClassNamePropertyTree.isLeaf())))) {
            this.comManagerClassName = _other.comManagerClassName;
        }
        final PropertyTree comDriverClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDriverClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDriverClassNamePropertyTree!= null):((comDriverClassNamePropertyTree == null)||(!comDriverClassNamePropertyTree.isLeaf())))) {
            this.comDriverClassName = _other.comDriverClassName;
        }
        final PropertyTree comDriverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDriverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDriverParametersPropertyTree!= null):((comDriverParametersPropertyTree == null)||(!comDriverParametersPropertyTree.isLeaf())))) {
            if (_other.comDriverParameters == null) {
                this.comDriverParameters = null;
            } else {
                this.comDriverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: _other.comDriverParameters) {
                    this.comDriverParameters.add(((_item == null)?null:_item.createCopy(comDriverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
    }

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

    @Override
    public AssignedComDevice createCopy() {
        final AssignedComDevice _newObject;
        try {
            _newObject = ((AssignedComDevice) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.comDeviceID = this.comDeviceID;
        _newObject.comDeviceType = this.comDeviceType;
        _newObject.comDeviceClassification = this.comDeviceClassification;
        _newObject.comDeviceDescription = this.comDeviceDescription;
        _newObject.comManagerClassName = this.comManagerClassName;
        _newObject.comDriverClassName = this.comDriverClassName;
        if (this.comDriverParameters == null) {
            _newObject.comDriverParameters = null;
        } else {
            _newObject.comDriverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: this.comDriverParameters) {
                _newObject.comDriverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        return _newObject;
    }

    @Override
    public AssignedComDevice createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final AssignedComDevice _newObject;
        try {
            _newObject = ((AssignedComDevice) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree comDeviceIDPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceID"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceIDPropertyTree!= null):((comDeviceIDPropertyTree == null)||(!comDeviceIDPropertyTree.isLeaf())))) {
            _newObject.comDeviceID = this.comDeviceID;
        }
        final PropertyTree comDeviceTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceTypePropertyTree!= null):((comDeviceTypePropertyTree == null)||(!comDeviceTypePropertyTree.isLeaf())))) {
            _newObject.comDeviceType = this.comDeviceType;
        }
        final PropertyTree comDeviceClassificationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceClassification"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceClassificationPropertyTree!= null):((comDeviceClassificationPropertyTree == null)||(!comDeviceClassificationPropertyTree.isLeaf())))) {
            _newObject.comDeviceClassification = this.comDeviceClassification;
        }
        final PropertyTree comDeviceDescriptionPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDeviceDescription"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDeviceDescriptionPropertyTree!= null):((comDeviceDescriptionPropertyTree == null)||(!comDeviceDescriptionPropertyTree.isLeaf())))) {
            _newObject.comDeviceDescription = this.comDeviceDescription;
        }
        final PropertyTree comManagerClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comManagerClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comManagerClassNamePropertyTree!= null):((comManagerClassNamePropertyTree == null)||(!comManagerClassNamePropertyTree.isLeaf())))) {
            _newObject.comManagerClassName = this.comManagerClassName;
        }
        final PropertyTree comDriverClassNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDriverClassName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDriverClassNamePropertyTree!= null):((comDriverClassNamePropertyTree == null)||(!comDriverClassNamePropertyTree.isLeaf())))) {
            _newObject.comDriverClassName = this.comDriverClassName;
        }
        final PropertyTree comDriverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("comDriverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(comDriverParametersPropertyTree!= null):((comDriverParametersPropertyTree == null)||(!comDriverParametersPropertyTree.isLeaf())))) {
            if (this.comDriverParameters == null) {
                _newObject.comDriverParameters = null;
            } else {
                _newObject.comDriverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: this.comDriverParameters) {
                    _newObject.comDriverParameters.add(((_item == null)?null:_item.createCopy(comDriverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        return _newObject;
    }

    @Override
    public AssignedComDevice copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public AssignedComDevice copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends AssignedComDevice.Selector<AssignedComDevice.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static AssignedComDevice.Select _root() {
            return new AssignedComDevice.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceID = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceType = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceClassification = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceDescription = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comManagerClassName = null;
        private com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDriverClassName = null;
        private ConfigurationParameter.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDriverParameters = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.comDeviceID!= null) {
                products.put("comDeviceID", this.comDeviceID.init());
            }
            if (this.comDeviceType!= null) {
                products.put("comDeviceType", this.comDeviceType.init());
            }
            if (this.comDeviceClassification!= null) {
                products.put("comDeviceClassification", this.comDeviceClassification.init());
            }
            if (this.comDeviceDescription!= null) {
                products.put("comDeviceDescription", this.comDeviceDescription.init());
            }
            if (this.comManagerClassName!= null) {
                products.put("comManagerClassName", this.comManagerClassName.init());
            }
            if (this.comDriverClassName!= null) {
                products.put("comDriverClassName", this.comDriverClassName.init());
            }
            if (this.comDriverParameters!= null) {
                products.put("comDriverParameters", this.comDriverParameters.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceID() {
            return ((this.comDeviceID == null)?this.comDeviceID = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDeviceID"):this.comDeviceID);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceType() {
            return ((this.comDeviceType == null)?this.comDeviceType = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDeviceType"):this.comDeviceType);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceClassification() {
            return ((this.comDeviceClassification == null)?this.comDeviceClassification = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDeviceClassification"):this.comDeviceClassification);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDeviceDescription() {
            return ((this.comDeviceDescription == null)?this.comDeviceDescription = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDeviceDescription"):this.comDeviceDescription);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comManagerClassName() {
            return ((this.comManagerClassName == null)?this.comManagerClassName = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comManagerClassName"):this.comManagerClassName);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDriverClassName() {
            return ((this.comDriverClassName == null)?this.comDriverClassName = new com.kscs.util.jaxb.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDriverClassName"):this.comDriverClassName);
        }

        public ConfigurationParameter.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>> comDriverParameters() {
            return ((this.comDriverParameters == null)?this.comDriverParameters = new ConfigurationParameter.Selector<TRoot, AssignedComDevice.Selector<TRoot, TParent>>(this._root, this, "comDriverParameters"):this.comDriverParameters);
        }

    }

}
