package osh.datatypes.registry.driver.details.chp.raw;

import osh.datatypes.registry.StateExchange;

import javax.xml.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

/**
 * @author Kaibin Bao, Ingo Mauser
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "DachsDriverDetails")
@XmlType
@SuppressWarnings("unused")
public class DachsDriverDetails extends StateExchange {

    private static final long serialVersionUID = 1449180454003778385L;

    // Dachs
    private String hkaBdAnforderungModulAnzahl;
    private String hkaBdAnforderungUStromFAnfbFlagSF;
    private String hkaBdUStromFFreibFreigabe;
    private String hkaBdbStoerung;
    private String hkaBdbWarnung;
    private String hkaBdUHkaAnfAnforderungfStrom;
    private String hkaBdUHkaAnfusAnforderung;
    private String hkaBdUHkaFreiusFreigabe;
    private String hkaBdulArbeitElektr;
    private String hkaBdulArbeitThermHka;
    private String hkaBdulBetriebssekunden;
    private String hkaBdulAnzahlStarts;

    // "Betriebsdaten 31.12."
    private String bD3112HkaBdulBetriebssekunden;
    private String bD3112HkaBdulAnzahlStarts;
    private String bD3112HkaBdulArbeitElektr;
    private String bD3112HkaBdulArbeitThermHka;
    //	private String bD3112HkaBdulArbeitThermKon;
    private String bD3112WwBdulWwMengepA;

    // "Daten 2. Waermeerzeuger (SEplus)"
    private String brennerBdbIstStatus;
    // ... (KIT)

    // "Hydraulik Schema"
    //none

    // Temperaturen
    // ...


    private String hkaMw1TempsbAussen;
    private String hkaMw1TempsbFuehler1;
    private String hkaMw1TempsbFuehler2;
    private String hkaMw1TempsbGen;
    private String hkaMw1TempsbMotor;
    private String hkaMw1TempsbRuecklauf;
    private String hkaMw1TempsbVorlauf;

    // ...

    private String hkaMw1TempsbZSVorlauf1;

    // ...

    private String hkaMw1TempsbZSWarmwasser;

    // ...

    // "Aktoren"
    private String hkaMw1sWirkleistung;
    private String hkaMw1ulMotorlaufsekunden;
    private String hkaMw1usDrehzahl;

    // Tageslauf
    //none

    // Informationen über Wartung

    private String wartungCachefStehtAn;

    @XmlTransient
    private HashMap<String, String> values;


    /**
     * for JAXB only
     */
    @Deprecated
    public DachsDriverDetails() {
        super(null, -1L);
    }

    /**
     * CONSTRUCTOR
     */
    public DachsDriverDetails(UUID sender, long timestamp) {
        super(sender, timestamp);
    }

    /**
     * use with CARE! (is not cloned)
     */
    public HashMap<String, String> getValues() {
        return this.values;
    }

    //	@Transient
    public void setValues(Map<String, String> values) {
        this.values = new HashMap<>(values);
        for (Entry<String, String> e : values.entrySet()) {
            // Betriebsdaten Dachs
            switch (e.getKey()) {
                case "Hka_Bd.Anforderung.ModulAnzahl":
                    this.hkaBdAnforderungModulAnzahl = e.getValue();
                    break;
                case "Hka_Bd.Anforderung.UStromF_Anf.bFlagSF":
                    this.hkaBdAnforderungUStromFAnfbFlagSF = e.getValue();
                    break;
                case "Hka_Bd.UStromF_Frei.bFreigabe":
                    this.hkaBdUStromFFreibFreigabe = e.getValue();
                    break;
                case "Hka_Bd.bStoerung":
                    this.hkaBdbStoerung = e.getValue();
                    break;
                case "Hka_Bd.bWarnung":
                    this.hkaBdbWarnung = e.getValue();
                    break;
                case "Hka_Bd.UHka_Anf.Anforderung.fStrom":
                    this.hkaBdUHkaAnfAnforderungfStrom = e.getValue();
                    break;
                case "Hka_Bd.UHka_Anf.usAnforderung":
                    this.hkaBdUHkaAnfusAnforderung = e.getValue();
                    break;
                case "Hka_Bd.UHka_Frei.usFreigabe":
                    this.hkaBdUHkaFreiusFreigabe = e.getValue();
                    break;
                case "Hka_Bd.ulArbeitElektr":
                    this.hkaBdulArbeitElektr = e.getValue();
                    break;
                case "Hka_Bd.ulArbeitThermHka":
                    this.hkaBdulArbeitThermHka = e.getValue();
                    break;
                case "Hka_Bd.ulBetriebssekunden":
                    this.hkaBdulBetriebssekunden = e.getValue();
                    break;
                case "Hka_Bd.ulAnzahlStarts":
                    this.hkaBdulAnzahlStarts = e.getValue();
                    break;

                // Betriebsdaten 31.12.
                case "BD3112.Hka_Bd.ulBetriebssekunden":
                    this.bD3112HkaBdulBetriebssekunden = e.getValue();
                    break;
                case "BD3112.Hka_Bd.ulAnzahlStarts":
                    this.bD3112HkaBdulAnzahlStarts = e.getValue();
                    break;
                case "BD3112.Hka_Bd.ulArbeitElektr":
                    this.bD3112HkaBdulArbeitElektr = e.getValue();
                    break;
                case "BD3112.Hka_Bd.ulArbeitThermHka":
                    this.bD3112HkaBdulArbeitThermHka = e.getValue();
                    break;
//			else if (e.getKey().equals("BD3112.Hka_Bd.ulArbeitThermKon")) {
//				bD3112HkaBdulArbeitThermKon = e.getValue();
//			}
                case "BD3112.Ww_Bd.ulWwMengepA":
                    this.bD3112WwBdulWwMengepA = e.getValue();
                    break;
                // Daten 2. Wärmeerzeuger (SEplus)
                case "Brenner_Bd.bIstStatus":
                    this.brennerBdbIstStatus = e.getValue();
                    break;
                // Temperaturen
                case "Hka_Mw1.Temp.sbAussen":
                    this.hkaMw1TempsbAussen = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbFuehler1":
                    this.hkaMw1TempsbFuehler1 = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbFuehler2":
                    this.hkaMw1TempsbFuehler2 = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbGen":
                    this.hkaMw1TempsbGen = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbMotor":
                    this.hkaMw1TempsbMotor = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbVorlauf":
                    this.hkaMw1TempsbVorlauf = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbRuecklauf":
                    this.hkaMw1TempsbRuecklauf = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbZS_Vorlauf1":
                    this.hkaMw1TempsbZSVorlauf1 = e.getValue();
                    break;
                case "Hka_Mw1.Temp.sbZS_Warmwasser":
                    this.hkaMw1TempsbZSWarmwasser = e.getValue();
                    break;

                // Hydraulik Schema
                //none
                // Aktoren
                case "Hka_Mw1.sWirkleistung":
                    this.hkaMw1sWirkleistung = e.getValue();
                    break;
                case "Hka_Mw1.ulMotorlaufsekunden":
                    this.hkaMw1ulMotorlaufsekunden = e.getValue();
                    break;
                case "Hka_Mw1.usDrehzahl":
                    this.hkaMw1usDrehzahl = e.getValue();
                    break;
                // Tageslauf
                //none
                // Informationen über Wartung
                case "Wartung_Cache.fStehtAn":
                    this.wartungCachefStehtAn = e.getValue();
                    break;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");

        boolean first = true;

        for (Entry<String, String> e : this.values.entrySet()) {
            if (!first) {
                builder.append(",");
            } else {
                first = false;
            }
            builder.append(e.getKey()).append("=").append(e.getValue());
        }

        builder.append("]");

        return builder.toString();
    }

    public boolean isEmpty() {
        return this.values == null;
    }

}
