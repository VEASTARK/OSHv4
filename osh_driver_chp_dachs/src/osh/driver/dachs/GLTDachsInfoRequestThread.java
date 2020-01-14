package osh.driver.dachs;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import osh.core.exceptions.OSHException;
import osh.core.logging.IGlobalLogger;
import osh.datatypes.registry.driver.details.chp.raw.DachsDriverDetails;
import osh.driver.GLTDachsChpDriver;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/**
 * @author Florian Allerding, Kaibin Bao, Till Schuberth, Ingo Mauser
 */
@SuppressWarnings("deprecation")
public class GLTDachsInfoRequestThread implements Runnable {

    private final IGlobalLogger globalLogger;
    private final GLTDachsChpDriver dachsDriver;

    private final ArrayList<String> parametersToGet = new ArrayList<>(Arrays.asList(// Betriebsdaten Dachs
            "Hka_Bd.Anforderung.ModulAnzahl",
            "Hka_Bd.Anforderung.UStromF_Anf.bFlagSF",
            "Hka_Bd.UStromF_Frei.bFreigabe",
            "Hka_Bd.bStoerung",
            "Hka_Bd.bWarnung",
            "Hka_Bd.UHka_Anf.Anforderung.fStrom",
            "Hka_Bd.UHka_Anf.usAnforderung",
            "Hka_Bd.UHka_Frei.usFreigabe",
            "Hka_Bd.ulArbeitElektr",
            "Hka_Bd.ulArbeitThermHka",
//			"Hka_Bd.ulArbeitThermKon", // n.a.
            "Hka_Bd.ulBetriebssekunden",
            "Hka_Bd.ulAnzahlStarts",
//			"Hka_Bd_Stat.uchSeriennummer,"  // static
//			"Hka_Bd_Stat.uchTeilenummer", // static
//			"Hka_Bd_Stat.ulInbetriebnahmedatum", // static

            // Betriebsdaten 31.12.
            "BD3112.Hka_Bd.ulBetriebssekunden",
            "BD3112.Hka_Bd.ulAnzahlStarts",
            "BD3112.Hka_Bd.ulArbeitElektr",
            "BD3112.Hka_Bd.ulArbeitThermHka",
//			"BD3112.Hka_Bd.ulArbeitThermKon", // n.a.
            "BD3112.Ww_Bd.ulWwMengepA",

            // Daten 2. Wärmeerzeuger (SEplus) // KIT
            "Brenner_Bd.bIstStatus",
            "Brenner_Bd.bWarnung",
            "Brenner_Bd.UBrenner_Anf.usAnforderung",
            "Brenner_Bd.UBrenner_Frei.bFreigabe",
            "Brenner_Bd.ulAnzahlStarts",
            "Brenner_Bd.ulBetriebssekunden",

            // Hydraulik Schema
//			"Hka_Ew.HydraulikNr.bSpeicherArt", // static
//			"Hka_Ew.HydraulikNr.bWW_Art", // static
//			"Hka_Ew.HydraulikNr.b2_Waermeerzeuger", // static
//			"Hka_Ew.HydraulikNr.bMehrmodul", // static

            // Temperaturen
            "Hka_Mw1.Temp.sAbgasHKA",
            "Hka_Mw1.Temp.sAbgasMotor",
            "Hka_Mw1.Temp.sKapsel",
            "Hka_Mw1.Temp.sbAussen",
            "Hka_Mw1.Temp.sbFreigabeModul",
            "Hka_Mw1.Temp.sbFuehler1",
            "Hka_Mw1.Temp.sbFuehler2",
            "Hka_Mw1.Temp.sbGen",
            "Hka_Mw1.Temp.sbMotor",
            "Hka_Mw1.Temp.sbRegler",
            "Hka_Mw1.Temp.sbRuecklauf",
            "Hka_Mw1.Temp.sbVorlauf",
            "Hka_Mw1.Temp.sbZS_Fuehler3",
            "Hka_Mw1.Temp.sbZS_Fuehler4",
            "Hka_Mw1.Temp.sbZS_Vorlauf1",
            "Hka_Mw1.Temp.sbZS_Vorlauf2",
            "Hka_Mw1.Temp.sbZS_Warmwasser",
            "Hka_Mw1.Solltemp.sbRuecklauf",
            "Hka_Mw1.Solltemp.sbVorlauf",

            // Aktoren
//			"Hka_Mw1.Aktor.bWwPumpe",
//			"Hka_Mw1.Aktor.fFreiAltWaerm",
//			"Hka_Mw1.Aktor.fMischer1Auf",
            // ...
            "Hka_Mw1.sWirkleistung",
            "Hka_Mw1.ulMotorlaufsekunden",
            "Hka_Mw1.usDrehzahl",

            // Tageslauf
            // ...

            // Informationen über Wartung
            "Wartung_Cache.fStehtAn",
            "Wartung_Cache.ulBetriebssekundenBei", // quasi-static
//			"Wartung_Cache.ulZeitstempel", // quasi-static
            "Wartung_Cache.usIntervall" // quasi-static
    ));

