package osh.core.oc;

import osh.core.OCComponent;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.ILifeCycleListener;
import osh.core.interfaces.IOSHOC;
import osh.eal.time.TimeExchange;
import osh.registry.Registry.OCRegistry;
import osh.registry.interfaces.ITimeRegistryListener;

/**
 * abstract superclass for all controllers
 *
 * @author Florian Allerding
 */
public abstract class Controller extends OCComponent implements ITimeRegistryListener, ILifeCycleListener {


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public Controller(IOSHOC osh) {
        super(osh);
    }


    @Override
    public IOSHOC getOSH() {
        return super.getOSH();
    }


    protected OCRegistry getOCRegistry() {
        return this.getOSH().getOCRegistry();
    }

    @Override
    public void onSystemError() {
        //...in case of use please override
    }

    @Override
    public void onSystemHalt() {
        //...in case of use please override
    }

    @Override
    public void onSystemRunning() {
        //...in case of use please override
    }

    @Override
    public void onSystemIsUp() throws OSHException {
        //...in case of use please override
    }

    @Override
    public void onSystemResume() {
        //...in case of use please override
    }

    @Override
    public void onSystemShutdown() throws OSHException {
        //...in case of use please override
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        //...in case of use please override
    }
}
