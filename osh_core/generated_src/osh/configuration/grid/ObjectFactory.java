//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.4.0-b180830.0438 
// See <a href="https://javaee.github.io/jaxb-v2/">https://javaee.github.io/jaxb-v2/</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2020.04.01 at 07:36:51 PM CEST 
//


package osh.configuration.grid;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the osh.configuration.grid package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: osh.configuration.grid
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GridLayout }
     * 
     */
    public GridLayout createGridLayout() {
        return new GridLayout();
    }

    /**
     * Create an instance of {@link LayoutConnection }
     * 
     */
    public LayoutConnection createLayoutConnection() {
        return new LayoutConnection();
    }

    /**
     * Create an instance of {@link DevicePerMeter }
     * 
     */
    public DevicePerMeter createDevicePerMeter() {
        return new DevicePerMeter();
    }

}
