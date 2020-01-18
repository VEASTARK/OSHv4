package osh.simulation;

import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;

import java.util.Collection;
import java.util.UUID;

/**
 * @author Florian Allerding, Ingo Mauser
 */
public interface ISimulationSubject {

    /**
     * is invoked when the complete simulation environment has been set up
     */
    void onSimulationIsUp() throws SimulationSubjectException;

    /**
     * is invoked before every simulation tick by the simulation engine
     */
    void onSimulationPreTickHook();

    /**
     * is invoked by the SimulationEngine on every time tick to synchronize the subjects<br>
     * 1. trigger onNextTimeTick()<br>
     * 2. do action handling
     */
    void triggerSubject();

    /**
     * is invoked after every simulation tick by the simulation engine
     */
    void onSimulationPostTickHook();


    // ### ACTION related ###

    /**
     * delete all actions from the list
     */
    void flushActions();

    /**
     * Sets an action for this simulation subject
     *
     */
    void setAction(SubjectAction action);

    /**
     * gets all actions for a subject
     *
     * @return
     */
    Collection<SubjectAction> getActions();

    /**
     * @param nextAction is invoked when the subject has to do the action "nextAction"
     */
    void performNextAction(SubjectAction nextAction);


    // ### GETTERS and SETTERS ###

    void setSimulationEngine(BuildingSimulationEngine simulationEngine);

    UUID getDeviceID();

    ISimulationSubject getAppendingSubject(UUID SubjectID);

    void setSimulationActionLogger(ISimulationActionLogger simulationLogger);

}
