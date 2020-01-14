//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.6 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2014.07.22 um 01:54:29 PM CEST 
//


package constructsimulation;

import osh.simulation.screenplay.ScreenplayType;

import javax.xml.bind.annotation.*;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java-Klasse für anonymous complex type.
 *
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 *
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Type" type="{http://osh/Simulation/Screenplay}SimulationType"/>
 *         &lt;element name="InitialRandomSeed" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="AlwaysNewRandomSeed" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="NumberOfRuns" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="SimulationResultLogging" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="SimulationDuration" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *         &lt;element name="SimulationStartTime" type="{http://www.w3.org/2001/XMLSchema}dateTime"/>
 *         &lt;element name="ScreenplayType" type="{http://osh/Simulation/Screenplay}ScreenplayType"/>
 *         &lt;element name="StaticScreenplayArguments">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="PriceCurveDuration" type="{http://www.w3.org/2001/XMLSchema}long"/>
 *                   &lt;element name="ChosenPriceCurve" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="DynamicScreenplayArguments">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="NumPersons" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *                 &lt;/sequence>
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="GUIScreenplayArguments">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *         &lt;element name="EPSOptimizationObjectives" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *         &lt;element name="PLSOptimizationObjectives" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *         &lt;element name="PLSOverLimitFactor" type="{http://www.w3.org/2001/XMLSchema}double"/>
 *         &lt;element name="OptimizationAlgorithms" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "type",
        "initialRandomSeed",
        "alwaysNewRandomSeed",
        "numberOfRuns",
        "simulationResultLogging",
        "simulationDuration",
        "simulationStartTime",
        "screenplayType",
        "staticScreenplayArguments",
        "dynamicScreenplayArguments",
        "guiScreenplayArguments",
        "epsOptimizationObjectives",
        "plsOptimizationObjectives",
        "plsOverLimitFactor",
        "optimizationAlgorithms"
})
@XmlRootElement(name = "SimulationPackage")
public class SimulationPackage implements Cloneable {

    @XmlElement(name = "InitialRandomSeed")
    protected long initialRandomSeed;
    @XmlElement(name = "AlwaysNewRandomSeed")
    protected boolean alwaysNewRandomSeed;
    @XmlElement(name = "NumberOfRuns")
    protected int numberOfRuns;
    @XmlElement(name = "SimulationResultLogging")
    protected boolean simulationResultLogging;
    @XmlElement(name = "SimulationDuration")
    protected long simulationDuration;
    @XmlElement(name = "SimulationStartTime", required = true)
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar simulationStartTime;
    @XmlElement(name = "ScreenplayType", required = true)
    protected ScreenplayType screenplayType;
    @XmlElement(name = "StaticScreenplayArguments", required = true)
    protected SimulationPackage.StaticScreenplayArguments staticScreenplayArguments;
    @XmlElement(name = "DynamicScreenplayArguments", required = true)
    protected SimulationPackage.DynamicScreenplayArguments dynamicScreenplayArguments;
    @XmlElement(name = "GUIScreenplayArguments", required = true)
    protected SimulationPackage.GUIScreenplayArguments guiScreenplayArguments;
    @XmlElement(name = "EPSOptimizationObjectives", type = Integer.class)
    protected List<Integer> epsOptimizationObjectives;
    @XmlElement(name = "PLSOptimizationObjectives", type = Integer.class)
    protected List<Integer> plsOptimizationObjectives;
    @XmlElement(name = "PLSOverLimitFactor")
    protected double plsOverLimitFactor;
    @XmlElement(name = "OptimizationAlgorithms", type = Integer.class)
    protected List<Integer> optimizationAlgorithms;

    /**
     * Creates a new {@code SimulationPackage} instance.
     */
    public SimulationPackage() {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
    }

