package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.pv.PvProfileHollSingleton;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.hal.exchange.PvControllerExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.time.TimeConversion;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

//import osh.hal.exchange.PvHALStaticDetailsExchange;

/**
 * @author Ingo Mauser
 */
public class PvSimulationDriverHollData extends DeviceSimulationDriver {

    /**
     * nominal power of PV (e.g. 4.6 kWpeak)
     */
    private final int nominalPower;
    /**
     * according to inverter (technical, due to VAmax) S = SQR(P^2+Q^2)
     */
    @SuppressWarnings("unused")
    private final int complexPowerMax;
    private boolean pvSwitchedOn;
    private String pathToFiles;
    private String fileExtension;
    private double profileNominalPower;
    private int pastDaysPrediction;
    /**
     * according to inverter (technical, due to cosPhi: e.g. 0.8ind...0.8cap)
     */
    private double cosPhiMax;

    private PvProfileHollSingleton profile;

    /**
     * CONSTRUCTOR
     *
     * @throws Exception
     */
    public PvSimulationDriverHollData(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws Exception {
        super(osh, deviceID, driverConfig);

        this.pvSwitchedOn = true;

//		String profileSourceName = driverConfig.getParameter("profilesource");
        this.nominalPower = Integer.parseInt(driverConfig.getParameter("nominalpower"));
        this.complexPowerMax = Integer.parseInt(driverConfig.getParameter("complexpowermax"));
        this.cosPhiMax = Double.parseDouble(driverConfig.getParameter("cosphimax"));

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
            this.pathToFiles = "configfiles/pv/holl2013cleaned";
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
            this.profileNominalPower = 5307.48;
            this.getGlobalLogger().logWarning("Can't get profileNominalPower, using the default value: " + this.profileNominalPower);
        }
        // load profile
        this.profile = new PvProfileHollSingleton(
                this.nominalPower, this.pathToFiles + this.fileExtension, this.profileNominalPower, this.cosPhiMax);

        //TODO adapt profile (power, heading), after making it singleton...

    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();
        //initially give LocalObserver load data of past days
        long startTime = this.getTimeDriver().getTimeAtStart().toEpochSecond();

        List<SparseLoadProfile> predictions = new LinkedList<>();

        int dayOfYear = TimeConversion.convertUnixTime2CorrectedDayOfYear(startTime);


        //starting in reverse so that the oldest profile is at index 0 in the list
        for (int i = this.pastDaysPrediction; i >= 1; i--) {

            int pastDay = Math.floorMod(dayOfYear - i, 365);
            predictions.add(this.profile.getPowerForDay(pastDay));
        }

        PvPredictionExchange _ox = new PvPredictionExchange(this.getUUID(), this.getTimeDriver().getCurrentTime(), predictions,
                this.pastDaysPrediction);
        this.notifyObserver(_ox);
    }


    @Override
    public void onNextTimeTick() {

        long now = this.getTimeDriver().getCurrentEpochSecond();

        if (this.pvSwitchedOn && this.getTimeDriver().getCurrentTimeEvents().contains(TimeSubscribeEnum.MINUTE)) {

            this.setPower(
                    Commodity.ACTIVEPOWER,
                    this.profile.getPowerAt(now));

            try {
                this.setPower(
                        Commodity.REACTIVEPOWER,
                        (int) ComplexPowerUtil.convertActiveToReactivePower(
                                this.getPower(Commodity.ACTIVEPOWER),
                                this.cosPhiMax,
                                true));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        PvObserverExchange _ox = new PvObserverExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentTime());
        _ox.setActivePower(this.getPower(Commodity.ACTIVEPOWER));
        _ox.setReactivePower(this.getPower(Commodity.REACTIVEPOWER));
        this.notifyObserver(_ox);
    }

    @Override
    protected void onControllerRequest(HALControllerExchange controllerRequest) {
        PvControllerExchange controllerExchange = (PvControllerExchange) controllerRequest;
        Boolean newPvSwitchedOn = controllerExchange.getNewPvSwitchedOn();

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
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
