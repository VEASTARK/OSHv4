package constructsimulation.generation;

import constructsimulation.data.OldSimOSHConfigurationData;
import constructsimulation.datatypes.*;
import constructsimulation.generation.device.CreateComDevice;
import constructsimulation.generation.device.CreateDevice;
import constructsimulation.generation.parameter.CreateConfigurationParameter;
import osh.configuration.cal.AssignedComDevice;
import osh.configuration.cal.CALConfiguration;
import osh.configuration.eal.AssignedDevice;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.*;
import osh.simulation.screenplay.*;

import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class ConstructSimulationHelper {

    /**
     * @param eALConfigurationData
     * @param oCConfigurationData
     * @param screenplayData
     * @return
     */
    public static SimulationConfigurationWrapper constructSimulation(
            EALConfigurationWrapper eALConfigurationData,
            CALConfigurationWrapper cALConfigurationData,
            OCConfigurationWrapper oCConfigurationData,
            OSHConfigurationWrapper oSHConfigurationWrapper,
            ScreenplayWrapper screenplayData) {

        /* # EALConfiguration # */
        EALConfiguration ealConfig = constructEALConfiguration(eALConfigurationData);

        /* # OCConfiguration # */
        OCConfiguration ocConfig = constructOCConfiguration(oCConfigurationData);

        /* # CALConfiguration # */
        CALConfiguration calConfig = constructCALConfiguration(cALConfigurationData);

        /* # CALConfiguration # */
        OSHConfiguration oshConfig = constructOSHConfiguration(oSHConfigurationWrapper);

        /* # Screenplay # */
        Screenplay screenplay = constructScreenplay(screenplayData);

        return new SimulationConfigurationWrapper(ocConfig, ealConfig, calConfig, oshConfig, screenplay);
    }

    /**
     * # EALConfiguration #<br>
     *
     * @param ealConfigurationData
     * @return
     */
    private static EALConfiguration constructEALConfiguration(
            EALConfigurationWrapper ealConfigurationData) {

        return new EALConfiguration();
    }

    /**
     * # ControllerBoxConfiguration #
     *
     * @param ocConfigurationData
     * @return
     */
    private static OCConfiguration constructOCConfiguration(
            OCConfigurationWrapper ocConfigurationData) {
        OCConfiguration ocConfig = new OCConfiguration();

        String globalObserverClassName = ocConfigurationData.globalObserverClassName;
        String globalControllerClassName = ocConfigurationData.globalControllerClassName;

        ocConfig.setGlobalObserverClass(globalObserverClassName);
        ocConfig.setGlobalControllerClass(globalControllerClassName);
        ocConfig.setGlobalOcUuid(OldSimOSHConfigurationData.globalOCUnitUUID.toString());
        ocConfig.setGaConfiguration(ocConfigurationData.gaConfiguration);

        // ### PARAMETERS ###

        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "epsoptimizationobjective",
                            "String",
                            "" + ocConfigurationData.epsOptimizationObjective);
            ocConfig.getGlobalControllerParameters().add(cp);
        }

        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "plsoptimizationobjective",
                            "String",
                            "" + ocConfigurationData.plsOptimizationObjective);
            ocConfig.getGlobalControllerParameters().add(cp);
        }

        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "varoptimizationobjective",
                            "String",
                            "" + ocConfigurationData.varOptimizationObjective);
            ocConfig.getGlobalControllerParameters().add(cp);
        }
        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "upperOverlimitFactor",
                            "String",
                            "" + ocConfigurationData.upperOverlimitFactor);
            ocConfig.getGlobalControllerParameters().add(cp);
        }
        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "lowerOverlimitFactor",
                            "String",
                            "" + ocConfigurationData.lowerOverlimitFactor);
            ocConfig.getGlobalControllerParameters().add(cp);
        }
        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "stepSize",
                            "String",
                            "" + ocConfigurationData.stepSize);
            ocConfig.getGlobalControllerParameters().add(cp);
        }

        {
            ConfigurationParameter cp =
                    CreateConfigurationParameter.createConfigurationParameter(
                            "hotWaterTankUUID",
                            "String",
                            "" + ocConfigurationData.hotWaterTankUUID);
            ocConfig.getGlobalControllerParameters().add(cp);
        }

        return ocConfig;
    }

    /**
     * # CALConfiguration #<br>
     *
     * @param cALConfigurationData
     * @return
     */
    private static CALConfiguration constructCALConfiguration(
            CALConfigurationWrapper cALConfigurationData) {
        CALConfiguration calConfig = new CALConfiguration();

        //add communication device User GUI
        if (cALConfigurationData.showGui) {
            AssignedComDevice guiComDevice = CreateComDevice.createComDevice(
                    ComDeviceTypes.GUI,
                    OldSimOSHConfigurationData.comDeviceIdGui,
                    cALConfigurationData.guiComDriverClassName,
                    cALConfigurationData.guiComManagerClassName);
            calConfig.getAssignedComDevices().add(guiComDevice);
        }

        return calConfig;
    }

    /**
     * # OSHConfiguration #<br>
     *
     * @return
     */
    private static OSHConfiguration constructOSHConfiguration(
            OSHConfigurationWrapper oshConfigurationData) {
        OSHConfiguration oshConfig = new OSHConfiguration();

        oshConfig.setRandomSeed(oshConfigurationData.mainRandomSeed + "");

        oshConfig.setLogFilePath(oshConfigurationData.logPath);

        oshConfig.setMeterUUID(oshConfigurationData.meterUUID.toString());
        oshConfig.setHhUUID(oshConfigurationData.hhUUID.toString());

        return oshConfig;

    }

    /**
     * # Screenplay #
     *
     * @param screenplayData
     * @return
     */
    private static Screenplay constructScreenplay(
            ScreenplayWrapper screenplayData) {
        Screenplay screenplay = new Screenplay();
        ScreenplayType screenplayType = screenplayData.screenplayType;
        if (screenplayType == ScreenplayType.DYNAMIC) {
            // Dynamic screenplay
            //Do NOT generate the price signal (it will be created dynamically)
        } else {
            System.out.println("[ERROR] Incorrect screenplayType");
            return null;
        }
        return screenplay;
    }


    /* ##################
     * # HELPER METHODS #
     * ################## */


    public static AssignedDevice createHouseHoldDevice(
            DeviceTypes deviceType,
            DeviceClassification classification,
            UUID deviceId,
            String driverClassName,
            String localObserverClass,
            boolean isControllable,
            String localControllerClass) {

        return CreateDevice.createDevice(
                deviceType,
                classification,
                deviceId,
                driverClassName,
                localObserverClass,
                isControllable,
                localControllerClass);
    }


    public static void generatePowerLimitSignal(PerformAction performAction) {

        ActionParameters actionParameters = new ActionParameters();
        actionParameters.setParametersName("PowerLimitSignal");
        for (int i = 0; i < 1440; i++) {
            ActionParameter parameter = new ActionParameter();
            parameter.setName(Integer.toString(i));
            parameter.setValue("" + 3000.0);
            actionParameters.getParameter().add(parameter);
        }

        performAction.getActionParameterCollection().add(actionParameters);
    }


    public static void generateDofActions(
            Screenplay myScreenplay,
            UUID commDeviceIdDoF,
            UUID[] deviceIds,
            String[] applianceDoF,
            String[] appliance2ndDoF) {

        //loading actions for the Dof-Device
        SubjectAction dofAction = new SubjectAction();
        dofAction.setActionType(ActionType.USER_ACTION);
        dofAction.setDeviceID(commDeviceIdDoF.toString());
        dofAction.setTick(0);

        //dof for each device
        PerformAction dofPerformAction = new PerformAction();
        ActionParameters dofParameters = new ActionParameters();
        dofParameters.setParametersName("dof");
        for (int i = 0; i < deviceIds.length; i++) {
            ActionParameter parameter = new ActionParameter();
            parameter.setName(deviceIds[i].toString());
            parameter.setValue(applianceDoF[i] + ";" + appliance2ndDoF[i]);
            dofParameters.getParameter().add(parameter);
        }

        dofPerformAction.getActionParameterCollection().add(dofParameters);
        dofAction.getPerformAction().add(dofPerformAction);

        myScreenplay.getSIMActions().add(dofAction);
    }

}
