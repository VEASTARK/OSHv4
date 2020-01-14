package constructsimulation.datatypes;

import osh.configuration.cal.CALConfiguration;
import osh.configuration.eal.EALConfiguration;
import osh.configuration.oc.OCConfiguration;
import osh.configuration.system.OSHConfiguration;
import osh.simulation.screenplay.Screenplay;

public class SimulationConfigurationWrapper {

    private final OCConfiguration ocConfig;
    private final EALConfiguration ealConfig;
    private final CALConfiguration calConfig;
    private final OSHConfiguration oshConfig;
    private final Screenplay myScreenplay;
    public SimulationConfigurationWrapper(
            OCConfiguration ocConfig,
            EALConfiguration ealConfig,
            CALConfiguration calConfig,
            OSHConfiguration oshConfig,
            Screenplay myScreenplay) {
        this.ocConfig = ocConfig;
        this.ealConfig = ealConfig;
        this.calConfig = calConfig;
        this.oshConfig = oshConfig;
        this.myScreenplay = myScreenplay;
    }

    public OCConfiguration getOcConfig() {
        return this.ocConfig;
    }

    public EALConfiguration getEalConfig() {
        return this.ealConfig;
    }

    public CALConfiguration getCalConfig() {
        return this.calConfig;
    }

    public OSHConfiguration getOshConfig() {
        return this.oshConfig;
    }

    public Screenplay getMyScreenplay() {
        return this.myScreenplay;
    }


}
