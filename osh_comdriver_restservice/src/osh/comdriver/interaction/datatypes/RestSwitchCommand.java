package osh.comdriver.interaction.datatypes;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "switchCommand")
public class RestSwitchCommand {
    protected boolean on;

    public boolean isTurnOn() {
        return this.on;
    }

    public void setTurnOn(boolean on) {
        this.on = on;
    }
}
