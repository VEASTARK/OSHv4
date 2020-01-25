package osh.mgmt.localcontroller;

import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalController;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.commands.globalcontroller.EASolutionCommandExchange;
import osh.datatypes.registry.oc.ipp.InterdependentProblemPart;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.datatypes.registry.oc.state.IAction;
import osh.datatypes.registry.oc.state.LastActionExchange;
import osh.datatypes.registry.oc.state.MieleDofStateExchange;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.en50523.EN50523DeviceState;
import osh.en50523.EN50523OIDExecutionOfACommandCommands;
import osh.hal.exchange.GenericApplianceStartTimesControllerExchange;
import osh.hal.exchange.MieleApplianceControllerExchange;
import osh.mgmt.localobserver.ipp.MieleApplianceIPP;
import osh.mgmt.localobserver.ipp.MieleApplianceNonControllableIPP;
import osh.mgmt.localobserver.ipp.MieleSolution;
import osh.mgmt.localobserver.miele.MieleAction;
import osh.mgmt.mox.MieleApplianceMOX;
import osh.registry.interfaces.IDataRegistryListener;


/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
public class MieleApplianceLocalController
        extends LocalController
        implements IDataRegistryListener {


    /**
     * SparseLoadProfile containing different profile with different commodities<br>
     * IMPORATANT: RELATIVE TIMES!
     */
    private SparseLoadProfile currentProfile;

    private EN50523DeviceState currentState;

    /**
     * use private setter setStartTime()
     */
    private long startTime = -1;

    // used for EA planning
    private final long expectedStartTime = -1;
    private long profileStarted = -1;
    private long programmedAt = -1;

    /**
     * Never change this by hand, use setDof()
     */
    private int firstDof;
    /**
     * Never change this by hand, use setDof()
     */
    private int secondDof;
    /**
     * Never change this by hand, use setDof()
     */
    private long latestStart;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;

    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public MieleApplianceLocalController(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        // register for onNextTimePeriod()
        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);

        this.getOCRegistry().subscribe(EASolutionCommandExchange.class, this.getUUID(), this);
        this.getOCRegistry().subscribe(DofStateExchange.class, this.getUUID(), this);

        //workaround bc this controller may not have this data from the driver->observer chain
        if (this.compressionType == null) {
            this.compressionType = LoadProfileCompressionTypes.DISCONTINUITIES;
            this.compressionValue = 100;
        }
        this.updateIPPExchange();
    }

    private void callDevice() {
        MieleApplianceControllerExchange halControllerExchangeObject
                = new MieleApplianceControllerExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentEpochSecond(),
                EN50523OIDExecutionOfACommandCommands.START);
        this.updateOcDataSubscriber(halControllerExchangeObject);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        if (exchange instanceof EASolutionCommandExchange) {
            @SuppressWarnings("unchecked")
            EASolutionCommandExchange<MieleSolution> solution = (EASolutionCommandExchange<MieleSolution>) exchange;
            if (!solution.getReceiver().equals(this.getUUID()) || solution.getPhenotype() == null) return;
            this.getGlobalLogger().logDebug("getting new starttime: " + solution.getPhenotype().startTime);
            this.setStartTime(solution.getPhenotype().startTime);
            this.setWAMPStartTime(solution.getPhenotype().startTime);
            this.updateDofExchange();

            //System.out.println(getDeviceID() + " got new start time: " + startTime);
        } else if (exchange instanceof DofStateExchange) {
            // 1st DoF and 2nd DoF may be NULL
            // (no DoF for device available yet and the change is because of another device)
            // DoF from Com Manager
            DofStateExchange dse = (DofStateExchange) exchange;
            this.setDof(
                    dse.getDevice1stDegreeOfFreedom(),
                    dse.getDevice2ndDegreeOfFreedom());
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
        long now = exchange.getEpochSecond();

        EN50523DeviceState oldState = this.currentState;
        this.updateMOX();

        if (oldState != this.currentState) {
            this.getGlobalLogger().logDebug(this.getLocalObserver().getDeviceType() + " statechange from: " + oldState + " to : " + this.currentState);
            switch (this.currentState) {
                case PROGRAMMED:
                case PROGRAMMEDWAITINGTOSTART: {
                    this.updateIPPExchange();
                }
                break;
                case RUNNING: {
                    this.setStartTime(this.profileStarted);
                    this.updateIPPExchange();
                }
                break;
                default: {
                    this.startTime = -1;
                }
            }
        }

        if ((this.currentState == EN50523DeviceState.PROGRAMMED
                || this.currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART)
                && this.startTime != -1 && this.startTime <= now) {
            this.callDevice();
        }

//		if (( currentState == EN50523DeviceState.PROGRAMMED 
//				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART )
//				&& getStartTime() == -1) {
//			setStartTime(Long.MAX_VALUE);
//			
//		}		
//		else if ( ( currentState == EN50523DeviceState.PROGRAMMED 
//				|| currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART )
//				&& getStartTime() > 0 ) {
//			if (getStartTime() <= now) { //already to be started?
//				//start device
//				callDevice();
//				setStartTime(-1);
//			}
//		}
//		//IMA @2016-05-13
//		else if ( currentState == EN50523DeviceState.RUNNING
//				&& getStartTime() == -1) {
//			setStartTime(profileStarted);
//			updateIPPExchange();
//		}
//		//IMA @2016-05-13
//		else if ( currentState == EN50523DeviceState.RUNNING
//				&& getStartTime() >= 0) {
////			setStartTime(profileStarted);
////			updateIPPExchange();
//		}
//		else {
//			setStartTime(-1);
//			if (oldState != currentState) { //IMA @2016-05-20
//				updateIPPExchange(); //IMA @2016-05-20
//			}
//			
//		}
    }

    /**
     * Get MOX from Observer
     */
    private void updateMOX() {
        MieleApplianceMOX mox = (MieleApplianceMOX) this.getDataFromLocalObserver();
        this.currentProfile = mox.getCurrentProfile();
        this.currentState = mox.getCurrentState();
        this.profileStarted = mox.getProfileStarted();
        this.programmedAt = mox.getProgrammedAt();
        this.compressionType = mox.getCompressionType();
        this.compressionValue = mox.getCompressionValue();
    }

    private long getStartTime() {
        return this.startTime;
    }

    private void setStartTime(long startTime) {
        this.startTime = startTime;

        this.getOCRegistry().publish(
                ExpectedStartTimeExchange.class,
                this.getUUID(),
                new ExpectedStartTimeExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentEpochSecond(),
                        startTime));

        this.getOCRegistry().publish(
                ExpectedStartTimeChangedExchange.class,
                new ExpectedStartTimeChangedExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentEpochSecond(),
                        startTime));


    }

    private void setWAMPStartTime(long startTime) {
        GenericApplianceStartTimesControllerExchange halControllerExchangeObject
                = new GenericApplianceStartTimesControllerExchange(
                this.getUUID(),
                this.getTimeDriver().getCurrentEpochSecond(),
                startTime);
        this.updateOcDataSubscriber(halControllerExchangeObject);
    }

    public void setDof(Integer firstDof, Integer secondDof) {

        boolean dofChanged = false;

        if ((firstDof != null && firstDof < 0)
                || (secondDof != null && secondDof < 0)) {
            throw new IllegalArgumentException("firstDof or secondDof < 0");
        }

        if (firstDof == null && secondDof == null) {
            throw new IllegalArgumentException("firstDof and secondDof == null");
        }

        if (firstDof != null && this.firstDof != firstDof) {
            this.firstDof = firstDof;
            dofChanged = true;
        }

        if (secondDof != null && this.secondDof != secondDof) {
            this.secondDof = secondDof;
            dofChanged = true;
        }

        if (dofChanged && (this.currentState == EN50523DeviceState.PROGRAMMED
                || this.currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART)) {
            this.updateIPPExchange();
        }

    }


    public void updateDofExchange() {
        // state for REST and logging
        this.getOCRegistry().publish(
                MieleDofStateExchange.class,
                this.getUUID(),
                new MieleDofStateExchange(
                        this.getUUID(),
                        this.getTimeDriver().getCurrentEpochSecond(),
                        this.firstDof,
                        Math.min(this.getTimeDriver().getCurrentEpochSecond(), this.latestStart),
                        this.latestStart,
                        this.expectedStartTime));
    }


    protected void updateIPPExchange() {
        InterdependentProblemPart<?, ?> ipp;

        long now = this.getTimeDriver().getCurrentEpochSecond();

        if (this.currentState == EN50523DeviceState.PROGRAMMED
                || this.currentState == EN50523DeviceState.PROGRAMMEDWAITINGTOSTART) {
            assert this.programmedAt >= 0;

//			if( deviceStartTime != -1 )
//				latestStart = deviceStartTime;
//			else
            this.latestStart = this.programmedAt + this.firstDof;

            ipp = new MieleApplianceIPP(
                    this.getUUID(),
                    this.getGlobalLogger(),
                    now, //now
                    now, //earliest starting time
                    this.latestStart,
                    this.currentProfile.clone(),
                    true, //reschedule
                    false,
                    this.latestStart + this.currentProfile.getEndingTimeOfProfile(),
                    this.getLocalObserver().getDeviceType(),
                    this.compressionType,
                    this.compressionValue);

            IAction mieleAction = new MieleAction(
                    this.getUUID(),
                    this.programmedAt,
                    (MieleApplianceIPP) ipp);

            this.getOCRegistry().publish(
                    LastActionExchange.class,
                    this.getUUID(),
                    new LastActionExchange(
                            this.getUUID(),
                            this.getTimeDriver().getCurrentEpochSecond(),
                            mieleAction));
        } else {
            if (this.profileStarted > 0) {
                ipp = new MieleApplianceNonControllableIPP(
                        this.getUUID(),
                        this.getGlobalLogger(),
                        now,
                        new SparseLoadProfile().merge(this.currentProfile, this.profileStarted),
                        true, // reschedule
                        this.getLocalObserver().getDeviceType(),
                        this.compressionType,
                        this.compressionValue
                );
            } else {
                // for a real Smart Home we should reschedule, because the user
                // could have aborted an action.
                // for a simulation we don't need that, because nobody will (at
                // the moment) abort anything

                // IMA: StaticEAProblemPartExchange causes rescheduling


                ipp = new MieleApplianceNonControllableIPP(
                        this.getUUID(),
                        this.getGlobalLogger(),
                        now,
                        new SparseLoadProfile(),
                        true, // reschedule
                        this.getLocalObserver().getDeviceType(),
                        this.compressionType,
                        this.compressionValue
                );
            }
        }
        this.getOCRegistry().publish(InterdependentProblemPart.class, this.getUUID(), ipp);
        this.updateDofExchange();
    }
}
