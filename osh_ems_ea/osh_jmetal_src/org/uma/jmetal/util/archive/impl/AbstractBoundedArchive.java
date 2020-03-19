package org.uma.jmetal.util.archive.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.archive.Archive;
import org.uma.jmetal.util.archive.BoundedArchive;

import java.util.List;

/**
 * @param <S>
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractBoundedArchive<S extends Solution<?>> implements BoundedArchive<S> {
    protected final NonDominatedSolutionListArchive<S> archive;
    protected final int maxSize;

    public AbstractBoundedArchive(int maxSize) {
        this.maxSize = maxSize;
        this.archive = new NonDominatedSolutionListArchive<>();
    }

    @Override
    public boolean add(S solution) {
        boolean success = this.archive.add(solution);
        if (success) {
            this.prune();
        }

        return success;
    }

    @Override
    public S get(int index) {
        return this.getSolutionList().get(index);
    }

    @Override
    public List<S> getSolutionList() {
        return this.archive.getSolutionList();
    }

    @Override
    public int size() {
        return this.archive.size();
    }

    @Override
    public int getMaxSize() {
        return this.maxSize;
    }

    public abstract void prune();

    public Archive<S> join(Archive<S> archive) {
        for (S solution : archive.getSolutionList()) {
            this.add(solution);
        }

        return archive;
    }
}
