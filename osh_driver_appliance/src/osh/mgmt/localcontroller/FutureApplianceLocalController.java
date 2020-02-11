package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.appliance.future.ApplianceProgramConfigurationStatus;
import osh.datatypes.ea.Schedule;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.FutureApplianceControllerExchange;
import osh.mgmt.localcontroller.ipp.FutureApplianceIPP;
import osh.mgmt.localcontroller.ipp.FutureAppliancesStaticIPP;
import osh.mgmt.localcontroller.ipp.GenericApplianceSolution;
import osh.mgmt.mox.GenericApplianceMOX;
import osh.registry.interfaces.IDataRegistryListener;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Ingo Mauser
 */
public class FutureApplianceLocalController
        extends LocalController
        implements IDataRegistryListener {

    /**
     * State of the device as EN 50523 state
     */
    private EN50523DeviceState currentState;

    /**
     * initially: ACP = null<br>
     * otherwise: current ACP
     */
    private ApplianceProgramConfigurationStatus acp;

    // TDoF variables
    /**
     * Never change this by hand, use setDof()
     */
    private Duration current1stTemporalDof = Duration.ZERO;

    /**
     * Never change this by hand, use setDof()
     */
    private Duration max2ndTemporalDof = Duration.ZERO; // currently not in use

    /**
     * indicates whether tDoF has been changed by the user (e.g., using the GUI)
     */
    private boolean tDOFChanged;

    // scheduling variables
    private UUID acpIDForScheduledTimes;
    private ZonedDateTime earliestStartingTime;
    private ZonedDateTime latestStartingTime;
    private Duration originalMaxDuration;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public FutureApplianceLocalController(IOSHOC osh) {
        super(osh);
        //currently NOTHING
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();
        // register to be called every time step
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
        // register for solutions of the optimization
        this.getOCRegistry().subscribe(EASolutionCommandExchange.class, this.getUUID(),this);
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        // check whether there is something new...
        // ...and update MOX object for controller
        this.updateMox();
    }

    /**
     * Update MOX
     */
    private void updateMox() {
        //
        // is new IPP necessary? default: no!
        boolean triggerNewIPP = false;
        // is rescheduling necessary? default: no!
        boolean triggerScheduling = false;
        // get new MOX
        GenericApplianceMOX mox = (GenericApplianceMOX) this.getDataFromLocalObserver();
        // compression may have changed
        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();

//		getGlobalLogger().logDebug("getAcpReferenceTime "+mox.getAcpReferenceTime());
//		getGlobalLogger().logDebug("getAcpID "+mox.getAcpID());
//		getGlobalLogger().logDebug("getAcp "+mox.getAcp());
//		getGlobalLogger().logDebug("getCurrentState "+mox.getCurrentState());
//		getGlobalLogger().logDebug("getDof "+mox.getDof());

        // check whether device has been set to OFF
        if (mox.getCurrentState() == EN50523DeviceState.OFF && (this.currentState == null || this.currentState != EN50523DeviceState.OFF)) {

            // #1 initial OFF...
            // #2 or appliance is set OFF and has been scheduled before...
            // #3 or device is new...

            triggerScheduling = this.currentState != null;
            // get state
            this.currentState = mox.getCurrentState();
            // just update IPP
            triggerNewIPP = true;
            // reset ACP
            this.acp = null;
            // reset scheduling  variables
            this.acpIDForScheduledTimes = null;
            this.earliestStartingTime = null;
            this.latestStartingTime = null;
            this.originalMaxDuration = null;
        } else if (mox.getCurrentState() == EN50523DeviceState.ENDPROGRAMMED && (this.currentState == null || this.currentState != EN50523DeviceState.ENDPROGRAMMED)) {
            // device is finished (waiting for being switched off)
            //
            // get state
            this.currentState = mox.getCurrentState();
            // just update IPP
            triggerNewIPP = true;
            triggerScheduling = false;
            // reset ACP
            this.acp = null;
            // reset scheduling  variables
            this.acpIDForScheduledTimes = null;
            this.earliestStartingTime = null;
            this.latestStartingTime = null;
            this.originalMaxDuration = null;
        } else if (mox.getDof() != null && (this.current1stTemporalDof.minus(mox.getDof()).abs().getSeconds() >= 10)) {
            this.currentState = mox.getCurrentState();
            this.setTemporalDof(mox.getDof(), null);
        } else if ((mox.getCurrentState() == EN50523DeviceState.OFF && this.currentState == EN50523DeviceState.OFF)
                || (mox.getCurrentState() == EN50523DeviceState.PROGRAMMED && this.currentState == EN50523DeviceState.PROGRAMMED)
                || (mox.getCurrentState() == EN50523DeviceState.ENDPROGRAMMED && this.currentState == EN50523DeviceState.ENDPROGRAMMED)) {
            // device still OFF
            // device still PROGRAMMED
            // device still ENDPOGRAMMED
            // ...do nothing!!!


        }
        // check whether there is a new ACP...
        else if (mox.getAcp() != null) {
            // got new ACP...
            // i.e., something has changed
            // #1 new configuration or
            // #2 new phase

            // get state
            this.currentState = mox.getCurrentState();

            // safety check only trigger rescheduling if acp has changed or acp has not been scheduled
            if (this.acpIDForScheduledTimes == null || !this.acpIDForScheduledTimes.equals(mox.getAcpID())) {
                // new ACP -> device has been (re-)configured (changed program, or set off, or ...)
                this.acp = mox.getAcp();

                // reset scheduling  variables
                this.acpIDForScheduledTimes = null;
                //earliestStartingTime = null; // NO!
                //latestStartingTime = null; // NO!

                if (!this.acp.isDoNotReschedule()) {
                    // trigger scheduling
                    triggerNewIPP = true;
                    triggerScheduling = true;
                }
            }
        }
        // device ON, but no new ACP
        else {
            // mox.getAcp() == null
            // no new ACP, check whether ACP available
            if (this.acp != null) {
                // ACP is available
                if (this.currentState != mox.getCurrentState()) {
                    this.getGlobalLogger().logDebug("ERROR: state changed without new ACP! should NOT happen!");
                }
                if (this.acp.getAcpID() != mox.getAcpID()) {
                    // acpID changed without new ACP!
                    // should NOT happen!
                    this.getGlobalLogger().logDebug("ERROR: acpID changed without new ACP! should NOT happen!");
                }

                // nothing changed, device is running...
            } else {
                this.getGlobalLogger().logDebug("ERROR: should not happen. Device ON, but no new ACP = null");
            }
        }
        // check whether to update IPP
        if (triggerScheduling || this.tDOFChanged || triggerNewIPP) {
            //  update of IPP...
            this.updateIPP(triggerScheduling);
        }
    }

    /**
     * called if:
     * 1. new tDoF<br>
     * 2. triggered by MOX<br>
     * 2.1. new ACP (new configuration or new phase)<br>
     * 2.2. switched OFF or ENDPROGRAMMED<br>
     */
    protected void updateIPP(boolean toBeScheduled) {

        InterdependentProblemPart<?, ?> ipp = null;
        ZonedDateTime now = this.getTimeDriver().getCurrentTime();

        // no profile to be scheduled...and no profile has been scheduled...
        if (this.acp == null && this.acpIDForScheduledTimes == null) {
            // OFF or ENDPROGRAMMED
            if (this.currentState != EN50523DeviceState.OFF && this.currentState != EN50523DeviceState.ENDPROGRAMMED) {
                this.getGlobalLogger().logDebug("ERROR: should not happen. Device is OFF or ENDPROGRAMMED ");
            }
//			if (toBeScheduled) {
            // IPP for OFF or ENDPROGAMMED
            ipp = new FutureAppliancesStaticIPP(
                    this.getUUID(),
                    now,
                    toBeScheduled,
                    this.getLocalObserver().getDeviceType(),
                    new Schedule(new SparseLoadProfile(), 0.0, this.getLocalObserver().getDeviceType().toString()),
                    this.compressionType,
                    this.compressionValue);
//			}

        } else if (this.acp == null) {
            this.getGlobalLogger().logDebug("ERROR: should NOT happen! acp == null");
        }
        // got ACP, but without DLP -> should NOT happen
        else if (this.acp.getDynamicLoadProfiles() == null) {
            this.getGlobalLogger().logDebug("ERROR: acp != null && acp.getDynamicLoadProfiles() == null");
        }
        // there is probably something to be scheduled...
        else {

            // should NOT be OFF or ENDPROGRAMMED
            if (this.currentState == EN50523DeviceState.OFF || this.currentState == EN50523DeviceState.ENDPROGRAMMED) {
                this.getGlobalLogger().logDebug("ERROR: should not happen. Device is OFF or ENDPROGRAMMED but something wants to be scheduled");
            }

            // check whether ACP has already been scheduled
            if (this.acpIDForScheduledTimes == null) {
                // new ACP

                //TODO NO new phase within one configuration) from appliance (has not been scheduled, yet)

                // triggered by MoOX (resets acpIDForTimes and acpIDforScheduledTimes to NULL)

                // reset earliest starting time
                this.earliestStartingTime = now;

                if (this.currentState == EN50523DeviceState.PROGRAMMED) {
                    //OK
                    this.latestStartingTime = now.plus(this.current1stTemporalDof);
                    this.originalMaxDuration =
                            Duration.ofSeconds(ApplianceProgramConfigurationStatus.getTotalMaxDuration(this.acp));
//					getGlobalLogger().logDebug("Setting new acp: " + originalMaxduration);
                } else if (this.currentState == EN50523DeviceState.RUNNING) {
                    // keep latest starting time
                } else {
                    this.getGlobalLogger().logDebug("ERROR: should not happen. acpIDforScheduledTimes == null but device ist not PROGRAMMED or RUNNING");
                }

                this.acpIDForScheduledTimes = this.acp.getAcpID();
            } else {
                // some ACP has already been scheduled...
                // check whether it was the current ACP...
                if (this.acpIDForScheduledTimes.equals(this.acp.getAcpID())) {
                    // current ACP
                    if (this.tDOFChanged) {
                        // triggered by setTemporalDoF
                        // rescheduling of old one with new tDoF (dofChanged)
                        // reset earliest and latest starting times according to 1st tDoF
                        this.earliestStartingTime = now;
                        this.latestStartingTime = now.plus(this.current1stTemporalDof);
                    } else {
                        this.getGlobalLogger().logDebug("ERROR: should not happen (acpID has already been scheduled...no new TDOF...why reschedule?!?)");
                    }
                } else {
                    // other ACP
                    this.getGlobalLogger().logDebug("ERROR: should not happen acpIDforScheduledTimes unequals to acp.getAcpID() ");
                }
            }

            // determine longest profile of alternatives (with minimum times of phases)
            Duration maxDuration = Duration.ofSeconds(ApplianceProgramConfigurationStatus.getTotalMaxDuration(this.acp));

            // determine absolute optimization horizon (absolute time!)
            if (this.latestStartingTime == null) {
                this.latestStartingTime = now;
            }

            //for interruptible devices, we need to correct the LST
            if (this.originalMaxDuration.compareTo(maxDuration) > 0) {
//				getGlobalLogger().logDebug("Correcting LST by " + (originalMaxduration - maxDuration));
                this.latestStartingTime = this.latestStartingTime.plus(this.originalMaxDuration.minus(maxDuration));
                // correcting old maxDuration so that the next phase does not cause to much correction
                this.originalMaxDuration = maxDuration;

            } else if (this.originalMaxDuration.compareTo(maxDuration) < 0) {
                this.getGlobalLogger().logError("New Maxduration bigger than older duration, this should not happen");
            }

            ZonedDateTime optimizationHorizon =
                    now.plus(Duration.between(this.earliestStartingTime, this.latestStartingTime).plus(maxDuration));

            //ensure that hybrid devices have a long horizon
            if (this.acp.getDynamicLoadProfiles().length > 1) {
                if (now.plusDays(1).isAfter(optimizationHorizon)) optimizationHorizon = now.plusDays(1);
            }

//			getGlobalLogger().logDebug("EST: " + earliestStartingTime);
//			getGlobalLogger().logDebug("LST: " + latestStartingTime);
//			getGlobalLogger().logDebug("maxDur: " + maxDuration);
//			getGlobalLogger().logDebug("New horizon: " + optimizationHorizon);

            // construct EAPart
            ipp = new FutureApplianceIPP(
                    this.getUUID(),
                    now,
                    toBeScheduled,
                    optimizationHorizon.toEpochSecond(),
                    this.earliestStartingTime.toEpochSecond(),
                    this.latestStartingTime.toEpochSecond(),
                    this.acp,
                    this.getLocalObserver().getDeviceType(),
                    this.compressionType,
                    this.compressionValue);

        }

        // reset tDOF, because it is scheduled...
        this.tDOFChanged = false; //reset

        // send EAP to Global O/C-unit
        if (ipp != null) {
//			if (toBeScheduled && (now - lastTimeSchedulingCaused) > 60) {
//				lastTimeSchedulingCaused = now;
//				this.getOCRegistry().setState(InterdependentProblemPart.class, this, ipp);
//			} else if (!toBeScheduled) {
            this.getOCRegistry().publish(InterdependentProblemPart.class, this, ipp);
//			}
        }
    }


    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        // EAP Solution
        if (exchange instanceof EASolutionCommandExchange) {
            try {
                @SuppressWarnings("unchecked")
                EASolutionCommandExchange<GenericApplianceSolution> solution =
                        (EASolutionCommandExchange<GenericApplianceSolution>) exchange;

                // must be for somebody else... or solution is empty...
                if (!solution.getReceiver().equals(this.getUUID())
                        || solution.getPhenotype() == null) {
                    return;
                }

                try {
                    // solution is for old ACP
                    if (this.acp == null || solution.getPhenotype().acpUUID != this.acp.getAcpID()) {
                        this.getGlobalLogger().logDebug("received invalid solution: was for old ACP");
                        return;
                    }
                } catch (NullPointerException e) {
                    this.getGlobalLogger().logDebug(e);
                }

                // ### transform solution to phenotype ###

                // get selected profile id (e.g. hybrid or normal)
                int selectedProfileId = solution.getPhenotype().profileId;

                //should never be NULL...
                if (this.acp != null) {
                    FutureApplianceControllerExchange cx = new FutureApplianceControllerExchange(
                            this.getUUID(),
                            this.getTimeDriver().getCurrentTime(),
                            this.acp.getAcpID(),
                            selectedProfileId,
                            solution.getPhenotype().getZonedStartingTimes());
                    // send CX to driver
                    this.updateOcDataSubscriber(cx);
                } else {
                    // received command although no ACP available...
                    this.getGlobalLogger().logDebug("acp == null");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Use this method to set new tDoF
     */
    protected void setTemporalDof(Duration firstTemporalDof, Duration secondTemporalDof) {
        if ((firstTemporalDof != null && firstTemporalDof.isNegative())
                || (secondTemporalDof != null && secondTemporalDof.isNegative())) {
            throw new IllegalArgumentException("firstDof or secondDof < 0");
        }

        if (firstTemporalDof == null && secondTemporalDof == null) {
            throw new IllegalArgumentException("firstDof and secondDof == null");
        }

        if (firstTemporalDof != null && !this.current1stTemporalDof.equals(firstTemporalDof)) {
            this.current1stTemporalDof = firstTemporalDof;
            this.tDOFChanged = true;
        }

        if (secondTemporalDof != null && !this.max2ndTemporalDof.equals(secondTemporalDof)) {
            this.max2ndTemporalDof = secondTemporalDof;
            this.tDOFChanged = true;
        }

        if (this.tDOFChanged) {
            // trigger rescheduling if this happens
            if (this.currentState != EN50523DeviceState.OFF && (this.currentState != EN50523DeviceState.PROGRAMMED || this.acp != null))
                this.updateIPP(true);
        }
    }
}
