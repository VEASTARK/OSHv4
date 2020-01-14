package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.csv.CSVImporter;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class PvSimulationDriverESHLData extends DeviceSimulationDriver {

    private static final String delimiter = ";";
    private String pathToFiles;
    private String fileExtension;
    private double profileNominalPower;
    private int pastDaysPrediction;
    private boolean pvSwitchedOn;

    /**
     * nominal power of PV (e.g. 4.6kWpeak)
     */
    private int nominalPower;

    private double scalingFactor;

    private int[][] currentDayProfile;


    public PvSimulationDriverESHLData(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws Exception {
        super(osh, deviceID, driverConfig);
        this.pvSwitchedOn = true;

        this.nominalPower = Integer.parseInt(driverConfig.getParameter("nominalpower"));

        try {
            this.pastDaysPrediction = Integer.parseInt(driverConfig.getParameter("pastDaysPrediction"));
        } catch (Exception e) {
            this.pastDaysPrediction = 14;
            this.getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
        }

        try {
            this.pathToFiles = driverConfig.getParameter("pathToFiles");
            if (this.pathToFiles == null) throw new IllegalArgumentException();
        } catch (Exception e) {
            this.pathToFiles = "configfiles/pv/eshl/1s/cleaned_20112012_";
            this.getGlobalLogger().logWarning("Can't get pathToFiles, using the default value: " + this.pathToFiles);
        }

        try {
            this.fileExtension = driverConfig.getParameter("fileExtension");
            if (this.fileExtension == null) throw new IllegalArgumentException();
        } catch (Exception e) {
            this.fileExtension = ".csv";
            this.getGlobalLogger().logWarning("Can't get fileExtension, using the default value: " + this.fileExtension);
        }

        try {
            this.profileNominalPower = Double.parseDouble(driverConfig.getParameter("profileNominalPower"));
        } catch (Exception e) {
            this.profileNominalPower = -4600.0;
            this.getGlobalLogger().logWarning("Can't get profileNominalPower, using the default value: " + this.profileNominalPower);
        }

        this.scalingFactor = this.nominalPower / this.profileNominalPower;
    }

    private static int[][] loadProfile(String fileName) {
        return CSVImporter.readInteger2DimArrayFromFile(fileName, delimiter, "\"");
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        PvControllerExchange controllerExchange = (PvControllerExchange) controllerRequest;
        Boolean newPvSwitchedOn = controllerExchange.getNewPvSwitchedOn();
//		Integer newReactivePower = controllerExchange.getNewReactivePower();

        // check whether to switch pv on or off
        if (newPvSwitchedOn != null) {
            this.pvSwitchedOn = newPvSwitchedOn;
            if (!this.pvSwitchedOn) {
                this.setPower(Commodity.ACTIVEPOWER, 0);
                this.setPower(Commodity.REACTIVEPOWER, 0);
            }
        }
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();
        //initially give LocalObserver load data of past days
        long startTime = this.getTimer().getUnixTimeAtStart();

        List<SparseLoadProfile> predictions = new LinkedList<>();

        //starting in reverse so that the oldest profile is at index 0 in the list
        for (int i = this.pastDaysPrediction; i >= 1; i--) {
            int[][] dim2Profile = loadProfile(this.pathToFiles + Math.floorMod((startTime / 86400 - i), 365) + this.fileExtension);
            SparseLoadProfile dayProfile = new SparseLoadProfile();
            for (int sec = 0; sec < dim2Profile.length; sec += 60) {
                dayProfile.setLoad(Commodity.ACTIVEPOWER, sec, (int) this.scalingFactor * dim2Profile[sec][1]);
                dayProfile.setLoad(Commodity.REACTIVEPOWER, sec, 0);
            }
            dayProfile.setEndingTimeOfProfile(86400);
            predictions.add(dayProfile.getProfileWithoutDuplicateValues());
        }

        PvPredictionExchange _ox = new PvPredictionExchange(this.getDeviceID(), this.getTimer().getUnixTime(), predictions, this.pastDaysPrediction);
        this.notifyObserver(_ox);
    }

    @Override
    public void onNextTimeTick() {

        long now = this.getTimer().getUnixTime();

        int seconds = (int) (now % 86400);

        if (seconds == 0) {
            // read next file
            this.currentDayProfile = loadProfile(this.pathToFiles + (now / 86400) + this.fileExtension);
        }

        if (this.pvSwitchedOn) {

            int power = (int) (this.scalingFactor * this.currentDayProfile[seconds][1]);


            this.setPower(Commodity.ACTIVEPOWER, power);
            this.setPower(Commodity.REACTIVEPOWER, 0);
        }

        PvObserverExchange _ox = new PvObserverExchange(this.getDeviceID(), this.getTimer().getUnixTime());
        _ox.setActivePower(this.getPower(Commodity.ACTIVEPOWER));
        _ox.setReactivePower(this.getPower(Commodity.REACTIVEPOWER));

        this.notifyObserver(_ox);
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
