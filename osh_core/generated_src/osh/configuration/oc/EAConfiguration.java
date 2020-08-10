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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;


/**
 * <p>Java class for EAConfiguration complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="EAConfiguration"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="algorithms" type="{http://osh/configuration/oc}AlgorithmConfiguration" maxOccurs="unbounded"/&gt;
 *         &lt;element name="eaObjectives" type="{http://osh/configuration/oc}EAObjectives" maxOccurs="unbounded"/&gt;
 *         &lt;element name="solutionRanking" type="{http://osh/configuration/oc}SolutionRanking" minOccurs="0"/&gt;
 *         &lt;element name="loggingConfiguration" type="{http://osh/configuration/oc}LoggingConfiguration" minOccurs="0"/&gt;
 *         &lt;element name="executeAlgorithmsParallel" type="{http://www.w3.org/2001/XMLSchema}boolean"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EAConfiguration", propOrder = {
    "algorithms",
    "eaObjectives",
    "solutionRanking",
    "loggingConfiguration",
    "executeAlgorithmsParallel"
})
public class EAConfiguration implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    protected List<AlgorithmConfiguration> algorithms;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected List<EAObjectives> eaObjectives;
    protected SolutionRanking solutionRanking;
    protected LoggingConfiguration loggingConfiguration;
    protected boolean executeAlgorithmsParallel;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public EAConfiguration() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a EAConfiguration copying the state of another EAConfiguration
     * 
     * @param _other
     *     The original EAConfiguration from which to copy state.
     */
    public EAConfiguration(final EAConfiguration _other) {
        if (_other.algorithms == null) {
            this.algorithms = null;
        } else {
            this.algorithms = new ArrayList<AlgorithmConfiguration>();
            for (AlgorithmConfiguration _item: _other.algorithms) {
                this.algorithms.add(((_item == null)?null:_item.createCopy()));
            }
        }
        this.eaObjectives = ((_other.eaObjectives == null)?null:new ArrayList<EAObjectives>(_other.eaObjectives));
        this.solutionRanking = ((_other.solutionRanking == null)?null:_other.solutionRanking.createCopy());
        this.loggingConfiguration = ((_other.loggingConfiguration == null)?null:_other.loggingConfiguration.createCopy());
        this.executeAlgorithmsParallel = _other.executeAlgorithmsParallel;
    }

    /**
     * Instantiates a EAConfiguration copying the state of another EAConfiguration
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original EAConfiguration from which to copy state.
     */
    public EAConfiguration(final EAConfiguration _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree algorithmsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithms"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmsPropertyTree!= null):((algorithmsPropertyTree == null)||(!algorithmsPropertyTree.isLeaf())))) {
            if (_other.algorithms == null) {
                this.algorithms = null;
            } else {
                this.algorithms = new ArrayList<AlgorithmConfiguration>();
                for (AlgorithmConfiguration _item: _other.algorithms) {
                    this.algorithms.add(((_item == null)?null:_item.createCopy(algorithmsPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree eaObjectivesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("eaObjectives"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(eaObjectivesPropertyTree!= null):((eaObjectivesPropertyTree == null)||(!eaObjectivesPropertyTree.isLeaf())))) {
            this.eaObjectives = ((_other.eaObjectives == null)?null:new ArrayList<EAObjectives>(_other.eaObjectives));
        }
        final PropertyTree solutionRankingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("solutionRanking"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(solutionRankingPropertyTree!= null):((solutionRankingPropertyTree == null)||(!solutionRankingPropertyTree.isLeaf())))) {
            this.solutionRanking = ((_other.solutionRanking == null)?null:_other.solutionRanking.createCopy(solutionRankingPropertyTree, _propertyTreeUse));
        }
        final PropertyTree loggingConfigurationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("loggingConfiguration"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(loggingConfigurationPropertyTree!= null):((loggingConfigurationPropertyTree == null)||(!loggingConfigurationPropertyTree.isLeaf())))) {
            this.loggingConfiguration = ((_other.loggingConfiguration == null)?null:_other.loggingConfiguration.createCopy(loggingConfigurationPropertyTree, _propertyTreeUse));
        }
        final PropertyTree executeAlgorithmsParallelPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("executeAlgorithmsParallel"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(executeAlgorithmsParallelPropertyTree!= null):((executeAlgorithmsParallelPropertyTree == null)||(!executeAlgorithmsParallelPropertyTree.isLeaf())))) {
            this.executeAlgorithmsParallel = _other.executeAlgorithmsParallel;
        }
    }

    /**
     * Gets the value of the algorithms property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the algorithms property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getAlgorithms().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link AlgorithmConfiguration }
     * 
     * 
     */
    public List<AlgorithmConfiguration> getAlgorithms() {
        if (algorithms == null) {
            algorithms = new ArrayList<AlgorithmConfiguration>();
        }
        return this.algorithms;
    }

    /**
     * Gets the value of the eaObjectives property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the eaObjectives property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEaObjectives().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link EAObjectives }
     * 
     * 
     */
    public List<EAObjectives> getEaObjectives() {
        if (eaObjectives == null) {
            eaObjectives = new ArrayList<EAObjectives>();
        }
        return this.eaObjectives;
    }

    /**
     * Gets the value of the solutionRanking property.
     * 
     * @return
     *     possible object is
     *     {@link SolutionRanking }
     *     
     */
    public SolutionRanking getSolutionRanking() {
        return solutionRanking;
    }

    /**
     * Sets the value of the solutionRanking property.
     * 
     * @param value
     *     allowed object is
     *     {@link SolutionRanking }
     *     
     */
    public void setSolutionRanking(SolutionRanking value) {
        this.solutionRanking = value;
    }

    /**
     * Gets the value of the loggingConfiguration property.
     * 
     * @return
     *     possible object is
     *     {@link LoggingConfiguration }
     *     
     */
    public LoggingConfiguration getLoggingConfiguration() {
        return loggingConfiguration;
    }

    /**
     * Sets the value of the loggingConfiguration property.
     * 
     * @param value
     *     allowed object is
     *     {@link LoggingConfiguration }
     *     
     */
    public void setLoggingConfiguration(LoggingConfiguration value) {
        this.loggingConfiguration = value;
    }

    /**
     * Gets the value of the executeAlgorithmsParallel property.
     * 
     */
    public boolean isExecuteAlgorithmsParallel() {
        return executeAlgorithmsParallel;
    }

    /**
     * Sets the value of the executeAlgorithmsParallel property.
     * 
     */
    public void setExecuteAlgorithmsParallel(boolean value) {
        this.executeAlgorithmsParallel = value;
    }

    @Override
    public EAConfiguration createCopy() {
        final EAConfiguration _newObject;
        try {
            _newObject = ((EAConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        if (this.algorithms == null) {
            _newObject.algorithms = null;
        } else {
            _newObject.algorithms = new ArrayList<AlgorithmConfiguration>();
            for (AlgorithmConfiguration _item: this.algorithms) {
                _newObject.algorithms.add(((_item == null)?null:_item.createCopy()));
            }
        }
        _newObject.eaObjectives = ((this.eaObjectives == null)?null:new ArrayList<EAObjectives>(this.eaObjectives));
        _newObject.solutionRanking = ((this.solutionRanking == null)?null:this.solutionRanking.createCopy());
        _newObject.loggingConfiguration = ((this.loggingConfiguration == null)?null:this.loggingConfiguration.createCopy());
        _newObject.executeAlgorithmsParallel = this.executeAlgorithmsParallel;
        return _newObject;
    }

    @Override
    public EAConfiguration createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final EAConfiguration _newObject;
        try {
            _newObject = ((EAConfiguration) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree algorithmsPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("algorithms"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(algorithmsPropertyTree!= null):((algorithmsPropertyTree == null)||(!algorithmsPropertyTree.isLeaf())))) {
            if (this.algorithms == null) {
                _newObject.algorithms = null;
            } else {
                _newObject.algorithms = new ArrayList<AlgorithmConfiguration>();
                for (AlgorithmConfiguration _item: this.algorithms) {
                    _newObject.algorithms.add(((_item == null)?null:_item.createCopy(algorithmsPropertyTree, _propertyTreeUse)));
                }
            }
        }
        final PropertyTree eaObjectivesPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("eaObjectives"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(eaObjectivesPropertyTree!= null):((eaObjectivesPropertyTree == null)||(!eaObjectivesPropertyTree.isLeaf())))) {
            _newObject.eaObjectives = ((this.eaObjectives == null)?null:new ArrayList<EAObjectives>(this.eaObjectives));
        }
        final PropertyTree solutionRankingPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("solutionRanking"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(solutionRankingPropertyTree!= null):((solutionRankingPropertyTree == null)||(!solutionRankingPropertyTree.isLeaf())))) {
            _newObject.solutionRanking = ((this.solutionRanking == null)?null:this.solutionRanking.createCopy(solutionRankingPropertyTree, _propertyTreeUse));
        }
        final PropertyTree loggingConfigurationPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("loggingConfiguration"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(loggingConfigurationPropertyTree!= null):((loggingConfigurationPropertyTree == null)||(!loggingConfigurationPropertyTree.isLeaf())))) {
            _newObject.loggingConfiguration = ((this.loggingConfiguration == null)?null:this.loggingConfiguration.createCopy(loggingConfigurationPropertyTree, _propertyTreeUse));
        }
        final PropertyTree executeAlgorithmsParallelPropertyTree = ((_propertyTree == null)?null:_propertyTree.get("executeAlgorithmsParallel"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(executeAlgorithmsParallelPropertyTree!= null):((executeAlgorithmsParallelPropertyTree == null)||(!executeAlgorithmsParallelPropertyTree.isLeaf())))) {
            _newObject.executeAlgorithmsParallel = this.executeAlgorithmsParallel;
        }
        return _newObject;
    }

    @Override
    public EAConfiguration copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public EAConfiguration copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends EAConfiguration.Selector<EAConfiguration.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static EAConfiguration.Select _root() {
            return new EAConfiguration.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private AlgorithmConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> algorithms = null;
        private com.kscs.util.jaxb.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> eaObjectives = null;
        private SolutionRanking.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> solutionRanking = null;
        private LoggingConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> loggingConfiguration = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.algorithms!= null) {
                products.put("algorithms", this.algorithms.init());
            }
            if (this.eaObjectives!= null) {
                products.put("eaObjectives", this.eaObjectives.init());
            }
            if (this.solutionRanking!= null) {
                products.put("solutionRanking", this.solutionRanking.init());
            }
            if (this.loggingConfiguration!= null) {
                products.put("loggingConfiguration", this.loggingConfiguration.init());
            }
            return products;
        }

        public AlgorithmConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> algorithms() {
            return ((this.algorithms == null)?this.algorithms = new AlgorithmConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>>(this._root, this, "algorithms"):this.algorithms);
        }

        public com.kscs.util.jaxb.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> eaObjectives() {
            return ((this.eaObjectives == null)?this.eaObjectives = new com.kscs.util.jaxb.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>>(this._root, this, "eaObjectives"):this.eaObjectives);
        }

        public SolutionRanking.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> solutionRanking() {
            return ((this.solutionRanking == null)?this.solutionRanking = new SolutionRanking.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>>(this._root, this, "solutionRanking"):this.solutionRanking);
        }

        public LoggingConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>> loggingConfiguration() {
            return ((this.loggingConfiguration == null)?this.loggingConfiguration = new LoggingConfiguration.Selector<TRoot, EAConfiguration.Selector<TRoot, TParent>>(this._root, this, "loggingConfiguration"):this.loggingConfiguration);
        }

    }

}
