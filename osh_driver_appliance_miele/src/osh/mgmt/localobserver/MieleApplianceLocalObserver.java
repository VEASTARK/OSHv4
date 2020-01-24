package osh.mgmt.localobserver;

import osh.core.exceptions.OCUnitException;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSHOC;
import osh.core.oc.LocalObserver;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.dof.DofStateExchange;
import osh.datatypes.mox.IModelOfObservationExchange;
import osh.datatypes.mox.IModelOfObservationType;
import osh.datatypes.power.LoadProfileCompressionTypes;
import osh.datatypes.power.SparseLoadProfile;
import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.oc.details.DeviceMetaOCDetails;
import osh.datatypes.registry.oc.state.globalobserver.CommodityPowerStateExchange;
import osh.eal.hal.exchange.HALObserverExchange;
import osh.eal.hal.exchange.compression.StaticCompressionExchange;
import osh.eal.hal.interfaces.common.IHALDeviceMetaDetails;
import osh.eal.hal.interfaces.electricity.IHALElectricalPowerDetails;
import osh.eal.time.TimeExchange;
import osh.eal.time.TimeSubscribeEnum;
import osh.en50523.EN50523DeviceState;
import osh.hal.exchange.GenericApplianceDofObserverExchange;
import osh.hal.exchange.MieleApplianceObserverExchange;
import osh.hal.interfaces.appliance.IHALGenericApplianceDetails;
import osh.hal.interfaces.appliance.IHALMieleApplianceProgramDetails;
import osh.mgmt.mox.MieleApplianceMOX;
import osh.registry.interfaces.IDataRegistryListener;


/**
 * @author Florian Allerding, Ingo Mauser, Sebastian Kramer
 */
