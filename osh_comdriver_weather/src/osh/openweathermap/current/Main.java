package osh.openweathermap.current;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"temp", "pressure", "humidity", "temp_min", "temp_max", "sea_level", "grnd_level"})
public class Main implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1939193954635065119L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("temp")
    private Double temp;
    @JsonProperty("pressure")
    private Double pressure;
    @JsonProperty("humidity")
    private Integer humidity;
    @JsonProperty("temp_min")
    private Double tempMin;
    @JsonProperty("temp_max")
    private Double tempMax;
    @JsonProperty("sea_level")
    private Double seaLevel;
    @JsonProperty("grnd_level")
    private Double grndLevel;

    /**
     * @return The temp
     */
    @JsonProperty("temp")
    public Double getTemp() {
        return temp;
    }

    /**
     * @param temp The temp
     */
    @JsonProperty("temp")
    public void setTemp(Double temp) {
        this.temp = temp;
    }

    /**
     * @return The pressure
     */
    @JsonProperty("pressure")
    public Double getPressure() {
        return pressure;
    }

    /**
     * @param pressure The pressure
     */
    @JsonProperty("pressure")
    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    /**
     * @return The humidity
     */
    @JsonProperty("humidity")
    public Integer getHumidity() {
        return humidity;
    }

    /**
     * @param humidity The humidity
     */
    @JsonProperty("humidity")
    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    /**
     * @return The tempMin
     */
    @JsonProperty("temp_min")
    public Double getTempMin() {
        return tempMin;
    }

    /**
     * @param tempMin The temp_min
     */
    @JsonProperty("temp_min")
    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    /**
     * @return The tempMax
     */
    @JsonProperty("temp_max")
    public Double getTempMax() {
        return tempMax;
    }

    /**
     * @param tempMax The temp_max
     */
    @JsonProperty("temp_max")
    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    /**
     * @return The seaLevel
     */
    @JsonProperty("sea_level")
    public Double getSeaLevel() {
        return seaLevel;
    }

    /**
     * @param seaLevel The sea_level
     */
    @JsonProperty("sea_level")
    public void setSeaLevel(Double seaLevel) {
        this.seaLevel = seaLevel;
    }

    /**
     * @return The grndLevel
     */
    @JsonProperty("grnd_level")
    public Double getGrndLevel() {
        return grndLevel;
    }

    /**
     * @param grndLevel The grnd_level
     */
    @JsonProperty("grnd_level")
    public void setGrndLevel(Double grndLevel) {
        this.grndLevel = grndLevel;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
