//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.16 at 05:56:34 PM CEST 
//


package osh.configuration.system;

import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.HashMap;
import java.util.Map;


/**
 * <p>Java class for ConfigurationParameter complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ConfigurationParameter"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="parameterName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="parameterValue" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="parameterType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ConfigurationParameter", propOrder = {
    "parameterName",
    "parameterValue",
    "parameterType"
})
public class ConfigurationParameter implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    protected String parameterName;
    @XmlElement(required = true)
    protected String parameterValue;
    @XmlElement(required = true)
    protected String parameterType;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public ConfigurationParameter() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a ConfigurationParameter copying the state of another ConfigurationParameter
     * 
     * @param _other
     *     The original ConfigurationParameter from which to copy state.
     */
    public ConfigurationParameter(final ConfigurationParameter _other) {
        this.parameterName = _other.parameterName;
        this.parameterValue = _other.parameterValue;
        this.parameterType = _other.parameterType;
    }

    /**
     * Instantiates a ConfigurationParameter copying the state of another ConfigurationParameter
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original ConfigurationParameter from which to copy state.
     */
    public ConfigurationParameter(final ConfigurationParameter _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree parameterNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterNamePropertyTree!= null):((parameterNamePropertyTree == null)||(!parameterNamePropertyTree.isLeaf())))) {
            this.parameterName = _other.parameterName;
        }
        final PropertyTree parameterValuePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterValue"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterValuePropertyTree!= null):((parameterValuePropertyTree == null)||(!parameterValuePropertyTree.isLeaf())))) {
            this.parameterValue = _other.parameterValue;
        }
        final PropertyTree parameterTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterTypePropertyTree!= null):((parameterTypePropertyTree == null)||(!parameterTypePropertyTree.isLeaf())))) {
            this.parameterType = _other.parameterType;
        }
    }

    /**
     * Gets the value of the parameterName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterName() {
        return parameterName;
    }

    /**
     * Sets the value of the parameterName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterName(String value) {
        this.parameterName = value;
    }

    /**
     * Gets the value of the parameterValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterValue() {
        return parameterValue;
    }

    /**
     * Sets the value of the parameterValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterValue(String value) {
        this.parameterValue = value;
    }

    /**
     * Gets the value of the parameterType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getParameterType() {
        return parameterType;
    }

    /**
     * Sets the value of the parameterType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setParameterType(String value) {
        this.parameterType = value;
    }

    @Override
    public ConfigurationParameter createCopy() {
        final ConfigurationParameter _newObject;
        try {
            _newObject = ((ConfigurationParameter) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.parameterName = this.parameterName;
        _newObject.parameterValue = this.parameterValue;
        _newObject.parameterType = this.parameterType;
        return _newObject;
    }

    @Override
    public ConfigurationParameter createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final ConfigurationParameter _newObject;
        try {
            _newObject = ((ConfigurationParameter) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree parameterNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterName"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterNamePropertyTree!= null):((parameterNamePropertyTree == null)||(!parameterNamePropertyTree.isLeaf())))) {
            _newObject.parameterName = this.parameterName;
        }
        final PropertyTree parameterValuePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterValue"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterValuePropertyTree!= null):((parameterValuePropertyTree == null)||(!parameterValuePropertyTree.isLeaf())))) {
            _newObject.parameterValue = this.parameterValue;
        }
        final PropertyTree parameterTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("parameterType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(parameterTypePropertyTree!= null):((parameterTypePropertyTree == null)||(!parameterTypePropertyTree.isLeaf())))) {
            _newObject.parameterType = this.parameterType;
        }
        return _newObject;
    }

    @Override
    public ConfigurationParameter copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public ConfigurationParameter copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends ConfigurationParameter.Selector<ConfigurationParameter.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static ConfigurationParameter.Select _root() {
            return new ConfigurationParameter.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterName = null;
        private com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterValue = null;
        private com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterType = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.parameterName!= null) {
                products.put("parameterName", this.parameterName.init());
            }
            if (this.parameterValue!= null) {
                products.put("parameterValue", this.parameterValue.init());
            }
            if (this.parameterType!= null) {
                products.put("parameterType", this.parameterType.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterName() {
            return ((this.parameterName == null)?this.parameterName = new com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>>(this._root, this, "parameterName"):this.parameterName);
        }

        public com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterValue() {
            return ((this.parameterValue == null)?this.parameterValue = new com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>>(this._root, this, "parameterValue"):this.parameterValue);
        }

        public com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>> parameterType() {
            return ((this.parameterType == null)?this.parameterType = new com.kscs.util.jaxb.Selector<TRoot, ConfigurationParameter.Selector<TRoot, TParent>>(this._root, this, "parameterType"):this.parameterType);
        }

    }

}
