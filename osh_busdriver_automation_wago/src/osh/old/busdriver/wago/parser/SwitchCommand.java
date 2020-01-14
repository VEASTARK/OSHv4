package osh.old.busdriver.wago.parser;

import osh.old.busdriver.wago.SmartPlugException;
import osh.old.busdriver.wago.TCPUDPConnectionHandler;
import osh.old.busdriver.wago.TCPUDPConnectionHandler.CommandGenerator;

public class SwitchCommand extends CommandGenerator {

    private final String device;
    private final int id;
    private final int function;
    private final Command cmd;
    private final TCPUDPConnectionHandler handler;

    public SwitchCommand(String device, int id, Command cmd, TCPUDPConnectionHandler handler) {
        this(device, id, 0, cmd, handler);
    }

    public SwitchCommand(String device, int id, int function, Command cmd, TCPUDPConnectionHandler handler) {
        this.device = device;
        this.id = id;
        this.function = function;
        this.cmd = cmd;
        this.handler = handler;
    }

    @Override
    public void sendCommand() throws SmartPlugException {
        super.sendCommand(this.device, Integer.toString(this.id), Integer.toString(this.function), this.cmd.getCommand(), this.handler);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SwitchCommand)) return false;
        SwitchCommand obj = (SwitchCommand) o;

        return obj.cmd == this.cmd &&
                this.equalsTarget(obj);
    }

    @Override
    public boolean equalsTarget(Object o) {
        if (!(o instanceof SwitchCommand)) return false;
        SwitchCommand obj = (SwitchCommand) o;

        return obj.device.equals(this.device) &&
                obj.function == this.function &&
                obj.id == this.id &&
                obj.handler == this.handler;
    }

    public enum Command {
        CMD_ON("on"),
        CMD_OFF("off"),
        CMD_TOGGLE("toggle");

        private final String command;

        Command(String cmd) {
            this.command = cmd;
        }

        public String getCommand() {
            return this.command;
        }
    }


}
