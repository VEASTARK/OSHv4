//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.12 at 10:41:12 PM CEST 
//


package osh.configuration.oc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;
import osh.configuration.system.ConfigurationParameter;


/**
 * <p>Java class for QualityIndicatorConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="QualityIndicatorConfiguration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="type" type="{http://osh/configuration/oc}qualityIndicator"/&gt;
 *         &lt;element name="indicatorParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QualityIndicatorConfiguration", propOrder = {
    "type",
    "indicatorParameters"
})
public class QualityIndicatorConfiguration implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected QualityIndicator type;
    protected List<ConfigurationParameter> indicatorParameters;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public QualityIndicatorConfiguration() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a QualityIndicatorConfiguration copying the state of another QualityIndicatorConfiguration
     * 
     * @param _other
     *     The original QualityIndicatorConfiguration from which to copy state.
     */
    public QualityIndicatorConfiguration(final QualityIndicatorConfiguration _other) {
        this.type = _other.type;
        if (_other.indicatorParameters == null) {
            this.indicatorParameters = null;
        } else {
            this.indicatorParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: _other.indicatorParameters) {
                this.indicatorParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
    }

    /**
     * Instantiates a QualityIndicatorConfiguration copying the state of another QualityIndicatorConfiguration
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original QualityIndicatorConfiguration from which to copy state.
     */
    public QualityIndicatorConfiguration(final QualityIndicatorConfiguration _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree typePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("type"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(typePropertyTree!= null):((typePropertyTree == null)||(!typePropertyTree.isLeaf())))) {
            this.type = _other.type;
        }
        final PropertyTree indicatorParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("indicatorParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(indicatorParametersPropertyTree!= null):((indicatorParametersPropertyTree == null)||(!indicatorParametersPropertyTree.isLeaf())))) {
            if (_other.indicatorParameters == null) {
                this.indicatorParameters = null;
            } else {
                this.indicatorParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: _other.indicatorParameters) {
                    this.indicatorParameters.add(((_item == null)?null:_item.createCopy(indicatorParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
    }

    /**
     * Gets the value of the type property.
     * 
     * @return
     *     possible object is
     *     {@link QualityIndicator }
     *     
     */
    public QualityIndicator getType() {
        return type;
    }

    /**
     * Sets the value of the type property.
     * 
     * @param value
     *     allowed object is
     *     {@link QualityIndicator }
     *     
     */
    public void setType(QualityIndicator value) {
        this.type = value;
    }

    /**
     * Gets the value of the indicatorParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the indicatorParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIndicatorParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getIndicatorParameters() {
        if (indicatorParameters == null) {
            indicatorParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.indicatorParameters;
    }

    @Override
    public QualityIndicatorConfiguration createCopy() {
        final QualityIndicatorConfiguration _newObject;
        try {
            _newObject = ((QualityIndicatorConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.type = this.type;
        if (this.indicatorParameters == null) {
            _newObject.indicatorParameters = null;
        } else {
            _newObject.indicatorParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: this.indicatorParameters) {
                _newObject.indicatorParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        return _newObject;
    }

    @Override
    public QualityIndicatorConfiguration createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final QualityIndicatorConfiguration _newObject;
        try {
            _newObject = ((QualityIndicatorConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree typePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("type"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(typePropertyTree!= null):((typePropertyTree == null)||(!typePropertyTree.isLeaf())))) {
            _newObject.type = this.type;
        }
        final PropertyTree indicatorParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("indicatorParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(indicatorParametersPropertyTree!= null):((indicatorParametersPropertyTree == null)||(!indicatorParametersPropertyTree.isLeaf())))) {
            if (this.indicatorParameters == null) {
                _newObject.indicatorParameters = null;
            } else {
                _newObject.indicatorParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: this.indicatorParameters) {
                    _newObject.indicatorParameters.add(((_item == null)?null:_item.createCopy(indicatorParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        return _newObject;
    }

    @Override
    public QualityIndicatorConfiguration copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public QualityIndicatorConfiguration copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends QualityIndicatorConfiguration.Selector<QualityIndicatorConfiguration.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static QualityIndicatorConfiguration.Select _root() {
            return new QualityIndicatorConfiguration.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>> type = null;
        private ConfigurationParameter.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>> indicatorParameters = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.type!= null) {
                products.put("type", this.type.init());
            }
            if (this.indicatorParameters!= null) {
                products.put("indicatorParameters", this.indicatorParameters.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>> type() {
            return ((this.type == null)?this.type = new com.kscs.util.jaxb.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>>(this._root, this, "type"):this.type);
        }

        public ConfigurationParameter.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>> indicatorParameters() {
            return ((this.indicatorParameters == null)?this.indicatorParameters = new ConfigurationParameter.Selector<TRoot, QualityIndicatorConfiguration.Selector<TRoot, TParent>>(this._root, this, "indicatorParameters"):this.indicatorParameters);
        }

    }

}
