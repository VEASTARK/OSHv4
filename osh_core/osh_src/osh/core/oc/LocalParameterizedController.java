package osh.core.oc;

import osh.core.interfaces.IOSHOC;

import java.util.HashMap;
import java.util.Map;


@Deprecated
public abstract class LocalParameterizedController extends LocalController {

    private Map<String, String> runtimeParameters = new HashMap<>();

    public LocalParameterizedController(IOSHOC osh) {
        super(osh);
    }

    /**
     * Get all runtime parameters for local unit (like dof)
     *
     * @return
     */
    public Map<String, String> getRuntimeParameters() {
        return this.runtimeParameters;
    }

    /**
     * Set all runtime parameters for local unit
     *
     * @return
     */
    public void setRuntimeParameters(Map<String, String> runtimeParameters) {
        this.runtimeParameters = runtimeParameters;
    }

    /**
     * Get one runtime parameter for local unit (like dof)
     *
     * @return
     */
    public String getRuntimeParameter(String key) {
        return this.runtimeParameters.get(key);
    }

    /**
     * Set one runtime parameter for local unit
     *
     * @return
     */
    public void setRuntimeParameters(String key, String value) {
        this.runtimeParameters.put(key, value);
    }


}
