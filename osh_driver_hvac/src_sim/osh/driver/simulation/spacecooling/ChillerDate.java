package osh.driver.simulation.spacecooling;

/**
 * @author Julian Feder, Ingo Mauser
 */
public class ChillerDate {

    private final long startTimestamp;
    private final long length;
    private final int amountOfPerson;
    private final double setTemperature;
    private final int knownPower;


    /**
     * CONSTRUCTOR
     */
    public ChillerDate(long startTimestamp, long length, int amountOfPerson, double setTemperature, int knownPower) {
        this.startTimestamp = startTimestamp;
        this.length = length;
        this.amountOfPerson = amountOfPerson;
        this.setTemperature = setTemperature;
        this.knownPower = knownPower;
    }


    //GETTER METHODS
    public long getStartTimestamp() {
        return this.startTimestamp;
    }

    public long getLength() {
        return this.length;
    }

    public int getAmountOfPerson() {
        return this.amountOfPerson;
    }

    public double getSetTemperature() {
        return this.setTemperature;
    }

    public int getKnownPower() {
        return this.knownPower;
    }

}