package osh.en50523;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

/**
 * Probably no DIN EN 50523 standardized parameters! Parameters for Dishwasher
 * (according to Miele Dishwasher XXX)
 *
 * @author Julian Rothenbacher, Ingo Mauser
 */
@XmlType
public enum EN50523OIDWashingParametersDWPrograms {

    /**
     * Device: Dishwasher<br>
     * Program: no program selected<br>
     * DE: Kein Programm ausgewaehlt<br>
     * Meaning: No dishwashing program has been selected<br>
     */
    @XmlEnumValue("0")
    NOPROGRAMSELECTED(0, "KEIN PROGRAMM AUSGEWAEHLT"),

    /**
     * Device: Dishwasher<br>
     * Program: Pots and Pans 75°C<br>
     * DE: <br>
     * Meaning: Pots and Pans 75°C dishwashing program has been selected<br>
     */
    @XmlEnumValue("1")
    POTSANDPANS(1, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Pre wash<br>
     * DE: <br>
     * Meaning: Pre Wash dishwashing program has been selected<br>
     */
    @XmlEnumValue("2")
    PREWASH(2, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Energy save<br>
     * DE: <br>
     * Meaning: Energy save dishwashing program has been selected<br>
     */
    @XmlEnumValue("3")
    ENERGYSAVE(3, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Quick wash<br>
     * DE: <br>
     * Meaning: Quick wash dishwashing program has been selected<br>
     */
    @XmlEnumValue("4")
    QUICKWASH(4, ""),

    /**
     * Device: Dishwasher<br>
     * Program: n.a.<br>
     * DE: <br>
     * Meaning: n.a. dishwashing program has been selected<br>
     */
    @XmlEnumValue("5")
    NA(5, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Normal 55°C without water heater<br>
     * DE: <br>
     * Meaning: Normal 55°C without water heater dishwashing program has been selected<br>
     */
    @XmlEnumValue("6")
    NORMALWITHOUTWATERHEATER(6, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Intensive 65°C sensor wash<br>
     * DE: <br>
     * Meaning: Intensive 65°C sensor wash program has been selected<br>
     */
    @XmlEnumValue("7")
    INTENSIVESENSORWASH(7, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Glas without water heater sensor wash<br>
     * DE: <br>
     * Meaning: Glas without water heater sensor wash dishwashing program has been selected<br>
     */
    @XmlEnumValue("8")
    GLASWITHOUTWATERHEATERSENSORWASH(8, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Sensor wash water save<br>
     * DE: <br>
     * Meaning: Sensor wash water save dishwashing program has been selected<br>
     */
    @XmlEnumValue("9")
    SENSORWASHWATERSAVE(9, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Sensor wash gentle hygiene<br>
     * DE: <br>
     * Meaning: Sensor wash gentle hygiene dishwashing program has been selected<br>
     */
    @XmlEnumValue("10")
    SENSORWASHGENTLEHYGIENE(10, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Water save oven/grill racks<br>
     * DE: <br>
     * Meaning: Water save oven/grill racks dishwashing program has been selected<br>
     */
    @XmlEnumValue("11")
    WATERSAVEOVENGRILLRACKS(11, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Hygiene pasta<br>
     * DE: <br>
     * Meaning: Hygiene pasta dishwashing program has been selected<br>
     */
    @XmlEnumValue("12")
    HYGIENEPASTA(12, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Fondue<br>
     * DE: <br>
     * Meaning: Fondue dishwashing program has been selected<br>
     */
    @XmlEnumValue("13")
    FONDUE(13, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Pasta plastics<br>
     * DE: <br>
     * Meaning: Pasta plastics dishwashing program has been selected<br>
     */
    @XmlEnumValue("14")
    PASTAPLASTICS(14, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Fondue without upper basket 65°C<br>
     * DE: <br>
     * Meaning: Fondue without upper basket 65°C dishwashing program has been selected<br>
     */
    @XmlEnumValue("15")
    FONDUEWITHOUTUPPERBASKET(15, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Plastics without upper basket 45°C<br>
     * DE: <br>
     * Meaning: Plastics without upper basket 45°C dishwashing program has been selected<br>
     */
    @XmlEnumValue("16")
    PLASTICSWITHOUTUPPERBASKET(16, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Tall items 65°C specialist glasses<br>
     * DE: <br>
     * Meaning: Tall items 65°C specialist glasses dishwashing program has been selected<br>
     */
    @XmlEnumValue("17")
    TALLITEMSSPECIALISTGLASSES(17, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Tall items 45°C glasses quick<br>
     * DE: <br>
     * Meaning: Tall items 45°C glasses quick dishwashing program has been selected<br>
     */
    @XmlEnumValue("18")
    TALLITEMSGLASSESQUICK(18, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Glasses warm pre-wash warm<br>
     * DE: <br>
     * Meaning: Glasses warm pre-wash warm dishwashing program has been selected<br>
     */
    @XmlEnumValue("19")
    GLASSESWARMPREWASHWARM(19, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Glasses quick normal<br>
     * DE: <br>
     * Meaning: Glasses quick normal dishwashing program has been selected<br>
     */
    @XmlEnumValue("20")
    GLASSESQUICKNORMAL(20, ""),

    /**
     * Device: Dishwasher<br>
     * Program: Pre-wash 30°C<br>
     * DE: <br>
     * Meaning: Pre-wash 30°C dishwashing program has been selected<br>
     */
    @XmlEnumValue("21")
    PREWASH30C(21, "");

    /**
     * Unsigned Byte
     */
    @XmlElement
    private final byte programID;
    @XmlAttribute
    private final String descriptionDE;

    EN50523OIDWashingParametersDWPrograms(int programId, String descriptionDE) {
        this.programID = (byte) programId;
        this.descriptionDE = descriptionDE;

    }

    public static EN50523OIDWashingParametersDWPrograms fromString(String v) {
        // 0 to 127
        for (EN50523OIDWashingParametersDWPrograms c : EN50523OIDWashingParametersDWPrograms
                .values()) {
            if (Byte.toString(c.programID).equals(v)) {
                return c;
            }
        }
        // 128 to 255
        for (EN50523OIDWashingParametersDWPrograms c : EN50523OIDWashingParametersDWPrograms
                .values()) {
            if (Integer.toString(c.programID & 0xFF).equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

    /**
     * @return -128 to 127
     */
    public byte getSignedValue() {
        return this.programID;
    }

    /**
     * @return 0 to 255
     */
    public int getUnsignedValue() {
        return this.programID & 0xFF;
    }

    public byte getProgramID() {
        return this.programID;
    }

    public String getDescriptionDE() {
        return this.descriptionDE;
    }

}
