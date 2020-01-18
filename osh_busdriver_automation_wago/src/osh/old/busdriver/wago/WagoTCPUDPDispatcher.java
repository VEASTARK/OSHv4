package osh.old.busdriver.wago;

import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.old.busdriver.wago.data.*;
import osh.old.busdriver.wago.parser.SwitchCommand;

import java.net.InetAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * This class organizes all necessary steps to receive data from a Wago controller.
 *
 * @author Kaibin Bao, Till Schuberth, Florian Allerding, Ingo Mauser
 */
@Deprecated
public class WagoTCPUDPDispatcher {
    public final HashMap<Integer, WagoRelayData> switchData = new HashMap<>();
    public final HashMap<Integer, WagoPowerMeter> powerData = new HashMap<>();
    public final HashMap<Integer, WagoVirtualSwitch> vsData = new HashMap<>();
    public final HashMap<Integer, WagoVirtualGroup> vsgData = new HashMap<>();
    public final HashMap<Integer, WagoDiData> diData = new HashMap<>();
    public final HashMap<Integer, WagoDiGroup> digData = new HashMap<>();
    public final HashMap<Integer, WagoDoData> doData = new HashMap<>();
    public final HashMap<Integer, WagoDoGroup> dogData = new HashMap<>();
    private final Set<UpdateListener> updateListeners = new HashSet<>();
    public boolean connected;
    private TCPUDPConnectionHandler connHandler;

    public WagoTCPUDPDispatcher(IGlobalLogger logger, InetAddress address) throws OSHException {
        try {
            this.connHandler = new TCPUDPConnectionHandler(logger, address);
            this.connHandler.setConnectionListener(isConnected -> {
                WagoTCPUDPDispatcher.this.connected = isConnected;

                WagoTCPUDPDispatcher.this.notifyUpdateListeners();
            });
            this.connHandler.setListener(deviceList -> {
                // convert power data
                if (deviceList.getInputs() != null) {
                    for (WagoMeterGroup meterGroup : deviceList.getInputs()) {
                        int gid = meterGroup.getGroupId();
                        for (WagoPowerMeter meter : meterGroup.getMeters()) {
                            meter.setGroupId(gid);
                            int uid = gid * 10 + meter.getMeterId();
                            WagoTCPUDPDispatcher.this.powerData.put(uid, meter);
                        }
                    }
                }

                // convert switch data
                if (deviceList.getRelays() != null) {
                    for (WagoRelayData relay : deviceList.getRelays()) {
                        int uid = relay.getId();
                        WagoTCPUDPDispatcher.this.switchData.put(uid, relay);
                    }
                }

                // convert vs data
                if (deviceList.getVsGroups() != null) {
                    for (WagoVirtualGroup vsGroup : deviceList.getVsGroups()) {
                        int gid = vsGroup.getGroupId();
                        for (WagoVirtualSwitch vs : vsGroup.getVSwitches()) {
                            vs.setGroupId(gid);
                            int uid = gid * 10 + vs.getId();
                            WagoTCPUDPDispatcher.this.vsData.put(uid, vs);
                        }
                        WagoTCPUDPDispatcher.this.vsgData.put(gid, vsGroup);
                    }
                }

                // convert digital in data
                if (deviceList.getDi8Groups() != null) {
                    for (WagoDiGroup diGroup : deviceList.getDi8Groups()) {
                        int gid = diGroup.getGroupId();
                        for (WagoDiData di : diGroup.getDigitalIns()) {
                            di.setGroupId(gid);
                            int uid = gid * 10 + di.getId();
                            WagoTCPUDPDispatcher.this.diData.put(uid, di);
                        }
                        WagoTCPUDPDispatcher.this.digData.put(gid, diGroup);
                    }
                }

                // convert digital out data
                if (deviceList.getDo8Groups() != null) {
                    for (WagoDoGroup doGroup : deviceList.getDo8Groups()) {
                        int gid = doGroup.getGroupId();
                        for (WagoDoData do8 : doGroup.getDigitalOuts()) {
                            do8.setGroupId(gid);
                            int uid = gid * 10 + do8.getId();
                            WagoTCPUDPDispatcher.this.doData.put(uid, do8);
                        }
                        WagoTCPUDPDispatcher.this.dogData.put(gid, doGroup);
                    }
                }

                WagoTCPUDPDispatcher.this.notifyUpdateListeners();
            });
        } catch (SmartPlugException e) {
            throw new OSHException(e);
        }
    }

    public boolean isConnected() {
        return this.connected;
    }

    public Collection<WagoPowerMeter> getPowerData() {
        synchronized (this.powerData) {
            return this.powerData.values();
        }
    }

    public Collection<WagoRelayData> getSwitchData() {
        synchronized (this.switchData) {
            return this.switchData.values();
        }
    }

    public Collection<WagoDiData> getDigitalInData() {
        synchronized (this.diData) {
            return this.diData.values();
        }
    }

    public Collection<WagoDiGroup> getDigitalInGroup() {
        synchronized (this.digData) {
            return this.digData.values();
        }
    }

    public Collection<WagoDoData> getDigitalOutData() {
        synchronized (this.doData) {
            return this.doData.values();
        }
    }

    public Collection<WagoDoGroup> getDigitalOutGroup() {
        synchronized (this.dogData) {
            return this.dogData.values();
        }
    }

    public Collection<WagoVirtualSwitch> getVirtualSwitchData() {
        synchronized (this.vsData) {
            return this.vsData.values();
        }
    }

    public Collection<WagoVirtualGroup> getVirtualSwitchGroupData() {
        synchronized (this.vsData) {
            return this.vsgData.values();
        }
    }

    public void setSwitch(int id, boolean state) throws SmartPlugException {
        SwitchCommand.Command cmd;
        if (state) cmd = SwitchCommand.Command.CMD_ON;
        else cmd = SwitchCommand.Command.CMD_OFF;

        new SwitchCommand("relay", id, cmd, this.connHandler).sendCommand();
    }

    public void setVirtualSwitch(int moduleId, int portId, boolean state) throws SmartPlugException {
        SwitchCommand.Command cmd;
        if (state) cmd = SwitchCommand.Command.CMD_ON;
        else cmd = SwitchCommand.Command.CMD_OFF;

        new SwitchCommand("vs", moduleId, portId, cmd, this.connHandler).sendCommand();
    }

    public void setDigitalOutput(int moduleId, int portId, boolean state) throws SmartPlugException {
        SwitchCommand.Command cmd;
        if (state) cmd = SwitchCommand.Command.CMD_ON;
        else cmd = SwitchCommand.Command.CMD_OFF;

        new SwitchCommand("do8", moduleId, portId, cmd, this.connHandler).sendCommand();
    }

    public void registerUpdateListener(UpdateListener l) {
        this.updateListeners.add(l);
    }

    public void unregisterUpdateListener(UpdateListener l) {
        this.updateListeners.remove(l);
    }

    protected void notifyUpdateListeners() {
        for (UpdateListener l : this.updateListeners) {
            l.wagoUpdateEvent();
        }
    }

    public interface UpdateListener {
        void wagoUpdateEvent();
    }

}