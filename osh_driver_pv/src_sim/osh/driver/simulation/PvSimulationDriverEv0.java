package osh.driver.simulation;

import osh.configuration.OSHParameterCollection;
import osh.core.interfaces.IOSH;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.driver.simulation.pv.PvProfile;
import osh.eal.hal.exchange.HALControllerExchange;
import osh.hal.exchange.PvControllerExchange;
import osh.hal.exchange.PvObserverExchange;
import osh.hal.exchange.PvPredictionExchange;
import osh.simulation.DeviceSimulationDriver;
import osh.simulation.exception.SimulationSubjectException;
import osh.simulation.screenplay.SubjectAction;
import osh.utils.physics.ComplexPowerUtil;
import osh.utils.time.TimeConversion;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class PvSimulationDriverEv0 extends DeviceSimulationDriver {

    private boolean pvSwitchedOn;
    private int reactivePowerTarget;

    private int pastDaysPrediction;

    /**
     * nominal power of PV (e.g. 4.6kWpeak)
     */
    private int nominalPower;
    /**
     * according to inverter (technical, due to VAmax)
     */
    private int complexPowerMax;
    @SuppressWarnings("unused")
    private int reactivePowerMax;
    /**
     * according to inverter (technical, due to cosPhi: e.g. 0.8ind...0.8cap)
     */
    private double cosPhiMax;

    private PvProfile profile;

    /**
     * CONSTRUCTOR
     *
     * @throws Exception
     */
    public PvSimulationDriverEv0(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig)
            throws Exception {
        super(osh, deviceID, driverConfig);

        this.pvSwitchedOn = true;
        this.reactivePowerTarget = 0;

        String profileSourceName = driverConfig.getParameter("profilesource");
        this.nominalPower = Integer.parseInt(driverConfig.getParameter("nominalpower"));
        this.complexPowerMax = Integer.parseInt(driverConfig.getParameter("complexpowermax"));
        this.cosPhiMax = Double.parseDouble(driverConfig.getParameter("cosphimax"));

        try {
            this.pastDaysPrediction = Integer.parseInt(driverConfig.getParameter("pastDaysPrediction"));
        } catch (Exception e) {
            this.pastDaysPrediction = 14;
            this.getGlobalLogger().logWarning("Can't get pastDaysPrediction, using the default value: " + this.pastDaysPrediction);
        }

        this.profile = new PvProfile(profileSourceName, this.nominalPower);
        this.reactivePowerMax =
                (int) ComplexPowerUtil.convertComplexToReactivePower(
                        this.complexPowerMax, this.cosPhiMax, true);
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
                this.reactivePowerTarget = 0;
            }
        }
    }

    @Override
    public void onSimulationIsUp() throws SimulationSubjectException {
        super.onSimulationIsUp();
        //initially give LocalObserver load data of past days
        long startTime = this.getTimeDriver().getUnixTimeAtStart();

        List<SparseLoadProfile> predictions = new LinkedList<>();

        ZonedDateTime current = TimeConversion.convertUnixTimeToZonedDateTime(startTime);

        //starting in reverse so that the oldest profile is at index 0 in the list
        for (int i = this.pastDaysPrediction; i >= 1; i--) {

            ZonedDateTime pastDay = current.minusDays(i);
            SparseLoadProfile prof = this.profile.getProfileForDayOfYear(pastDay.toLocalDate()).getProfileWithoutDuplicateValues();
            try {
                Long nextLoadChange = 0L;
                while (nextLoadChange != null) {
                    int firstLoad = prof.getLoadAt(Commodity.ACTIVEPOWER, nextLoadChange);
                    double newCosPhi;
                    int reactivePower = 0;
                    try {
                        newCosPhi = ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(
                                firstLoad, this.reactivePowerTarget);

                        if (newCosPhi > this.cosPhiMax) {
                            reactivePower = (int) ComplexPowerUtil.convertActiveToReactivePower(
                                    firstLoad,
                                    this.cosPhiMax,
                                    (this.reactivePowerTarget >= 0));
                        }
                    } catch (Exception ignored) {

                    }

                    prof.setLoad(Commodity.REACTIVEPOWER, nextLoadChange, reactivePower);
                    nextLoadChange = prof.getNextLoadChange(Commodity.ACTIVEPOWER, nextLoadChange);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            predictions.add(prof);
        }

        PvPredictionExchange _ox = new PvPredictionExchange(this.getUUID(), this.getTimeDriver().getUnixTime(), predictions, this.pastDaysPrediction);
        this.notifyObserver(_ox);
    }

    @Override
    public void onNextTimeTick() {
        if (this.pvSwitchedOn) {
            long now = this.getTimeDriver().getUnixTime();
            this.setPower(Commodity.ACTIVEPOWER, this.profile.getPowerAt(now));

            if (this.getPower(Commodity.ACTIVEPOWER) != 0) {
                double newCosPhi;
                try {
                    newCosPhi = ComplexPowerUtil.convertActiveAndReactivePowerToCosPhi(
                            this.getPower(Commodity.ACTIVEPOWER), this.reactivePowerTarget);

                    if (newCosPhi > this.cosPhiMax) {
                        this.setPower(Commodity.REACTIVEPOWER, (int) ComplexPowerUtil.convertActiveToReactivePower(
                                this.getPower(Commodity.ACTIVEPOWER),
                                this.cosPhiMax,
                                (this.reactivePowerTarget >= 0)));
                    } else if (newCosPhi < -1) {
                        this.setPower(Commodity.REACTIVEPOWER, 0);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                this.setPower(Commodity.REACTIVEPOWER, 0);
            }
        }

        PvObserverExchange _ox = new PvObserverExchange(this.getUUID(), this.getTimeDriver().getUnixTime());
        _ox.setActivePower(this.getPower(Commodity.ACTIVEPOWER));
        _ox.setReactivePower(this.getPower(Commodity.REACTIVEPOWER));
        this.notifyObserver(_ox);
    }

    @Override
    public void performNextAction(SubjectAction nextAction) {
        //NOTHING
    }
}
