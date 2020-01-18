package osh.en50523;

/**
 * 5-bit HEX [DIN EN 50523-2 p.12] / 4-bit HEX (0 to F) [DIN EN 50523-2 p.13]
 *
 * @author Ingo Mauser
 */
public enum EN50523Category {

    COMMON((byte) 0x1, "common household appliance", "allgemeines Haushaltsger�t"),
    VENTILATION((byte) 0x2, "ventilation", "L�ftung"),
    WET((byte) 0xA, "wet appliance", "Nass"),
    HOT((byte) 0xB, "hot apliance", "Hei�"),
    COLD((byte) 0xC, "cold appliance", "K�lte"),
    HEAT((byte) 0xD, "warm appliance", "W�rme");

    private final byte categoryID;
    private final String descriptionEN;
    private final String descriptionDE;


    EN50523Category(byte categoryID, String descriptionEN, String descriptionDE) {
        this.categoryID = categoryID;
        this.descriptionEN = descriptionEN;
        this.descriptionDE = descriptionDE;
    }


    public byte getCategoryID() {
        return this.categoryID;
    }


    public String getDescriptionEN() {
        return this.descriptionEN;
    }


    public String getDescriptionDE() {
        return this.descriptionDE;
    }


}
