package osh.datatypes.registry.oc.ipp.solutionEncoding.variables;

/**
 * Provides information about encoded variables needed for the optimization loop.
 *
 * @author Sebastian Kramer
 */
public abstract class AbstractEncodedVariableInformation {

    /**
     * Return if variables are needed by this object.
     * @return true if this object needs variables
     */
    public abstract boolean needsNoVariables();
}
