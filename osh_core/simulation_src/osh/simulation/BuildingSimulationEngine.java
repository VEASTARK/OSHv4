package osh.simulation;

import osh.OSHComponent;
import osh.configuration.OSHParameterCollection;
import osh.configuration.system.ConfigurationParameter;
import osh.datatypes.commodity.AncillaryMeterState;
import osh.datatypes.registry.oc.details.energy.AncillaryMeterStateExchange;
import osh.simulation.energy.IDeviceEnergySubject;
import osh.simulation.energy.SimEnergySimulationCore;
import osh.simulation.exception.SimulationEngineException;
import osh.simulation.exception.SimulationSubjectException;

import java.util.*;

/**
 * Simulation engine for the smart-home-lab
 *
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class BuildingSimulationEngine extends SimulationEngine {

    private final UUID entityUUID;
    // Simulation Subjects
    private final ArrayList<ISimulationSubject> simSubjectsList;
    private final HashMap<UUID, ISimulationSubject> simSubjectsMap;
    private final OSHParameterCollection engineParameters;
    private final SimEnergySimulationCore energySimulationCore;
    // ESC Subjects
    private final ArrayList<IDeviceEnergySubject> energySimSubjectsList;
    private final HashMap<UUID, IDeviceEnergySubject> energySimSubjectsMap;


    /**
     * CONSTRUCTOR<br>
     * constructor with a given array of devices to simulate...yes everything is a device!
     *
     */
    public BuildingSimulationEngine(
            ArrayList<? extends OSHComponent> deviceList,
            List<ConfigurationParameter> engineParameters,
            SimEnergySimulationCore esc,
            ISimulationActionLogger simLogger,
            UUID entityUUID) throws SimulationEngineException {

        this.energySimulationCore = esc;

        this.simSubjectsList = new ArrayList<>();
        this.simSubjectsMap = new HashMap<>();

        this.energySimSubjectsList = new ArrayList<>();
        this.energySimSubjectsMap = new HashMap<>();

        // get simulation subjects
        try {
            for (OSHComponent _driver : deviceList) {
                if (_driver instanceof ISimulationSubject) {
                    ISimulationSubject _simSubj = (ISimulationSubject) _driver;

                    //assign the simulation engine
                    _simSubj.setSimulationEngine(this);

                    //assign logger
                    _simSubj.setSimulationActionLogger(simLogger);

                    //add subject
                    this.simSubjectsList.add(_simSubj);

                    //do the same for the HashMap (better direct Access)
                    this.simSubjectsMap.put(_simSubj.getUUID(), _simSubj);
                }
            }
        } catch (Exception ex) {
            throw new SimulationEngineException(ex);
        }


        // get ESC simulation subjects
        try {
            for (OSHComponent _driver : deviceList) {
                if (_driver instanceof IDeviceEnergySubject) {
                    IDeviceEnergySubject _simSubj = (IDeviceEnergySubject) _driver;

                    //add subject
                    this.energySimSubjectsList.add(_simSubj);

                    //do the same for the HashMap (better direct Access)
                    this.energySimSubjectsMap.put(_simSubj.getUUID(), _simSubj);
                }
            }
        } catch (Exception ex) {
            throw new SimulationEngineException(ex);
        }

        this.engineParameters = new OSHParameterCollection();
        this.engineParameters.loadCollection(engineParameters);

        this.entityUUID = entityUUID;
    }


    /**
     * simulate the next timeTick, increment the real-time driver
     *
     */
    @Override
    public void simulateNextTimeTick(long currentTick) {

        //		Map<AncillaryCommodity,AncillaryCommodityState> ancillaryMeterState;
        AncillaryMeterState ancillaryMeterState;

        // #1 EnergySimulation
        ancillaryMeterState = this.energySimulationCore.doNextEnergySimulation(this.energySimSubjectsList);

        // #2 Notify the Subject that the next Simulation Tick begins
        //    Simulation Pre-tick Hook
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.onSimulationPreTickHook();
        }

        // #3 DeviceSimulation (the Tick)
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.triggerSubject();
        }

        // #4 Notify the Subject that the current Simulation Tick ended
        //	  Simulation Post-tick Hook
        for (ISimulationSubject _simSubject : this.simSubjectsList) {
            _simSubject.onSimulationPostTickHook();
        }

        this.comRegistry.publish(AncillaryMeterStateExchange.class, new AncillaryMeterStateExchange(this.entityUUID,
                this.timeDriver.getCurrentTime(), ancillaryMeterState));
    }

    @Override
    protected void notifyLocalEngineOnSimulationIsUp() throws SimulationEngineException {
        try {
            for (ISimulationSubject simulationSubject : this.simSubjectsList) {
                simulationSubject.onSimulationIsUp();
            }
        } catch (SimulationSubjectException ex) {
            throw new SimulationEngineException(ex);
        }

    }


    // ### GETTERS ###

    /**
     * get a simulationSubject by his (device) ID.
     * This can be called from another subject to get an appending subject
     *
     */
    protected ISimulationSubject getSimulationSubjectByID(UUID subjectID) {
        ISimulationSubject _simSubj;
        _simSubj = this.simSubjectsMap.get(subjectID);
        return _simSubj;
    }
}
