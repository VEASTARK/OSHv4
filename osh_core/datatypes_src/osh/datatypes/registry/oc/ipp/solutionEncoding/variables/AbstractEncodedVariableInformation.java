package osh.datatypes.registry.oc.ipp.solutionEncoding.variables;

import java.io.Serializable;

/**
 * Provides information about encoded variables needed for the optimization loop.
 *
 * @author Sebastian Kramer
 */
public abstract class AbstractEncodedVariableInformation implements Serializable {

    private static final long serialVersionUID = -4356785076279536509L;

    /**
     * Return if variables are needed by this object.
     * @return true if this object needs variables
     */
    public abstract boolean needsNoVariables();
}
