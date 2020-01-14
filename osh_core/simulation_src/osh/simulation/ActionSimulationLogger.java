package osh.simulation;

import osh.core.logging.IGlobalLogger;
import osh.simulation.screenplay.Screenplay;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.xml.XMLSerialization;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.*;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ActionSimulationLogger implements ISimulationActionLogger {

    private final IGlobalLogger logger;
    private OutputStream stream;

    public ActionSimulationLogger(IGlobalLogger logger, String filename) throws FileNotFoundException {
        this.logger = logger;
        this.stream = new FileOutputStream(filename);
    }

    @Override
    public void logAction(SubjectAction action) {
        if (this.stream == null) {
            this.logger.logError("logger stream was closed", new Exception());
            return;
        }

        try {
            Screenplay sp = new Screenplay();
            sp.getSIMActions().add(action);
            XMLSerialization.marshal(this.stream, sp);
        } catch (JAXBException e) {
            this.logger.logError("could not log action", e);
        }
    }

    public void closeStream() {
        try {
            if (this.stream != null) this.stream.close();
        } catch (IOException ignored) {
        }
        this.stream = null;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
            "element"
    })
    @XmlRootElement(name = "root")
    public static class RootElement {


        @XmlElement(name = "element")
        private SubjectAction element;

        public RootElement() {
        }

        public RootElement(SubjectAction element) {
            super();
            this.element = element;
        }

        public SubjectAction getElement() {
            return this.element;
        }

        public void setElement(SubjectAction element) {
            this.element = element;
        }

    }

}
