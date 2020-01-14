package osh.openweathermap.prediction;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "speed",
        "deg"
})
public class Wind implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6011525204076144921L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("speed")
    private Double speed;
    @JsonProperty("deg")
    private Double deg;

    /**
     * @return The speed
     */
    @JsonProperty("speed")
    public Double getSpeed() {
        return speed;
    }

    /**
     * @param speed The speed
     */
    @JsonProperty("speed")
    public void setSpeed(Double speed) {
        this.speed = speed;
    }

    /**
     * @return The deg
     */
    @JsonProperty("deg")
    public Double getDeg() {
        return deg;
    }

    /**
     * @param deg The deg
     */
    @JsonProperty("deg")
    public void setDeg(Double deg) {
        this.deg = deg;
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
