package osh.mgmt.globalcontroller.modules.communication;

import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.globalobserver.GUIAncillaryMeterStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIHotWaterPredictionStateExchange;
import osh.datatypes.registry.oc.state.globalobserver.GUIScheduleStateExchange;
import osh.mgmt.globalcontroller.jmetal.builder.EAScheduleResult;
import osh.mgmt.globalcontroller.modules.GlobalControllerDataStorage;
import osh.mgmt.globalcontroller.modules.GlobalControllerEventEnum;
import osh.mgmt.globalcontroller.modules.GlobalControllerModule;
import osh.mgmt.globalcontroller.modules.scheduling.DetailedOptimisationResults;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Represents the communication of the scheduling results to the GUI.
 *
 * @author Sebastian Kramer
 */
public class CommunicateGUIModule extends GlobalControllerModule {

    private final boolean hasGUI;

    public CommunicateGUIModule(GlobalControllerDataStorage data) {
        super(data);
        this.getData().subscribe(GlobalControllerEventEnum.SCHEDULING_FINISHED, this);

        assert (this.getData().getStatus().hasGUI() || !this.getData().getStatus().isSimulation());

        this.hasGUI = this.getData().getStatus().hasGUI();
    }


    @Override
    public void notifyForEvent(GlobalControllerEventEnum event) {
        if (event == GlobalControllerEventEnum.SCHEDULING_FINISHED) {

            DetailedOptimisationResults optResults = this.getData().getLastOptimisationResults();
            ZonedDateTime now = this.getData().getNow();

            if (optResults != null) {
                EAScheduleResult result = optResults.getScheduleResult();
                List<InterdependentProblemPart<?, ?>> problemparts = this.getData().getProblemParts();

                if (this.hasGUI && !result.isDummySolution()) {
                    this.getData().getOCRegistry().publish(
                            GUIHotWaterPredictionStateExchange.class,
                            new GUIHotWaterPredictionStateExchange(this.getData().getUUID(),
                                    now, result.getPredictedHotWaterTankTemperature(),
                                    result.getPredictedHotWaterDemand(), result.getPredictedHotWaterSupply()));

                    this.getData().getOCRegistry().publish(
                            GUIAncillaryMeterStateExchange.class,
                            new GUIAncillaryMeterStateExchange(this.getData().getUUID(), now,
                                    result.getAncillaryMeter()));

                    //sending schedules last so the wait command has all the other things (waterPred, Ancillarymeter) first
                    // Send current Schedule to GUI (via Registry to Com)
                    this.getData().getOCRegistry().publish(
                            GUIScheduleStateExchange.class,
                            new GUIScheduleStateExchange(this.getData().getUUID(), now, result.getSchedules(),
                                    this.getData().getStepSize()));
                }
            }
        }
    }
}
