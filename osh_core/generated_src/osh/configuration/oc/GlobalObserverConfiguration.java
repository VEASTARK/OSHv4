//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.10 at 03:54:46 PM CEST 
//


package osh.configuration.oc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import osh.configuration.system.ConfigurationParameter;


/**
 * <p>Java class for GlobalObserverConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GlobalObserverConfiguration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="className" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="globalObserverParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalObserverConfiguration", propOrder = {
    "className",
    "globalObserverParameters"
})
public class GlobalObserverConfiguration implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    protected String className;
    protected List<ConfigurationParameter> globalObserverParameters;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public GlobalObserverConfiguration() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a GlobalObserverConfiguration copying the state of another GlobalObserverConfiguration
     * 
     * @param _other
     *     The original GlobalObserverConfiguration from which to copy state.
     */
    public GlobalObserverConfiguration(final GlobalObserverConfiguration _other) {
        this.className = _other.className;
        if (_other.globalObserverParameters == null) {
            this.globalObserverParameters = null;
        } else {
            this.globalObserverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: _other.globalObserverParameters) {
                this.globalObserverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
    }

    /**
     * Instantiates a GlobalObserverConfiguration copying the state of another GlobalObserverConfiguration
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original GlobalObserverConfiguration from which to copy state.
     */
    public GlobalObserverConfiguration(final GlobalObserverConfiguration _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree classNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("className"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(classNamePropertyTree!= null):((classNamePropertyTree == null)||(!classNamePropertyTree.isLeaf())))) {
            this.className = _other.className;
        }
        final PropertyTree globalObserverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("globalObserverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(globalObserverParametersPropertyTree!= null):((globalObserverParametersPropertyTree == null)||(!globalObserverParametersPropertyTree.isLeaf())))) {
            if (_other.globalObserverParameters == null) {
                this.globalObserverParameters = null;
            } else {
                this.globalObserverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: _other.globalObserverParameters) {
                    this.globalObserverParameters.add(((_item == null)?null:_item.createCopy(globalObserverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
    }

    /**
     * Gets the value of the className property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getClassName() {
        return className;
    }

    /**
     * Sets the value of the className property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setClassName(String value) {
        this.className = value;
    }

    /**
     * Gets the value of the globalObserverParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the globalObserverParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getGlobalObserverParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getGlobalObserverParameters() {
        if (globalObserverParameters == null) {
            globalObserverParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.globalObserverParameters;
    }

    @Override
    public GlobalObserverConfiguration createCopy() {
        final GlobalObserverConfiguration _newObject;
        try {
            _newObject = ((GlobalObserverConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.className = this.className;
        if (this.globalObserverParameters == null) {
            _newObject.globalObserverParameters = null;
        } else {
            _newObject.globalObserverParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: this.globalObserverParameters) {
                _newObject.globalObserverParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        return _newObject;
    }

    @Override
    public GlobalObserverConfiguration createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final GlobalObserverConfiguration _newObject;
        try {
            _newObject = ((GlobalObserverConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree classNamePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("className"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(classNamePropertyTree!= null):((classNamePropertyTree == null)||(!classNamePropertyTree.isLeaf())))) {
            _newObject.className = this.className;
        }
        final PropertyTree globalObserverParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("globalObserverParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(globalObserverParametersPropertyTree!= null):((globalObserverParametersPropertyTree == null)||(!globalObserverParametersPropertyTree.isLeaf())))) {
            if (this.globalObserverParameters == null) {
                _newObject.globalObserverParameters = null;
            } else {
                _newObject.globalObserverParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: this.globalObserverParameters) {
                    _newObject.globalObserverParameters.add(((_item == null)?null:_item.createCopy(globalObserverParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        return _newObject;
    }

    @Override
    public GlobalObserverConfiguration copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public GlobalObserverConfiguration copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends GlobalObserverConfiguration.Selector<GlobalObserverConfiguration.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static GlobalObserverConfiguration.Select _root() {
            return new GlobalObserverConfiguration.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>> className = null;
        private ConfigurationParameter.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>> globalObserverParameters = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.className!= null) {
                products.put("className", this.className.init());
            }
            if (this.globalObserverParameters!= null) {
                products.put("globalObserverParameters", this.globalObserverParameters.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>> className() {
            return ((this.className == null)?this.className = new com.kscs.util.jaxb.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>>(this._root, this, "className"):this.className);
        }

        public ConfigurationParameter.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>> globalObserverParameters() {
            return ((this.globalObserverParameters == null)?this.globalObserverParameters = new ConfigurationParameter.Selector<TRoot, GlobalObserverConfiguration.Selector<TRoot, TParent>>(this._root, this, "globalObserverParameters"):this.globalObserverParameters);
        }

    }

}
