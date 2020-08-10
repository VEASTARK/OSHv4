//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.08.10 at 03:54:47 PM CEST 
//


package osh.configuration.system;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import com.kscs.util.jaxb.Copyable;
import com.kscs.util.jaxb.PartialCopyable;
import com.kscs.util.jaxb.PropertyTree;
import com.kscs.util.jaxb.PropertyTreeUse;


/**
 * <p>Java class for GridConfig complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="GridConfig"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="gridType" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="gridLayoutSource" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GridConfig", propOrder = {
    "gridType",
    "gridLayoutSource"
})
public class GridConfig implements Copyable, PartialCopyable
{

    @XmlElement(required = true)
    protected String gridType;
    @XmlElement(required = true)
    protected String gridLayoutSource;

    /**
     * Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
     * 
     */
    public GridConfig() {
        // Generated by copy-constructor plugin, JAXB requires public no-arg constructor.
    }

    /**
     * Instantiates a GridConfig copying the state of another GridConfig
     * 
     * @param _other
     *     The original GridConfig from which to copy state.
     */
    public GridConfig(final GridConfig _other) {
        this.gridType = _other.gridType;
        this.gridLayoutSource = _other.gridLayoutSource;
    }

    /**
     * Instantiates a GridConfig copying the state of another GridConfig
     * 
     * @param _propertyTreeUse
     *     Meaning of the {@link PropertyPath}: Exclude or include members contained in property path.
     * @param _propertyTree
     *     A restricting {@link PropertyPath} that defines which nodes of the source object tree should actually be copied.
     * @param _other
     *     The original GridConfig from which to copy state.
     */
    public GridConfig(final GridConfig _other, final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final PropertyTree gridTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("gridType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(gridTypePropertyTree!= null):((gridTypePropertyTree == null)||(!gridTypePropertyTree.isLeaf())))) {
            this.gridType = _other.gridType;
        }
        final PropertyTree gridLayoutSourcePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("gridLayoutSource"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(gridLayoutSourcePropertyTree!= null):((gridLayoutSourcePropertyTree == null)||(!gridLayoutSourcePropertyTree.isLeaf())))) {
            this.gridLayoutSource = _other.gridLayoutSource;
        }
    }

    /**
     * Gets the value of the gridType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGridType() {
        return gridType;
    }

    /**
     * Sets the value of the gridType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGridType(String value) {
        this.gridType = value;
    }

    /**
     * Gets the value of the gridLayoutSource property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGridLayoutSource() {
        return gridLayoutSource;
    }

    /**
     * Sets the value of the gridLayoutSource property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGridLayoutSource(String value) {
        this.gridLayoutSource = value;
    }

    @Override
    public GridConfig createCopy() {
        final GridConfig _newObject;
        try {
            _newObject = ((GridConfig) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        _newObject.gridType = this.gridType;
        _newObject.gridLayoutSource = this.gridLayoutSource;
        return _newObject;
    }

    @Override
    public GridConfig createCopy(final PropertyTree _propertyTree, final PropertyTreeUse _propertyTreeUse) {
        final GridConfig _newObject;
        try {
            _newObject = ((GridConfig) super.clone());
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
        final PropertyTree gridTypePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("gridType"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(gridTypePropertyTree!= null):((gridTypePropertyTree == null)||(!gridTypePropertyTree.isLeaf())))) {
            _newObject.gridType = this.gridType;
        }
        final PropertyTree gridLayoutSourcePropertyTree = ((_propertyTree == null)?null:_propertyTree.get("gridLayoutSource"));
        if (((_propertyTreeUse == PropertyTreeUse.INCLUDE)?(gridLayoutSourcePropertyTree!= null):((gridLayoutSourcePropertyTree == null)||(!gridLayoutSourcePropertyTree.isLeaf())))) {
            _newObject.gridLayoutSource = this.gridLayoutSource;
        }
        return _newObject;
    }

    @Override
    public GridConfig copyExcept(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.EXCLUDE);
    }

    @Override
    public GridConfig copyOnly(final PropertyTree _propertyTree) {
        return createCopy(_propertyTree, PropertyTreeUse.INCLUDE);
    }

    public static class Select
        extends GridConfig.Selector<GridConfig.Select, Void>
    {


        Select() {
            super(null, null, null);
        }

        public static GridConfig.Select _root() {
            return new GridConfig.Select();
        }

    }

    public static class Selector<TRoot extends com.kscs.util.jaxb.Selector<TRoot, ?> , TParent >
        extends com.kscs.util.jaxb.Selector<TRoot, TParent>
    {

        private com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>> gridType = null;
        private com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>> gridLayoutSource = null;

        public Selector(final TRoot root, final TParent parent, final String propertyName) {
            super(root, parent, propertyName);
        }

        @Override
        public Map<String, PropertyTree> buildChildren() {
            final Map<String, PropertyTree> products = new HashMap<String, PropertyTree>();
            products.putAll(super.buildChildren());
            if (this.gridType!= null) {
                products.put("gridType", this.gridType.init());
            }
            if (this.gridLayoutSource!= null) {
                products.put("gridLayoutSource", this.gridLayoutSource.init());
            }
            return products;
        }

        public com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>> gridType() {
            return ((this.gridType == null)?this.gridType = new com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>>(this._root, this, "gridType"):this.gridType);
        }

        public com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>> gridLayoutSource() {
            return ((this.gridLayoutSource == null)?this.gridLayoutSource = new com.kscs.util.jaxb.Selector<TRoot, GridConfig.Selector<TRoot, TParent>>(this._root, this, "gridLayoutSource"):this.gridLayoutSource);
        }

    }

}
