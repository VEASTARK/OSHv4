package osh.core.oc;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSHOC;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

/**
 * represents the observer of the global o/c-unit
 *
 * @author Florian Allerding
 */
public abstract class GlobalObserver extends Observer {

    protected final OSHParameterCollection configurationParameters;
    private GlobalOCUnit assignedOCUnit;


    /**
     * CONSTRUCTOR
     */
    public GlobalObserver(IOSHOC osh, OSHParameterCollection configurationParameters) {
        super(osh);
        this.configurationParameters = configurationParameters;
    }

    /**
     * assign the osh o/c-unit
     */
    protected void assignControllerBox(GlobalOCUnit assignedOCUnit) {
        this.assignedOCUnit = assignedOCUnit;
    }

    /**
     * gets a local observer based on it's id
     *
     * @param deviceID
     * @return
     */
    public LocalObserver getLocalObserver(UUID deviceID) {

        LocalOCUnit _localOC = this.assignedOCUnit.getLocalUnits().get(deviceID);
        if (_localOC != null)
            return _localOC.localObserver;
        else
            return null;
    }

    /**
     * Returns all assigned local Observer
     *
     * @return
     */
    public ArrayList<LocalObserver> getAllLocalObservers() {

        ArrayList<LocalObserver> _localObserver = new ArrayList<>();
        Collection<LocalOCUnit> _ocCollection = this.assignedOCUnit.getLocalUnits().values();
        ArrayList<LocalOCUnit> _localOCUnits = new ArrayList<>(_ocCollection);

        for (LocalOCUnit localOCUnit : _localOCUnits) {
            _localObserver.add(localOCUnit.localObserver);
        }

        return _localObserver;
    }

    /**
     * returns the list of all ids form the assignd devices/local units
     *
     * @return
     */
    public ArrayList<UUID> getAssignedDeviceIDs() {
        return new ArrayList<>(this.assignedOCUnit.getLocalUnits().keySet());
    }


    public GlobalOCUnit getAssignedOCUnit() {
        return this.assignedOCUnit;
    }

}
