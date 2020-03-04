package org.uma.jmetal.util.solutionattribute.impl;

import org.uma.jmetal.solution.Solution;

@SuppressWarnings("serial")
public final class SolutionTextRepresentation extends GenericSolutionAttribute<Solution<?>, String> {

    private static SolutionTextRepresentation singleInstance;

    private SolutionTextRepresentation() {
    }

    public static SolutionTextRepresentation getAttribute() {
        if (singleInstance == null)
            singleInstance = new SolutionTextRepresentation();
        return singleInstance;
    }
}
