package osh.en50523;

/**
 * 3-bit HEX as BYTE (0 to 7)
 *
 * @author Ingo Mauser
 */
public enum EN50523Cluster {

    ALL((byte) 0x3, "all clusters", "allen Clustern gemeinsam"),
    HOUSEHOLD((byte) 0x6, "household cluster", "Haushaltscluster");

    private final byte clusterID;
    private final String descriptionEN;
    private final String descriptionDE;


    EN50523Cluster(byte clusterID, String descriptionEN, String descriptionDE) {
        this.clusterID = clusterID;
        this.descriptionEN = descriptionEN;
        this.descriptionDE = descriptionDE;
    }


    public byte getClusterID() {
        return this.clusterID;
    }


    public String getDescriptionEN() {
        return this.descriptionEN;
    }


    public String getDescriptionDE() {
        return this.descriptionDE;
    }


}