    /**
     * Creates a new {@code SimulationPackage} instance by deeply copying a given {@code SimulationPackage} instance.
     *
     * @param o The instance to copy.
     * @throws NullPointerException if {@code o} is {@code null}.
     */
    public SimulationPackage(final SimulationPackage o) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        super();
        if (o == null) {
            throw new NullPointerException("Cannot create a copy of 'SimulationPackage' from 'null'.");
        }
        // CBuiltinLeafInfo: java.lang.Long
        this.initialRandomSeed = o.initialRandomSeed;
        // CBuiltinLeafInfo: java.lang.Boolean
        this.alwaysNewRandomSeed = o.alwaysNewRandomSeed;
        // CBuiltinLeafInfo: java.lang.Integer
        this.numberOfRuns = o.numberOfRuns;
        // CBuiltinLeafInfo: java.lang.Boolean
        this.simulationResultLogging = o.simulationResultLogging;
        // CBuiltinLeafInfo: java.lang.Long
        this.simulationDuration = o.simulationDuration;
        // CBuiltinLeafInfo: javax.xml.datatype.XMLGregorianCalendar
        this.simulationStartTime = o.simulationStartTime == null ? null : (XMLGregorianCalendar) o.simulationStartTime.clone();
        // CEnumLeafInfo: osh.simulation.screenplay.ScreenplayType
        this.screenplayType = o.screenplayType;
        // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$StaticScreenplayArguments
        this.staticScreenplayArguments = o.staticScreenplayArguments == null ? null : o.staticScreenplayArguments.clone();
        // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$DynamicScreenplayArguments
        this.dynamicScreenplayArguments = o.dynamicScreenplayArguments == null ? null : o.dynamicScreenplayArguments.clone();
        // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$GUIScreenplayArguments
        this.guiScreenplayArguments = o.guiScreenplayArguments == null ? null : o.guiScreenplayArguments.clone();
        // 'EPSOptimizationObjectives' collection.
        if (o.epsOptimizationObjectives != null) {
            copyEPSOptimizationObjectives(o.getEPSOptimizationObjectives(), this.getEPSOptimizationObjectives());
        }
        // 'PLSOptimizationObjectives' collection.
        if (o.plsOptimizationObjectives != null) {
            copyPLSOptimizationObjectives(o.getPLSOptimizationObjectives(), this.getPLSOptimizationObjectives());
        }
        // CBuiltinLeafInfo: java.lang.Double
        this.plsOverLimitFactor = o.plsOverLimitFactor;
        // 'OptimizationAlgorithms' collection.
        if (o.optimizationAlgorithms != null) {
            copyOptimizationAlgorithms(o.getOptimizationAlgorithms(), this.getOptimizationAlgorithms());
        }
    }

    /**
     * Copies all values of property {@code EPSOptimizationObjectives} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyEPSOptimizationObjectives(final List<Integer> source, final List<Integer> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof Integer) {
                    // CBuiltinLeafInfo: java.lang.Integer
                    target.add(((Integer) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'EPSOptimizationObjectives' of class 'osh.simulation.simulationpackage.SimulationPackage'."));
            }
        }
    }

    /**
     * Copies all values of property {@code PLSOptimizationObjectives} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyPLSOptimizationObjectives(final List<Integer> source, final List<Integer> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof Integer) {
                    // CBuiltinLeafInfo: java.lang.Integer
                    target.add(((Integer) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'PLSOptimizationObjectives' of class 'osh.simulation.simulationpackage.SimulationPackage'."));
            }
        }
    }

    /**
     * Copies all values of property {@code OptimizationAlgorithms} deeply.
     *
     * @param source The source to copy from.
     * @param target The target to copy {@code source} to.
     * @throws NullPointerException if {@code target} is {@code null}.
     */
    private static void copyOptimizationAlgorithms(final List<Integer> source, final List<Integer> target) {
        // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
        if ((source != null) && (!source.isEmpty())) {
            for (final Object next : source) {
                if (next instanceof Integer) {
                    // CBuiltinLeafInfo: java.lang.Integer
                    target.add(((Integer) next));
                    continue;
                }
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError((("Unexpected instance '" + next) + "' for property 'OptimizationAlgorithms' of class 'osh.simulation.simulationpackage.SimulationPackage'."));
            }
        }
    }

    /**
     * Ruft den Wert der initialRandomSeed-Eigenschaft ab.
     */
    public long getInitialRandomSeed() {
        return this.initialRandomSeed;
    }

    /**
     * Legt den Wert der initialRandomSeed-Eigenschaft fest.
     */
    public void setInitialRandomSeed(long value) {
        this.initialRandomSeed = value;
    }

    /**
     * Ruft den Wert der alwaysNewRandomSeed-Eigenschaft ab.
     */
    public boolean isAlwaysNewRandomSeed() {
        return this.alwaysNewRandomSeed;
    }

    /**
     * Legt den Wert der alwaysNewRandomSeed-Eigenschaft fest.
     */
    public void setAlwaysNewRandomSeed(boolean value) {
        this.alwaysNewRandomSeed = value;
    }

    /**
     * Ruft den Wert der numberOfRuns-Eigenschaft ab.
     */
    public int getNumberOfRuns() {
        return this.numberOfRuns;
    }

    /**
     * Legt den Wert der numberOfRuns-Eigenschaft fest.
     */
    public void setNumberOfRuns(int value) {
        this.numberOfRuns = value;
    }

    /**
     * Ruft den Wert der simulationResultLogging-Eigenschaft ab.
     */
    public boolean isSimulationResultLogging() {
        return this.simulationResultLogging;
    }

    /**
     * Legt den Wert der simulationResultLogging-Eigenschaft fest.
     */
    public void setSimulationResultLogging(boolean value) {
        this.simulationResultLogging = value;
    }

    /**
     * Ruft den Wert der simulationDuration-Eigenschaft ab.
     */
    public long getSimulationDuration() {
        return this.simulationDuration;
    }

    /**
     * Legt den Wert der simulationDuration-Eigenschaft fest.
     */
    public void setSimulationDuration(long value) {
        this.simulationDuration = value;
    }

    /**
     * Ruft den Wert der simulationStartTime-Eigenschaft ab.
     *
     * @return possible object is
     * {@link XMLGregorianCalendar }
     */
    public XMLGregorianCalendar getSimulationStartTime() {
        return this.simulationStartTime;
    }

    /**
     * Legt den Wert der simulationStartTime-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link XMLGregorianCalendar }
     */
    public void setSimulationStartTime(XMLGregorianCalendar value) {
        this.simulationStartTime = value;
    }

    /**
     * Ruft den Wert der screenplayType-Eigenschaft ab.
     *
     * @return possible object is
     * {@link ScreenplayType }
     */
    public ScreenplayType getScreenplayType() {
        return this.screenplayType;
    }

    /**
     * Legt den Wert der screenplayType-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link ScreenplayType }
     */
    public void setScreenplayType(ScreenplayType value) {
        this.screenplayType = value;
    }

    /**
     * Ruft den Wert der staticScreenplayArguments-Eigenschaft ab.
     *
     * @return possible object is
     * {@link SimulationPackage.StaticScreenplayArguments }
     */
    public SimulationPackage.StaticScreenplayArguments getStaticScreenplayArguments() {
        return this.staticScreenplayArguments;
    }

    /**
     * Legt den Wert der staticScreenplayArguments-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link SimulationPackage.StaticScreenplayArguments }
     */
    public void setStaticScreenplayArguments(SimulationPackage.StaticScreenplayArguments value) {
        this.staticScreenplayArguments = value;
    }

    /**
     * Ruft den Wert der dynamicScreenplayArguments-Eigenschaft ab.
     *
     * @return possible object is
     * {@link SimulationPackage.DynamicScreenplayArguments }
     */
    public SimulationPackage.DynamicScreenplayArguments getDynamicScreenplayArguments() {
        return this.dynamicScreenplayArguments;
    }

    /**
     * Legt den Wert der dynamicScreenplayArguments-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link SimulationPackage.DynamicScreenplayArguments }
     */
    public void setDynamicScreenplayArguments(SimulationPackage.DynamicScreenplayArguments value) {
        this.dynamicScreenplayArguments = value;
    }

    /**
     * Ruft den Wert der guiScreenplayArguments-Eigenschaft ab.
     *
     * @return possible object is
     * {@link SimulationPackage.GUIScreenplayArguments }
     */
    public SimulationPackage.GUIScreenplayArguments getGUIScreenplayArguments() {
        return this.guiScreenplayArguments;
    }

    /**
     * Legt den Wert der guiScreenplayArguments-Eigenschaft fest.
     *
     * @param value allowed object is
     *              {@link SimulationPackage.GUIScreenplayArguments }
     */
    public void setGUIScreenplayArguments(SimulationPackage.GUIScreenplayArguments value) {
        this.guiScreenplayArguments = value;
    }

    /**
     * Gets the value of the epsOptimizationObjectives property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the epsOptimizationObjectives property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEPSOptimizationObjectives().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     */
    public List<Integer> getEPSOptimizationObjectives() {
        if (this.epsOptimizationObjectives == null) {
            this.epsOptimizationObjectives = new ArrayList<>();
        }
        return this.epsOptimizationObjectives;
    }

    /**
     * Gets the value of the plsOptimizationObjectives property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the plsOptimizationObjectives property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getPLSOptimizationObjectives().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     */
    public List<Integer> getPLSOptimizationObjectives() {
        if (this.plsOptimizationObjectives == null) {
            this.plsOptimizationObjectives = new ArrayList<>();
        }
        return this.plsOptimizationObjectives;
    }

    /**
     * Ruft den Wert der plsOverLimitFactor-Eigenschaft ab.
     */
    public double getPLSOverLimitFactor() {
        return this.plsOverLimitFactor;
    }

    /**
     * Legt den Wert der plsOverLimitFactor-Eigenschaft fest.
     */
    public void setPLSOverLimitFactor(double value) {
        this.plsOverLimitFactor = value;
    }

    /**
     * Gets the value of the optimizationAlgorithms property.
     *
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the optimizationAlgorithms property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOptimizationAlgorithms().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Integer }
     */
    public List<Integer> getOptimizationAlgorithms() {
        if (this.optimizationAlgorithms == null) {
            this.optimizationAlgorithms = new ArrayList<>();
        }
        return this.optimizationAlgorithms;
    }

    /**
     * Creates and returns a deep copy of this object.
     *
     * @return A deep copy of this object.
     */
    @Override
    public SimulationPackage clone() {
        try {
            {
                // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                final SimulationPackage clone = ((SimulationPackage) super.clone());
                // CBuiltinLeafInfo: java.lang.Long
                clone.initialRandomSeed = this.initialRandomSeed;
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.alwaysNewRandomSeed = this.alwaysNewRandomSeed;
                // CBuiltinLeafInfo: java.lang.Integer
                clone.numberOfRuns = this.numberOfRuns;
                // CBuiltinLeafInfo: java.lang.Boolean
                clone.simulationResultLogging = this.simulationResultLogging;
                // CBuiltinLeafInfo: java.lang.Long
                clone.simulationDuration = this.simulationDuration;
                // CBuiltinLeafInfo: javax.xml.datatype.XMLGregorianCalendar
                clone.simulationStartTime = this.simulationStartTime == null ? null : (XMLGregorianCalendar) this.simulationStartTime.clone();
                // CEnumLeafInfo: osh.simulation.screenplay.ScreenplayType
                clone.screenplayType = (this.screenplayType);
                // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$StaticScreenplayArguments
                clone.staticScreenplayArguments = this.staticScreenplayArguments == null ? null : this.staticScreenplayArguments.clone();
                // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$DynamicScreenplayArguments
                clone.dynamicScreenplayArguments = this.dynamicScreenplayArguments == null ? null : this.dynamicScreenplayArguments.clone();
                // CClassInfo: osh.simulation.simulationpackage.SimulationPackage$GUIScreenplayArguments
                clone.guiScreenplayArguments = this.guiScreenplayArguments == null ? null : this.guiScreenplayArguments.clone();
                // 'EPSOptimizationObjectives' collection.
                if (this.epsOptimizationObjectives != null) {
                    clone.epsOptimizationObjectives = null;
                    copyEPSOptimizationObjectives(this.getEPSOptimizationObjectives(), clone.getEPSOptimizationObjectives());
                }
                // 'PLSOptimizationObjectives' collection.
                if (this.plsOptimizationObjectives != null) {
                    clone.plsOptimizationObjectives = null;
                    copyPLSOptimizationObjectives(this.getPLSOptimizationObjectives(), clone.getPLSOptimizationObjectives());
                }
                // CBuiltinLeafInfo: java.lang.Double
                clone.plsOverLimitFactor = this.plsOverLimitFactor;
                // 'OptimizationAlgorithms' collection.
                if (this.optimizationAlgorithms != null) {
                    clone.optimizationAlgorithms = null;
                    copyOptimizationAlgorithms(this.getOptimizationAlgorithms(), clone.getOptimizationAlgorithms());
                }
                return clone;
            }
        } catch (CloneNotSupportedException e) {
            // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
            throw new AssertionError(e);
        }
    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     *
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="NumPersons" type="{http://www.w3.org/2001/XMLSchema}int" maxOccurs="unbounded"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "numPersons"
    })
    public static class DynamicScreenplayArguments implements Cloneable {

        @XmlElement(name = "NumPersons", type = Integer.class)
        protected List<Integer> numPersons;

        /**
         * Creates a new {@code DynamicScreenplayArguments} instance.
         */
        public DynamicScreenplayArguments() {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
        }

        /**
         * Creates a new {@code DynamicScreenplayArguments} instance by deeply copying a given {@code DynamicScreenplayArguments} instance.
         *
         * @param o The instance to copy.
         * @throws NullPointerException if {@code o} is {@code null}.
         */
        public DynamicScreenplayArguments(final SimulationPackage.DynamicScreenplayArguments o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
            if (o == null) {
                throw new NullPointerException("Cannot create a copy of 'DynamicScreenplayArguments' from 'null'.");
            }
            // 'NumPersons' collection.
            if (o.numPersons != null) {
                copyNumPersons(o.getNumPersons(), this.getNumPersons());
            }
        }

        /**
         * Copies all values of property {@code NumPersons} deeply.
         *
         * @param source The source to copy from.
         * @param target The target to copy {@code source} to.
         * @throws NullPointerException if {@code target} is {@code null}.
         */
        private static void copyNumPersons(final List<Integer> source, final List<Integer> target) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            if ((source != null) && (!source.isEmpty())) {
                for (final Object next : source) {
                    if (next instanceof Integer) {
                        // CBuiltinLeafInfo: java.lang.Integer
                        target.add(((Integer) next));
                        continue;
                    }
                    // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                    throw new AssertionError((("Unexpected instance '" + next) + "' for property 'NumPersons' of class 'osh.simulation.simulationpackage.SimulationPackage$DynamicScreenplayArguments'."));
                }
            }
        }

        /**
         * Gets the value of the numPersons property.
         *
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the numPersons property.
         *
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getNumPersons().add(newItem);
         * </pre>
         *
         *
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link Integer }
         */
        public List<Integer> getNumPersons() {
            if (this.numPersons == null) {
                this.numPersons = new ArrayList<>();
            }
            return this.numPersons;
        }

        /**
         * Creates and returns a deep copy of this object.
         *
         * @return A deep copy of this object.
         */
        @Override
        public SimulationPackage.DynamicScreenplayArguments clone() {
            try {
                {
                    // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                    final SimulationPackage.DynamicScreenplayArguments clone = ((SimulationPackage.DynamicScreenplayArguments) super.clone());
                    // 'NumPersons' collection.
                    if (this.numPersons != null) {
                        clone.numPersons = null;
                        copyNumPersons(this.getNumPersons(), clone.getNumPersons());
                    }
                    return clone;
                }
            } catch (CloneNotSupportedException e) {
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError(e);
            }
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     *
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class GUIScreenplayArguments implements Cloneable {


        /**
         * Creates a new {@code GUIScreenplayArguments} instance.
         */
        public GUIScreenplayArguments() {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
        }

        /**
         * Creates a new {@code GUIScreenplayArguments} instance by deeply copying a given {@code GUIScreenplayArguments} instance.
         *
         * @param o The instance to copy.
         * @throws NullPointerException if {@code o} is {@code null}.
         */
        public GUIScreenplayArguments(final SimulationPackage.GUIScreenplayArguments o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
            if (o == null) {
                throw new NullPointerException("Cannot create a copy of 'GUIScreenplayArguments' from 'null'.");
            }
        }

        /**
         * Creates and returns a deep copy of this object.
         *
         * @return A deep copy of this object.
         */
        @Override
        public SimulationPackage.GUIScreenplayArguments clone() {
            try {
                {
                    // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                    return ((GUIScreenplayArguments) super.clone());
                }
            } catch (CloneNotSupportedException e) {
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError(e);
            }
        }

    }


    /**
     * <p>Java-Klasse für anonymous complex type.
     *
     * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
     *
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;sequence>
     *         &lt;element name="PriceCurveDuration" type="{http://www.w3.org/2001/XMLSchema}long"/>
     *         &lt;element name="ChosenPriceCurve" type="{http://www.w3.org/2001/XMLSchema}int"/>
     *       &lt;/sequence>
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "priceCurveDuration",
            "chosenPriceCurve"
    })
    public static class StaticScreenplayArguments implements Cloneable {

        @XmlElement(name = "PriceCurveDuration")
        protected long priceCurveDuration;
        @XmlElement(name = "ChosenPriceCurve")
        protected int chosenPriceCurve;

        /**
         * Creates a new {@code StaticScreenplayArguments} instance.
         */
        public StaticScreenplayArguments() {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
        }

        /**
         * Creates a new {@code StaticScreenplayArguments} instance by deeply copying a given {@code StaticScreenplayArguments} instance.
         *
         * @param o The instance to copy.
         * @throws NullPointerException if {@code o} is {@code null}.
         */
        public StaticScreenplayArguments(final SimulationPackage.StaticScreenplayArguments o) {
            // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
            super();
            if (o == null) {
                throw new NullPointerException("Cannot create a copy of 'StaticScreenplayArguments' from 'null'.");
            }
            // CBuiltinLeafInfo: java.lang.Long
            this.priceCurveDuration = o.priceCurveDuration;
            // CBuiltinLeafInfo: java.lang.Integer
            this.chosenPriceCurve = o.chosenPriceCurve;
        }

        /**
         * Ruft den Wert der priceCurveDuration-Eigenschaft ab.
         */
        public long getPriceCurveDuration() {
            return this.priceCurveDuration;
        }

        /**
         * Legt den Wert der priceCurveDuration-Eigenschaft fest.
         */
        public void setPriceCurveDuration(long value) {
            this.priceCurveDuration = value;
        }

        /**
         * Ruft den Wert der chosenPriceCurve-Eigenschaft ab.
         */
        public int getChosenPriceCurve() {
            return this.chosenPriceCurve;
        }

        /**
         * Legt den Wert der chosenPriceCurve-Eigenschaft fest.
         */
        public void setChosenPriceCurve(int value) {
            this.chosenPriceCurve = value;
        }

        /**
         * Creates and returns a deep copy of this object.
         *
         * @return A deep copy of this object.
         */
        @Override
        public SimulationPackage.StaticScreenplayArguments clone() {
            try {
                {
                    // CC-XJC Version 2.0.1 Build 2012-03-02T12:09:12+0000
                    final SimulationPackage.StaticScreenplayArguments clone = ((SimulationPackage.StaticScreenplayArguments) super.clone());
                    // CBuiltinLeafInfo: java.lang.Long
                    clone.priceCurveDuration = this.priceCurveDuration;
                    // CBuiltinLeafInfo: java.lang.Integer
                    clone.chosenPriceCurve = this.chosenPriceCurve;
                    return clone;
                }
            } catch (CloneNotSupportedException e) {
                // Please report this at https://apps.sourceforge.net/mantisbt/ccxjc/
                throw new AssertionError(e);
            }
        }

    }

}
