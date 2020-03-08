package org.uma.jmetal.algorithm.impl;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.stoppingrule.StoppingRule;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract class representing a PSO algorithm
 *
 * @author Antonio J. Nebro <antonio@lcc.uma.es>
 */
@SuppressWarnings("serial")
public abstract class AbstractParticleSwarmOptimization<S, Result> implements Algorithm<Result> {
    private List<S> swarm;
    private final List<StoppingRule> stoppingRules = new ArrayList<>();

    public List<S> getSwarm() {
        return this.swarm;
    }

    public void setSwarm(List<S> swarm) {
        this.swarm = swarm;
    }

    protected abstract void initProgress();

    protected abstract void updateProgress();

    protected abstract boolean isStoppingConditionReached();

    protected abstract List<S> createInitialSwarm();

    protected abstract List<S> evaluateSwarm(List<S> swarm);

    protected abstract void initializeLeader(List<S> swarm);

    protected abstract void initializeParticlesMemory(List<S> swarm);

    protected abstract void initializeVelocity(List<S> swarm);

    protected abstract void updateVelocity(List<S> swarm);

    protected abstract void updatePosition(List<S> swarm);

    protected abstract void perturbation(List<S> swarm);

    protected abstract void updateLeaders(List<S> swarm);

    protected abstract void updateParticlesMemory(List<S> swarm);

    @Override
    public abstract Result getResult();

    @Override
    public List<StoppingRule> getStoppingRules() {
        return this.stoppingRules;
    }

    @Override
    public void run() {
        this.swarm = this.createInitialSwarm();
        this.swarm = this.evaluateSwarm(this.swarm);
        this.initializeVelocity(this.swarm);
        this.initializeParticlesMemory(this.swarm);
        this.initializeLeader(this.swarm);
        this.initProgress();

        while (!this.isStoppingConditionReached()) {
            this.updateVelocity(this.swarm);
            this.updatePosition(this.swarm);
            this.perturbation(this.swarm);
            this.swarm = this.evaluateSwarm(this.swarm);
            this.updateLeaders(this.swarm);
            this.updateParticlesMemory(this.swarm);
            this.updateProgress();
        }
    }
}
