//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.10 at 05:32:57 PM CEST 
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
 * <p>Java class for AlgorithmConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AlgorithmConfiguration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="algorithm" type="{http://osh/configuration/oc}algorithmType"/&gt;
 *         &lt;element name="variableEncoding" type="{http://osh/configuration/oc}variableEncoding"/&gt;
 *         &lt;element name="stoppingRules" type="{http://osh/configuration/oc}StoppingRuleConfiguration" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="operators" type="{http://osh/configuration/oc}OperatorConfiguration" maxOccurs="unbounded" minOccurs="0"/&gt;
 *         &lt;element name="algorithmParameters" type="{http://osh/configuration/system}ConfigurationParameter" maxOccurs="unbounded" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AlgorithmConfiguration", propOrder = {
    "algorithm",
    "variableEncoding",
    "stoppingRules",
    "operators",
    "algorithmParameters"
})
public class AlgorithmConfiguration implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected AlgorithmType algorithm;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected VariableEncoding variableEncoding;
    protected List<StoppingRuleConfiguration> stoppingRules;
    protected List<OperatorConfiguration> operators;
    protected List<ConfigurationParameter> algorithmParameters;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public AlgorithmConfiguration() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a AlgorithmConfiguration copying the state of another AlgorithmConfiguration
     * 
     * @param _other
     *     The original AlgorithmConfiguration from which to copy state.
     */
    public AlgorithmConfiguration(final AlgorithmConfiguration _other) {
        this.algorithm = _other.algorithm;
        this.variableEncoding = _other.variableEncoding;
        if (_other.stoppingRules == null) {
            this.stoppingRules = null;
        } else {
            this.stoppingRules = new ArrayList<StoppingRuleConfiguration>();
            for (StoppingRuleConfiguration _item: _other.stoppingRules) {
                this.stoppingRules.add(((_item == null)?null:_item.createCopy()));
            }
        }
        if (_other.operators == null) {
            this.operators = null;
        } else {
            this.operators = new ArrayList<OperatorConfiguration>();
            for (OperatorConfiguration _item: _other.operators) {
                this.operators.add(((_item == null)?null:_item.createCopy()));
            }
        }
        if (_other.algorithmParameters == null) {
            this.algorithmParameters = null;
        } else {
            this.algorithmParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: _other.algorithmParameters) {
                this.algorithmParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
    }

    /**
     * Instantiates a AlgorithmConfiguration copying the state of another AlgorithmConfiguration
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original AlgorithmConfiguration from which to copy state.
     */
    public AlgorithmConfiguration(final AlgorithmConfiguration _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree algorithmPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithm"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmPropertyTree!= null):((algorithmPropertyTree == null)||(!algorithmPropertyTree.isLeaf())))) {
            this.algorithm = _other.algorithm;
        }
        final PropertyTree variableEncodingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("variableEncoding"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(variableEncodingPropertyTree!= null):((variableEncodingPropertyTree == null)||(!variableEncodingPropertyTree.isLeaf())))) {
            this.variableEncoding = _other.variableEncoding;
        }
        final PropertyTree stoppingRulesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("stoppingRules"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(stoppingRulesPropertyTree!= null):((stoppingRulesPropertyTree == null)||(!stoppingRulesPropertyTree.isLeaf())))) {
            if (_other.stoppingRules == null) {
                this.stoppingRules = null;
            } else {
                this.stoppingRules = new ArrayList<StoppingRuleConfiguration>();
                for (StoppingRuleConfiguration _item: _other.stoppingRules) {
                    this.stoppingRules.add(((_item == null)?null:_item.createCopy(stoppingRulesPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree operatorsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("operators"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(operatorsPropertyTree!= null):((operatorsPropertyTree == null)||(!operatorsPropertyTree.isLeaf())))) {
            if (_other.operators == null) {
                this.operators = null;
            } else {
                this.operators = new ArrayList<OperatorConfiguration>();
                for (OperatorConfiguration _item: _other.operators) {
                    this.operators.add(((_item == null)?null:_item.createCopy(operatorsPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree algorithmParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithmParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmParametersPropertyTree!= null):((algorithmParametersPropertyTree == null)||(!algorithmParametersPropertyTree.isLeaf())))) {
            if (_other.algorithmParameters == null) {
                this.algorithmParameters = null;
            } else {
                this.algorithmParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: _other.algorithmParameters) {
                    this.algorithmParameters.add(((_item == null)?null:_item.createCopy(algorithmParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
    }

    /**
     * Gets the value of the algorithm property.
     * 
     * @return
     *     possible object is
     *     {@link AlgorithmType }
     *     
     */
    public AlgorithmType getAlgorithm() {
        return algorithm;
    }

    /**
     * Sets the value of the algorithm property.
     * 
     * @param value
     *     allowed object is
     *     {@link AlgorithmType }
     *     
     */
    public void setAlgorithm(AlgorithmType value) {
        this.algorithm = value;
    }

    /**
     * Gets the value of the variableEncoding property.
     * 
     * @return
     *     possible object is
     *     {@link VariableEncoding }
     *     
     */
    public VariableEncoding getVariableEncoding() {
        return variableEncoding;
    }

    /**
     * Sets the value of the variableEncoding property.
     * 
     * @param value
     *     allowed object is
     *     {@link VariableEncoding }
     *     
     */
    public void setVariableEncoding(VariableEncoding value) {
        this.variableEncoding = value;
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
     * {@link StoppingRuleConfiguration }
     * 
     * 
     */
    public List<StoppingRuleConfiguration> getStoppingRules() {
        if (stoppingRules == null) {
            stoppingRules = new ArrayList<StoppingRuleConfiguration>();
        }
        return this.stoppingRules;
    }

    /**
     * Gets the value of the operators property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the operators property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOperators().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OperatorConfiguration }
     * 
     * 
     */
    public List<OperatorConfiguration> getOperators() {
        if (operators == null) {
            operators = new ArrayList<OperatorConfiguration>();
        }
        return this.operators;
    }

    /**
     * Gets the value of the algorithmParameters property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the algorithmParameters property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlgorithmParameters().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link ConfigurationParameter }
     * 
     * 
     */
    public List<ConfigurationParameter> getAlgorithmParameters() {
        if (algorithmParameters == null) {
            algorithmParameters = new ArrayList<ConfigurationParameter>();
        }
        return this.algorithmParameters;
    }

    @Override
    public AlgorithmConfiguration createCopy() {
        final AlgorithmConfiguration _newObject;
        try {
            _newObject = ((AlgorithmConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.algorithm = this.algorithm;
        _newObject.variableEncoding = this.variableEncoding;
        if (this.stoppingRules == null) {
            _newObject.stoppingRules = null;
        } else {
            _newObject.stoppingRules = new ArrayList<StoppingRuleConfiguration>();
            for (StoppingRuleConfiguration _item: this.stoppingRules) {
                _newObject.stoppingRules.add(((_item == null)?null:_item.createCopy()));
            }
        }
        if (this.operators == null) {
            _newObject.operators = null;
        } else {
            _newObject.operators = new ArrayList<OperatorConfiguration>();
            for (OperatorConfiguration _item: this.operators) {
                _newObject.operators.add(((_item == null)?null:_item.createCopy()));
            }
        }
        if (this.algorithmParameters == null) {
            _newObject.algorithmParameters = null;
        } else {
            _newObject.algorithmParameters = new ArrayList<ConfigurationParameter>();
            for (ConfigurationParameter _item: this.algorithmParameters) {
                _newObject.algorithmParameters.add(((_item == null)?null:_item.createCopy()));
            }
        }
        return _newObject;
    }

    @Override
    public AlgorithmConfiguration createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final AlgorithmConfiguration _newObject;
        try {
            _newObject = ((AlgorithmConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree algorithmPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithm"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmPropertyTree!= null):((algorithmPropertyTree == null)||(!algorithmPropertyTree.isLeaf())))) {
            _newObject.algorithm = this.algorithm;
        }
        final PropertyTree variableEncodingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("variableEncoding"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(variableEncodingPropertyTree!= null):((variableEncodingPropertyTree == null)||(!variableEncodingPropertyTree.isLeaf())))) {
            _newObject.variableEncoding = this.variableEncoding;
        }
        final PropertyTree stoppingRulesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("stoppingRules"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(stoppingRulesPropertyTree!= null):((stoppingRulesPropertyTree == null)||(!stoppingRulesPropertyTree.isLeaf())))) {
            if (this.stoppingRules == null) {
                _newObject.stoppingRules = null;
            } else {
                _newObject.stoppingRules = new ArrayList<StoppingRuleConfiguration>();
                for (StoppingRuleConfiguration _item: this.stoppingRules) {
                    _newObject.stoppingRules.add(((_item == null)?null:_item.createCopy(stoppingRulesPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree operatorsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("operators"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(operatorsPropertyTree!= null):((operatorsPropertyTree == null)||(!operatorsPropertyTree.isLeaf())))) {
            if (this.operators == null) {
                _newObject.operators = null;
            } else {
                _newObject.operators = new ArrayList<OperatorConfiguration>();
                for (OperatorConfiguration _item: this.operators) {
                    _newObject.operators.add(((_item == null)?null:_item.createCopy(operatorsPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree algorithmParametersPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithmParameters"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmParametersPropertyTree!= null):((algorithmParametersPropertyTree == null)||(!algorithmParametersPropertyTree.isLeaf())))) {
            if (this.algorithmParameters == null) {
                _newObject.algorithmParameters = null;
            } else {
                _newObject.algorithmParameters = new ArrayList<ConfigurationParameter>();
                for (ConfigurationParameter _item: this.algorithmParameters) {
                    _newObject.algorithmParameters.add(((_item == null)?null:_item.createCopy(algorithmParametersPropertyTree, _propertyTreeUse)));
                }
            }
        }
        return _newObject;
    }

    @Override
    public AlgorithmConfiguration copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public AlgorithmConfiguration copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends AlgorithmConfiguration.Selector<AlgorithmConfiguration.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static AlgorithmConfiguration.Select _root() {
            return new AlgorithmConfiguration.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> algorithm = null;
        private com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> variableEncoding = null;
        private StoppingRuleConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> stoppingRules = null;
        private OperatorConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> operators = null;
        private ConfigurationParameter.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> algorithmParameters = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.algorithm!= null) {
                products.put("algorithm", this.algorithm.init());
            }
            if (this.variableEncoding!= null) {
                products.put("variableEncoding", this.variableEncoding.init());
            }
            if (this.stoppingRules!= null) {
                products.put("stoppingRules", this.stoppingRules.init());
            }
            if (this.operators!= null) {
                products.put("operators", this.operators.init());
            }
            if (this.algorithmParameters!= null) {
                products.put("algorithmParameters", this.algorithmParameters.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> algorithm() {
            return ((this.algorithm == null)?this.algorithm = new com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>>(this._root, this, "algorithm"):this.algorithm);
        }

        public com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> variableEncoding() {
            return ((this.variableEncoding == null)?this.variableEncoding = new com.kscs.util.jaxb.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>>(this._root, this, "variableEncoding"):this.variableEncoding);
        }

        public StoppingRuleConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> stoppingRules() {
            return ((this.stoppingRules == null)?this.stoppingRules = new StoppingRuleConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>>(this._root, this, "stoppingRules"):this.stoppingRules);
        }

        public OperatorConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> operators() {
            return ((this.operators == null)?this.operators = new OperatorConfiguration.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>>(this._root, this, "operators"):this.operators);
        }

        public ConfigurationParameter.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>> algorithmParameters() {
            return ((this.algorithmParameters == null)?this.algorithmParameters = new ConfigurationParameter.Selector<TRoot, AlgorithmConfiguration.Selector<TRoot, TParent>>(this._root, this, "algorithmParameters"):this.algorithmParameters);
        }

    }

}
