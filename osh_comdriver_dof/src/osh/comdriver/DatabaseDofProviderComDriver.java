package osh.comdriver;

import osh.cal.CALComDriver;
import osh.cal.ICALExchange;
import osh.comdriver.dof.DofDBThread;
import osh.configuration.OSHParameterCollection;
import osh.core.exceptions.OSHException;
import osh.core.interfaces.IOSH;
import osh.datatypes.registry.oc.state.ExpectedStartTimeExchange;
import osh.hal.exchange.DofComExchange;
import osh.hal.exchange.ScheduledApplianceUIExchange;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;


/**
 * @author Florian Allerding, Kaibin Bao, Ingo Mauser, Till Schuberth
 */
public class DatabaseDofProviderComDriver extends CALComDriver {

    final HashMap<UUID, Integer> device1stDegreeOfFreedom = new HashMap<>();
    final HashMap<UUID, Integer> device2ndDegreeOfFreedom = new HashMap<>();
    private final OSHParameterCollection config;
    private DofDBThread dbThread;


    /**
     * CONSTRUCTOR
     *
     * @param osh
     * @param deviceID
     * @param driverConfig
     */
    public DatabaseDofProviderComDriver(
            IOSH osh,
            UUID deviceID,
            OSHParameterCollection driverConfig) {
        super(osh, deviceID, driverConfig);

        this.config = driverConfig;
    }


    @Override
    public void onSystemIsUp() throws OSHException {
        super.onSystemIsUp();

        //processDummyDofInformation();
        String hostname = this.config.getParameter("dofdbhost");
        if (hostname == null)
            throw new OSHException("missing config argument dofdbhost");

        String port = this.config.getParameter("dofdbport");
        if (port == null)
            throw new OSHException("missing config argument dofdbport");

        String dbname = this.config.getParameter("dofdbname");
        if (dbname == null)
            throw new OSHException("missing config argument dofdbname");

        String dbLoginName = this.config.getParameter("dofdbloginname");
        if (dbLoginName == null)
            throw new OSHException("missing config argument dofdbloginname");

        String dbPassword = this.config.getParameter("dofdbloginpwd");
        if (dbPassword == null)
            throw new OSHException("missing config argument dofdbloginpwd");

        this.dbThread = new DofDBThread(this.getGlobalLogger(), this,
                hostname,
                port,
                dbname,
                dbLoginName,
                dbPassword);

        try {
            Class.forName("com.mysql.jdbc.Driver");
            this.dbThread.setUpSQLConnection();
        } catch (SQLException | ClassNotFoundException e) {
            this.getGlobalLogger().logError(e);
        }

        this.dbThread.start();
    }

    public ArrayList<ExpectedStartTimeExchange> triggerComManager() {
        // TODO: If you want to see the planned appliances in the EMP then
        // add here some fancy code...

        ScheduledApplianceUIExchange applianceUIExchange = new ScheduledApplianceUIExchange(
                this.getUUID(), this.getTimeDriver().getCurrentEpochSecond());
        return applianceUIExchange.getCurrentApplianceSchedules();
    }


    public void processDofInformation(HashMap<UUID, Integer> appliance1stDof, HashMap<UUID, Integer> appliance2ndDof) {

        for (Entry<UUID, Integer> e : appliance1stDof.entrySet()) {
            this.device1stDegreeOfFreedom.put(e.getKey(), e.getValue());
        }
        for (Entry<UUID, Integer> e : appliance2ndDof.entrySet()) {
            this.device2ndDegreeOfFreedom.put(e.getKey(), e.getValue());
        }

        DofComExchange dce = new DofComExchange(this.getUUID(), this.getTimeDriver().getCurrentEpochSecond());
        dce.setDevice1stDegreeOfFreedom(this.device1stDegreeOfFreedom);
        dce.setDevice2ndDegreeOfFreedom(this.device2ndDegreeOfFreedom);

        this.notifyComManager(dce);
    }

    @Override
    public void updateDataFromComManager(ICALExchange exchangeObject) {
        // NOTHING
    }
}
