package osh.openweathermap.prediction;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "population"
})
public class Sys implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5143705462140967057L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("population")
    private Integer population;

    /**
     * @return The population
     */
    @JsonProperty("population")
    public Integer getPopulation() {
        return population;
    }

    /**
     * @param population The population
     */
    @JsonProperty("population")
    public void setPopulation(Integer population) {
        this.population = population;
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
