package osh.openweathermap.prediction;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"3h"})
public class Snow implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 6359777993443251110L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("3h")
    private Integer _3h;

    /**
     * @return The _3h
     */
    @JsonProperty("3h")
    public Integer get3h() {
        return _3h;
    }

    /**
     * @param _3h The 3h
     */
    @JsonProperty("3h")
    public void set3h(Integer _3h) {
        this._3h = _3h;
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