    private boolean shutdown;
    private ZonedDateTime lastException = ZonedDateTime.now();
    private int reconnectWait;

    private final String urlToDachs;
    private final String loginName;
    private final String loginPwd;


    /**
     * CONSTRUCTOR
     *
     * @param globalLogger
     * @param dachsDriver
     * @param urlToDachs
     */
    public GLTDachsInfoRequestThread(
            IGlobalLogger globalLogger,
            GLTDachsChpDriver dachsDriver,
            String urlToDachs,
            String loginName,
            String loginPwd) {
        this.globalLogger = globalLogger;
        this.dachsDriver = dachsDriver;
        this.urlToDachs = urlToDachs;
        this.loginName = loginName;
        this.loginPwd = loginPwd;
    }


    @Override
    public void run() {
        while (!this.shutdown) {
            try {
//				globalLogger.logDebug("Getting new DACHS data");

                DachsDriverDetails dachsDetails = new DachsDriverDetails(
                        this.dachsDriver.getDeviceID(),
                        this.dachsDriver.getTimer().getUnixTime());

                // get information from DACHS and save into dachsDetails
                HashMap<String, String> values = this.getDataFromDachs(this.parametersToGet);
                dachsDetails.setValues(values);

                this.dachsDriver.processDachsDetails(dachsDetails);
            } catch (Exception e) {
                this.globalLogger.logError("Reading dachs data failed", e);

                long diff = ZonedDateTime.now().toEpochSecond() - this.lastException.toEpochSecond();
                if (diff < 0 || diff > 300000) {
                    this.reconnectWait = 0;
                } else {
                    if (this.reconnectWait <= 0) {
                        this.reconnectWait = 1;
                    }
                    this.reconnectWait *= 2;
                    if (this.reconnectWait > 180) {
                        this.reconnectWait = 180;
                    }
                }
                this.lastException = ZonedDateTime.now();

                try {
                    Thread.sleep(this.reconnectWait * 1000);
                } catch (InterruptedException ignored) {
                }
            }
            try {
                Thread.sleep(1000); // INCREASE???
            } catch (InterruptedException ignored) {
            }
        }
    }


    private HashMap<String, String> getDataFromDachs(List<String> keys) throws OSHException {
        if (keys.isEmpty()) return new HashMap<>();

        HashMap<String, String> fullMap = new HashMap<>();

        int numberOfKeys = keys.size();
        int startKey = 0;
        int endKey = Math.min(4, numberOfKeys);

        // request only 5 keys at a time
        while (endKey < numberOfKeys) {
            StringBuilder reqData = new StringBuilder();
            boolean first = true;
            for (int i = startKey; i <= endKey; i++) {
                if (!first) reqData.append('&');
                first = false;
                reqData.append("k=").append(keys.get(i));
            }

            DefaultHttpClient client = new DefaultHttpClient();
            try {
                HttpGet httpget = new HttpGet(this.urlToDachs + "getKey?" + reqData);
                client.getCredentialsProvider().setCredentials(
                        new AuthScope(
                                httpget.getURI().getHost(),
                                httpget.getURI().getPort()),
                        new UsernamePasswordCredentials(this.loginName, this.loginPwd));
                HttpResponse response = client.execute(httpget);

                HttpEntity entity = response.getEntity();
                HashMap<String, String> newMap = this.parseDachsAnswer(EntityUtils.toString(entity));

                for (Entry<String, String> e : newMap.entrySet()) {
                    fullMap.put(e.getKey(), e.getValue());
                }
            } catch (IOException ex) {
                this.globalLogger.logWarning("Could not connect to DACHS.");
            } finally {
                client.getConnectionManager().shutdown();
                client.close();
            }

            startKey = endKey + 1;
            endKey = Math.min(endKey + 5, numberOfKeys);

            try {
                Thread.sleep(100); // INCREASE???
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return fullMap;
    }

    private HashMap<String, String> parseDachsAnswer(String answer) throws OSHException {
        HashMap<String, String> res = new HashMap<>();

        for (String item : answer.split("\n")) {
            if (answer.trim().isEmpty()) {
                continue;
            }
            String[] keyValue = item.split("=");

            if (keyValue.length != 2) {
                throw new OSHException("problem parsing dachs answer");
            }

            res.put(keyValue[0].trim(), keyValue[1].trim());
        }

        return res;
    }

    public void shutdown() {
        this.shutdown = true;
    }

}
