package osh.datatypes.commodity;

import javax.xml.bind.annotation.XmlEnumValue;
import java.util.EnumSet;
import java.util.StringTokenizer;

/**
 * @author Ingo Mauser
 */
public enum Commodity {

    @XmlEnumValue("activepower")
    ACTIVEPOWER("activepower", "Wirkleistung", "active power", "W"),    //

    @XmlEnumValue("reactivepower")
    REACTIVEPOWER("reactivepower", "Blindleistung", "reactive power", "var"),    //

    @XmlEnumValue("naturalgaspower")
    NATURALGASPOWER("naturalgaspower", "Erdgas", "natural gas", "W"),    //

    @XmlEnumValue("liquidgaspower")
    LIQUIDGASPOWER("liquidgaspower", "Flüssiggas", "liquid gas", "W"),    //

    @XmlEnumValue("heatinghotwaterpower")
    HEATINGHOTWATERPOWER("heatinghotwaterpower", "Warmwasser (Heizung)", "hot water", "W"),    //

    @XmlEnumValue("domestichotwaterpower")
    DOMESTICHOTWATERPOWER("domestichotwaterpower", "Warmwasser (Trinkwasser)", "domestic hot water", "W"),    //

    @XmlEnumValue("coldwaterpower")
    COLDWATERPOWER("coldwaterpower", "Kaltwasser", "cold water", "W");    //

    private final String commodity;

    private final String descriptionDE;

    private final String descriptionEN;

    private final String unit;


    /**
     * CONSTRUCTOR
     *
     */
    Commodity(String commodity, String descriptionDE, String descriptionEN, String unit) {
        this.commodity = commodity;
        this.descriptionDE = descriptionDE;
        this.descriptionEN = descriptionEN;
        this.unit = unit;
    }

    public static Commodity fromString(String v) {
        for (Commodity c : Commodity.values()) {
            if (c.commodity.equalsIgnoreCase(v)) {
                return c;
            }
        }

        throw new IllegalArgumentException(v);
    }

    public static EnumSet<Commodity> parseCommodityArray(String str) throws IllegalArgumentException {
        String processed = str;
        while (processed.startsWith("["))
            processed = processed.substring(1);

        while (processed.endsWith("]"))
            processed = processed.substring(0, processed.length() - 1);

        StringTokenizer strTok = new StringTokenizer(processed, ",");
        EnumSet<Commodity> commoditySet = EnumSet.noneOf(Commodity.class);

        while (strTok.hasMoreElements()) {
            Commodity uuid = Commodity.fromString(strTok.nextToken());
            commoditySet.add(uuid);
        }
        return commoditySet;
    }

    @Override
    public String toString() {
        return this.commodity;
    }

    public String getCommodity() {
        return this.commodity;
    }

    public String getDescriptionDE() {
        return this.descriptionDE;
    }

    public String getDescriptionEN() {
        return this.descriptionEN;
    }

    public String getUnit() {
        return this.unit;
    }

}
