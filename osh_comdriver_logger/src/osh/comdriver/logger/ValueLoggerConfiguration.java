package osh.comdriver.logger;

/**
 * @author Ingo Mauser
 */
public class ValueLoggerConfiguration {

    //  RRD database
    private final Boolean valueLoggingToRrdDatabaseActive;
    // value logger for power values etc.
    //  console
    private Boolean valueLoggingToConsoleActive;
    private Integer valueLoggingToConsoleResolution;
    //  file
    private Boolean valueLoggingToFileActive;
    private Integer valueLoggingToFileResolution;
    //  database
    private Boolean valueLoggingToDatabaseActive;
    private Integer valueLoggingToDatabaseResolution;
    private Integer valueLoggingToRrdDatabaseResolution;


    public ValueLoggerConfiguration(Boolean valueLoggingToConsoleActive,
                                    Integer valueLoggingToConsoleResolution,
                                    Boolean valueLoggingToFileActive,
                                    Integer valueLoggingToFileResolution,
                                    Boolean valueLoggingToDatabaseActive,
                                    Integer valueLoggingToDatabaseResolution,
                                    Boolean valueLoggingToRrdDatabaseActive,
                                    Integer valueLoggingToRrdDatabaseResolution) {
        super();

        this.valueLoggingToConsoleActive = valueLoggingToConsoleActive;
        this.valueLoggingToConsoleResolution = valueLoggingToConsoleResolution;

        this.valueLoggingToFileActive = valueLoggingToFileActive;
        this.valueLoggingToFileResolution = valueLoggingToFileResolution;

        this.valueLoggingToDatabaseActive = valueLoggingToDatabaseActive;
        this.valueLoggingToDatabaseResolution = valueLoggingToDatabaseResolution;

        this.valueLoggingToRrdDatabaseActive = valueLoggingToRrdDatabaseActive;
        this.valueLoggingToRrdDatabaseResolution = valueLoggingToRrdDatabaseResolution;
    }


    public Boolean getIsValueLoggingToConsoleActive() {
        return this.valueLoggingToConsoleActive;
    }

    public void setIsValueLoggingToConsoleActive(Boolean isValueLoggingToConsoleActive) {
        this.valueLoggingToConsoleActive = isValueLoggingToConsoleActive;
    }

    public Boolean getIsValueLoggingToFileActive() {
        return this.valueLoggingToFileActive;
    }

    public void setIsValueLoggingToFileActive(Boolean isValueLoggingToFileActive) {
        this.valueLoggingToFileActive = isValueLoggingToFileActive;
    }

    public Integer getValueLoggingToFileResolution() {
        return this.valueLoggingToFileResolution;
    }

    public void setValueLoggingToFileResolution(Integer valueLoggingToFileResolution) {
        this.valueLoggingToFileResolution = valueLoggingToFileResolution;
    }

    public Boolean getIsValueLoggingToDatabaseActive() {
        return this.valueLoggingToDatabaseActive;
    }

    public void setIsValueLoggingToDatabaseActive(Boolean isValueLoggingToDatabaseActive) {
        this.valueLoggingToDatabaseActive = isValueLoggingToDatabaseActive;
    }

    public Integer getValueLoggingToDatabaseResolution() {
        return this.valueLoggingToDatabaseResolution;
    }

    public void setValueLoggingToDatabaseResolution(Integer valueLoggingToDatabaseResolution) {
        this.valueLoggingToDatabaseResolution = valueLoggingToDatabaseResolution;
    }

    // Console

    public Integer getValueLoggingToConsoleResolution() {
        return this.valueLoggingToConsoleResolution;
    }

    public void setValueLoggingToConsoleResolution(
            Integer valueLoggingToConsoleResolution) {
        this.valueLoggingToConsoleResolution = valueLoggingToConsoleResolution;
    }

    // RRD Database

    public Boolean getValueLoggingToRrdDatabaseActive() {
        return this.valueLoggingToRrdDatabaseActive;
    }

    public Integer getValueLoggingToRrdDatabaseResolution() {
        return this.valueLoggingToRrdDatabaseResolution;
    }

    public void setValueLoggingToRrdDatabaseResolution(
            Integer valueLoggingToRrdDatabaseResolution) {
        this.valueLoggingToRrdDatabaseResolution = valueLoggingToRrdDatabaseResolution;
    }

}