public class MieleApplianceLocalObserver
        extends LocalObserver
        implements IDataRegistryListener {

    /**
     * SparseLoadProfile containing different profile with different commodities<br>
     * IMPORATANT: RELATIVE TIMES!
     */
    private SparseLoadProfile currentProfile;
    private EN50523DeviceState currentState;

    private LoadProfileCompressionTypes compressionType;
    private int compressionValue;


    /**
     * latest start time set by device
     */
    private long deviceStartTime = -1;

    private int lastActivePowerLevel;
    private int lastReactivePowerLevel;

    private long profileStarted = -1;
    private long programmedAt = -1;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     */
    public MieleApplianceLocalObserver(IOSHOC osh) {
        super(osh);
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        this.getOSH().getTimeRegistry().subscribe(this, TimeSubscribeEnum.SECOND);
    }


    @Override
    public IModelOfObservationExchange getObservedModelData(IModelOfObservationType type) {
        return new MieleApplianceMOX(this.currentProfile, this.currentState, this.profileStarted, this.programmedAt, this.compressionType, this.compressionValue);
    }

    @Override
    public void onDeviceStateUpdate() throws OCUnitException {
        boolean programUpdated = false;
        HALObserverExchange _hx = (HALObserverExchange) this.getObserverDataObject();

        if (_hx instanceof MieleApplianceObserverExchange) {
            long currentDeviceStartTime = ((MieleApplianceObserverExchange) _hx).getDeviceStartTime();
            if (Math.abs(currentDeviceStartTime - this.deviceStartTime) >= 300 /* 5 Minutes */)
                programUpdated = true;
            this.deviceStartTime = currentDeviceStartTime;

            //well, well...
            //TODO what to do with deviceStartTime?!?
        } else if (_hx instanceof StaticCompressionExchange) {
            // TODO use config of static compression
        } else if (_hx instanceof GenericApplianceDofObserverExchange) {
            //TODO will be handled below
        } else {
            throw new OCUnitException("Miele Device " + this.getDeviceType() + " received invalid OX!");
        }

        if (_hx instanceof IHALElectricalPowerDetails) {
            IHALElectricalPowerDetails idepd = (IHALElectricalPowerDetails) _hx;

            int currentActivePower = idepd.getActivePower();
            int currentReactivePower = idepd.getReactivePower();

            if (Math.abs(currentActivePower - this.lastActivePowerLevel) > 0
                    || Math.abs(currentReactivePower - this.lastReactivePowerLevel) > 0) {
                CommodityPowerStateExchange cpse = new CommodityPowerStateExchange(
                        _hx.getDeviceID(),
                        _hx.getTimestamp(),
                        this.getDeviceType());
                cpse.addPowerState(Commodity.ACTIVEPOWER, currentActivePower);
                cpse.addPowerState(Commodity.REACTIVEPOWER, currentReactivePower);
                this.getOCRegistry().publish(
                        CommodityPowerStateExchange.class,
                        this,
                        cpse);

                this.lastActivePowerLevel = idepd.getActivePower();
                this.lastReactivePowerLevel = idepd.getReactivePower();
            }
        }

        if (_hx instanceof IHALGenericApplianceDetails
                && _hx instanceof IHALMieleApplianceProgramDetails) {
            IHALGenericApplianceDetails hgad = ((IHALGenericApplianceDetails) _hx);
            IHALMieleApplianceProgramDetails hgapd = ((IHALMieleApplianceProgramDetails) _hx);

            EN50523DeviceState newState = hgad.getEN50523DeviceState();

            if (this.currentState != newState || programUpdated) {
                if (newState != null) {
                    // set the current state
                    this.currentState = newState;

                    switch (newState) {
                        case OFF:
                        case STANDBY: {
                            this.profileStarted = -1;
                            this.programmedAt = -1;
                            this.currentProfile = new SparseLoadProfile();
                        }
                        break;
                        case PROGRAMMEDWAITINGTOSTART:
                        case PROGRAMMED: {
                            this.currentProfile = SparseLoadProfile
                                    .convertToSparseProfile(
                                            hgapd.getExpectedLoadProfiles(),
                                            LoadProfileCompressionTypes.DISCONTINUITIES,
                                            1,
                                            -1);

                            this.profileStarted = -1;
                            if (this.programmedAt == -1)
                                this.programmedAt = _hx.getTimestamp();
                        }
                        break;
                        case RUNNING: {
                            this.currentProfile = SparseLoadProfile
                                    .convertToSparseProfile(
                                            hgapd.getExpectedLoadProfiles(),
                                            LoadProfileCompressionTypes.DISCONTINUITIES,
                                            1,
                                            -1);
                            if (this.profileStarted == -1)
                                this.profileStarted = _hx.getTimestamp();
                        }
                        break;
                        case ENDPROGRAMMED: {
                            this.programmedAt = -1;
                            this.profileStarted = -1;
                        }
                        break;
                        default:
                            this.programmedAt = -1;
                            this.profileStarted = -1;
                            break;
                    }

                    this.getGlobalLogger().logDebug(
                            "Appliance " + _hx.getDeviceID().toString()
                                    + " " + this.currentState + ": ["
                                    + this.programmedAt + "]");
                } else { // newState == null
                    //TODO: Decide about state...
                    // 1. based on consumption...
                    // 2. based on EMP...
                }

            }
        }

        if (_hx instanceof IHALDeviceMetaDetails) {
            IHALDeviceMetaDetails ihdmd = (IHALDeviceMetaDetails) _hx;

            DeviceMetaOCDetails _devDetails = new DeviceMetaOCDetails(_hx.getDeviceID(), _hx.getTimestamp());
            _devDetails.setName(ihdmd.getName());
            _devDetails.setLocation(ihdmd.getLocation());
            _devDetails.setDeviceType(ihdmd.getDeviceType());
            _devDetails.setDeviceClassification(ihdmd.getDeviceClassification());
            _devDetails.setConfigured(ihdmd.isConfigured());

            this.getOCRegistry().publish(DeviceMetaOCDetails.class, this, _devDetails);
        }

        if (_hx instanceof StaticCompressionExchange) {
            StaticCompressionExchange stDe = (StaticCompressionExchange) _hx;

            this.compressionType = stDe.getCompressionType();
            this.compressionValue = stDe.getCompressionValue();
        }

        if (_hx instanceof GenericApplianceDofObserverExchange) {
            GenericApplianceDofObserverExchange gadoe = ((GenericApplianceDofObserverExchange) _hx);

            DofStateExchange dse = new DofStateExchange(this.getUUID(), this.getTimeDriver().getCurrentEpochSecond());
            dse.setDevice1stDegreeOfFreedom(gadoe.getDevice1stDegreeOfFreedom());
            dse.setDevice2ndDegreeOfFreedom(gadoe.getDevice2ndDegreeOfFreedom());

            this.getOCRegistry().publish(DofStateExchange.class, this, dse);
        }
    }

    @Override
    public <T extends TimeExchange> void onTimeExchange(T exchange) {
        super.onTimeExchange(exchange);
    }

    @Override
    public <T extends AbstractExchange> void onExchange(T exchange) {
        //nothing
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + " for " + this.getUUID()
                + " (" + this.getDeviceType() + ")";
    }
}
