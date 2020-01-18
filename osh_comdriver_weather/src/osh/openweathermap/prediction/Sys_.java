package osh.openweathermap.prediction;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
        "pod"
})
public class Sys_ implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2396860001716780611L;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<String, Object>();
    @JsonProperty("pod")
    private String pod;

    /**
     * @return The pod
     */
    @JsonProperty("pod")
    public String getPod() {
        return pod;
    }

    /**
     * @param pod The pod
     */
    @JsonProperty("pod")
    public void setPod(String pod) {
        this.pod = pod;
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
