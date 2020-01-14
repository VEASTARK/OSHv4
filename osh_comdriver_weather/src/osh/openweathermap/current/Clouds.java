package osh.openweathermap.current;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "all"
})
public class Clouds implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 114087702500001318L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("all")
    private Integer all;

    /**
     * @return The all
     */
    @JsonProperty("all")
    public Integer getAll() {
        return all;
    }

    /**
     * @param all The all
     */
    @JsonProperty("all")
    public void setAll(Integer all) {
        this.all = all;
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
