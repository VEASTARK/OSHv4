package org.uma.jmetal.algorithm.multiobjective.moead;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.algorithm.multiobjective.moead.util.MOEADUtils;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.operator.MutationOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.JMetalException;
import org.uma.jmetal.util.point.impl.IdealPoint;
import org.uma.jmetal.util.point.impl.NadirPoint;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Abstract class for implementing versions of the MOEA/D algorithm.
 *
 * @author Antonio J. Nebro
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractMOEAD<S extends Solution<?>> implements Algorithm<List<S>> {
    protected final Problem<S> problem;
    /**
     * T in Zhang & Li paper
     */
    protected final int neighborSize;
    /**
     * Delta in Zhang & Li paper
     */
    protected final double neighborhoodSelectionProbability;
    /**
     * nr in Zhang & Li paper
     */
    protected final int maximumNumberOfReplacedSolutions;
    protected final Solution<?>[] indArray;
    protected final FunctionType functionType;
    protected final String dataDirectory;
    protected final int populationSize;
    protected final int resultPopulationSize;
    protected final int maxEvaluations;
    protected final JMetalRandom randomGenerator;
    protected final CrossoverOperator<S> crossoverOperator;
    protected final MutationOperator<S> mutationOperator;
    /**
     * Z vector in Zhang & Li paper
     */
    protected IdealPoint idealPoint;
    // nadir point
    protected NadirPoint nadirPoint;
    /**
     * Lambda vectors
     */
    protected double[][] lambda;
    protected int[][] neighborhood;
    protected List<S> population;
    protected List<S> offspringPopulation;
    protected List<S> jointPopulation;
    protected int evaluations;

    public AbstractMOEAD(Problem<S> problem, int populationSize, int resultPopulationSize,
                         int maxEvaluations, CrossoverOperator<S> crossoverOperator, MutationOperator<S> mutation,
                         FunctionType functionType, String dataDirectory, double neighborhoodSelectionProbability,
                         int maximumNumberOfReplacedSolutions, int neighborSize) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.resultPopulationSize = resultPopulationSize;
        this.maxEvaluations = maxEvaluations;
        this.mutationOperator = mutation;
        this.crossoverOperator = crossoverOperator;
        this.functionType = functionType;
        this.dataDirectory = dataDirectory;
        this.neighborhoodSelectionProbability = neighborhoodSelectionProbability;
        this.maximumNumberOfReplacedSolutions = maximumNumberOfReplacedSolutions;
        this.neighborSize = neighborSize;

        this.randomGenerator = JMetalRandom.getInstance();

        this.population = new ArrayList<>(populationSize);
        this.indArray = new Solution[problem.getNumberOfObjectives()];
        this.neighborhood = new int[populationSize][neighborSize];
        this.idealPoint = new IdealPoint(problem.getNumberOfObjectives());
        this.nadirPoint = new NadirPoint(problem.getNumberOfObjectives());
        this.lambda = new double[populationSize][problem.getNumberOfObjectives()];
    }

    /**
     * Initialize weight vectors
     */
    protected void initializeUniformWeight() {
        if ((this.problem.getNumberOfObjectives() == 2) && (this.populationSize <= 300)) {
            for (int n = 0; n < this.populationSize; n++) {
                double a = 1.0 * n / (this.populationSize - 1);
                this.lambda[n][0] = a;
                this.lambda[n][1] = 1 - a;
            }
        } else {
            String dataFileName;
            dataFileName = "W" + this.problem.getNumberOfObjectives() + "D_" +
                    this.populationSize + ".dat";

            try {

                //       String path =
                // Paths.get(VectorFileUtils.class.getClassLoader().getResource(filePath).toURI()).toString
                // ();
                InputStream in =
                        this.getClass()
                                .getClassLoader()
                                .getResourceAsStream(this.dataDirectory + "/" + dataFileName);
                InputStreamReader isr = new InputStreamReader(in);
                BufferedReader br = new BufferedReader(isr);

                int i = 0;
                int j;
                String aux = br.readLine();
                while (aux != null) {
                    StringTokenizer st = new StringTokenizer(aux);
                    j = 0;
                    while (st.hasMoreTokens()) {
                        double value = Double.parseDouble(st.nextToken());
                        this.lambda[i][j] = value;
                        j++;
                    }
                    aux = br.readLine();
                    i++;
                }
                br.close();
            } catch (Exception e) {
                throw new JMetalException("initializeUniformWeight: failed when reading for file: "
                        + this.dataDirectory + "/" + dataFileName, e);
            }
        }
    }

    /**
     * Initialize neighborhoods
     */
    protected void initializeNeighborhood() {
        double[] x = new double[this.populationSize];
        int[] idx = new int[this.populationSize];

        for (int i = 0; i < this.populationSize; i++) {
            // calculate the distances based on weight vectors
            for (int j = 0; j < this.populationSize; j++) {
                x[j] = MOEADUtils.distVector(this.lambda[i], this.lambda[j]);
                idx[j] = j;
            }

            // find 'niche' nearest neighboring subproblems
            MOEADUtils.minFastSort(x, idx, this.populationSize, this.neighborSize);

            System.arraycopy(idx, 0, this.neighborhood[i], 0, this.neighborSize);
        }
    }

    protected NeighborType chooseNeighborType() {
        double rnd = this.randomGenerator.nextDouble();
        NeighborType neighborType;

        if (rnd < this.neighborhoodSelectionProbability) {
            neighborType = NeighborType.NEIGHBOR;
        } else {
            neighborType = NeighborType.POPULATION;
        }
        return neighborType;
    }

    protected List<S> parentSelection(int subProblemId, NeighborType neighborType) {
        List<Integer> matingPool = this.matingSelection(subProblemId, 2, neighborType);

        List<S> parents = new ArrayList<>(3);

        parents.add(this.population.get(matingPool.get(0)));
        parents.add(this.population.get(matingPool.get(1)));
        parents.add(this.population.get(subProblemId));

        return parents;
    }

    /**
     * @param subproblemId  the id of current subproblem
     * @param neighbourType neighbour type
     */
    protected List<Integer> matingSelection(int subproblemId, int numberOfSolutionsToSelect, NeighborType neighbourType) {
        int neighbourSize;
        int selectedSolution;

        List<Integer> listOfSolutions = new ArrayList<>(numberOfSolutionsToSelect);

        neighbourSize = this.neighborhood[subproblemId].length;
        while (listOfSolutions.size() < numberOfSolutionsToSelect) {
            if (neighbourType == NeighborType.NEIGHBOR) {
                int random = this.randomGenerator.nextInt(0, neighbourSize - 1);
                selectedSolution = this.neighborhood[subproblemId][random];
            } else {
                selectedSolution = this.randomGenerator.nextInt(0, this.populationSize - 1);
            }
            boolean flag = true;
            for (Integer individualId : listOfSolutions) {
                if (individualId == selectedSolution) {
                    flag = false;
                    break;
                }
            }

            if (flag) {
                listOfSolutions.add(selectedSolution);
            }
        }

        return listOfSolutions;
    }

    /**
     * Update neighborhood method
     *
     * @param individual
     * @param subProblemId
     * @param neighborType
     * @throws JMetalException
     */
    @SuppressWarnings("unchecked")
    protected void updateNeighborhood(S individual, int subProblemId, NeighborType neighborType) throws JMetalException {
        int size;
        int time;

        time = 0;

        if (neighborType == NeighborType.NEIGHBOR) {
            size = this.neighborhood[subProblemId].length;
        } else {
            size = this.population.size();
        }
        int[] perm = new int[size];

        MOEADUtils.randomPermutation(perm, size);

        for (int i = 0; i < size; i++) {
            int k;
            if (neighborType == NeighborType.NEIGHBOR) {
                k = this.neighborhood[subProblemId][perm[i]];
            } else {
                k = perm[i];
            }
            double f1, f2;

            f1 = this.fitnessFunction(this.population.get(k), this.lambda[k]);
            f2 = this.fitnessFunction(individual, this.lambda[k]);

            if (f2 < f1) {
                this.population.set(k, (S) individual.copy());
                time++;
            }

            if (time >= this.maximumNumberOfReplacedSolutions) {
                return;
            }
        }
    }

    double fitnessFunction(S individual, double[] lambda) throws JMetalException {
        double fitness;

        if (FunctionType.TCHE == this.functionType) {
            double maxFun = -1.0e+30;

            for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
                double diff = Math.abs(individual.getObjective(n) - this.idealPoint.getValue(n));

                double feval;
                if (lambda[n] == 0) {
                    feval = 0.0001 * diff;
                } else {
                    feval = diff * lambda[n];
                }
                if (feval > maxFun) {
                    maxFun = feval;
                }
            }

            fitness = maxFun;
        } else if (FunctionType.AGG == this.functionType) {
            double sum = 0.0;
            for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
                sum += (lambda[n]) * individual.getObjective(n);
            }

            fitness = sum;

        } else if (FunctionType.PBI == this.functionType) {
            double d1, d2, nl;
            double theta = 5.0;

            d1 = d2 = nl = 0.0;

            for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
                d1 += (individual.getObjective(i) - this.idealPoint.getValue(i)) * lambda[i];
                nl += Math.pow(lambda[i], 2.0);
            }
            nl = Math.sqrt(nl);
            d1 = Math.abs(d1) / nl;

            for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
                d2 += Math.pow((individual.getObjective(i) - this.idealPoint.getValue(i)) - d1 * (lambda[i] / nl), 2.0);
            }
            d2 = Math.sqrt(d2);

            fitness = (d1 + theta * d2);
        } else {
            throw new JMetalException(" MOEAD.fitnessFunction: unknown type " + this.functionType);
        }
        return fitness;
    }

    @Override
    public List<S> getResult() {
        if (this.populationSize > this.resultPopulationSize) {
            return MOEADUtils.getSubsetOfEvenlyDistributedSolutions(this.population, this.resultPopulationSize);
        } else {
            return this.population;
        }
    }

    protected enum NeighborType {NEIGHBOR, POPULATION}

    public enum FunctionType {TCHE, PBI, AGG}
}
