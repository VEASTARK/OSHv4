//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.uma.jmetal.algorithm.multiobjective.cdg;

import org.uma.jmetal.algorithm.Algorithm;
import org.uma.jmetal.operator.CrossoverOperator;
import org.uma.jmetal.problem.Problem;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.Ranking;
import org.uma.jmetal.util.solutionattribute.impl.DominanceRanking;

import java.util.*;

/**
 * Abstract class for implementing versions of the CDG algorithm.
 *
 * @author Feng Zhang
 * @version 1.0
 */
@SuppressWarnings("serial")
public abstract class AbstractCDG<S extends Solution<?>> implements Algorithm<List<S>> {

    protected final Problem<S> problem;
    /**
     * Z vector in Zhang & Li paper
     */
    protected final double[] idealPoint;
    // nadir point
    protected final double[] nadirPoint;
    protected final int[][] neighborhood;
    protected final int[] neighborhoodNum;
    protected final int childGrid_;
    protected final int childGridNum_;
    protected final int[][] subP;
    protected final int[] subPNum;
    protected final List<List<Integer>> team;
    /**
     * Delta in Zhang & Li paper
     */
    protected final double neighborhoodSelectionProbability;
    protected final double[] d_;
    protected final double sigma_;
    protected final int k_;
    protected final int t_;
    protected final int subproblemNum_;
    protected final int borderLength;
    protected final int slimDetal_;
    protected final int[] badSolution;
    protected final int[] gridDetal_;
    protected final double[][] gridDetalSum_;
    protected final List<S> population;
    protected final List<S> badPopulation;
    protected final List<S> specialPopulation;
    protected final List<Integer> spPopulationOrder;
    protected final List<List<S>> subproblem;
    protected final List<List<S>> tempBorder;
    protected final List<List<S>> border;
    protected final int populationSize;
    protected final int resultPopulationSize;
    protected final int maxEvaluations;
    protected final JMetalRandom randomGenerator;
    protected final CrossoverOperator<S> crossoverOperator;
    protected int badSolutionNum;
    protected int evaluations;

    public AbstractCDG(Problem<S> problem, int populationSize, int resultPopulationSize, int maxEvaluations,
                       CrossoverOperator<S> crossoverOperator, double neighborhoodSelectionProbability,
                       double sigma_, int k_, int t_, int subproblemNum_, int childGrid_, int childGridNum_) {
        this.problem = problem;
        this.populationSize = populationSize;
        this.resultPopulationSize = resultPopulationSize;
        this.maxEvaluations = maxEvaluations;
        this.crossoverOperator = crossoverOperator;
        this.neighborhoodSelectionProbability = neighborhoodSelectionProbability;
        this.sigma_ = sigma_;
        this.k_ = k_;
        this.t_ = t_;
        this.subproblemNum_ = subproblemNum_;
        this.childGrid_ = childGrid_;
        this.childGridNum_ = childGridNum_;

        this.randomGenerator = JMetalRandom.getInstance();

        this.population = new ArrayList<>(populationSize);

        this.badPopulation = new ArrayList<>(2 * populationSize);

        this.specialPopulation = new ArrayList<>(2 * populationSize);
        this.spPopulationOrder = new ArrayList<>(2 * populationSize);

        this.neighborhood = new int[populationSize][populationSize];
        this.neighborhoodNum = new int[populationSize];

        this.idealPoint = new double[problem.getNumberOfObjectives()];
        this.nadirPoint = new double[problem.getNumberOfObjectives()];

        this.d_ = new double[problem.getNumberOfObjectives()];

        this.subproblem = new ArrayList<>(subproblemNum_);

        for (int i = 0; i < subproblemNum_; i++) {
            List<S> list = new ArrayList<>(populationSize);
            this.subproblem.add(list);
        }

        int subPLength = (int) Math.pow(3, problem.getNumberOfObjectives());
        this.subP = new int[childGridNum_][subPLength];
        this.subPNum = new int[childGridNum_];
        this.team = new ArrayList<>(childGridNum_);
        for (int i = 0; i < childGridNum_; i++) {
            List<Integer> list = new ArrayList<>(populationSize);
            this.team.add(list);
        }

        this.slimDetal_ = k_ - 3;
        this.badSolution = new int[2 * populationSize];
        this.gridDetal_ = new int[k_];
        this.gridDetalSum_ = new double[problem.getNumberOfObjectives()][k_];

        this.tempBorder = new ArrayList<>(problem.getNumberOfObjectives());
        this.border = new ArrayList<>(problem.getNumberOfObjectives());
        this.borderLength = 2 * populationSize * (problem.getNumberOfObjectives() - 1);
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            List<S> list = new ArrayList<>(this.borderLength);
            this.tempBorder.add(list);
        }
        for (int i = 0; i < problem.getNumberOfObjectives(); i++) {
            List<S> list = new ArrayList<>(this.borderLength);
            this.border.add(list);
        }

    }

    protected void initialCDGAttributes(S individual) {
        int[] g_ = new int[this.problem.getNumberOfObjectives()];
        int[] rank_ = new int[this.problem.getNumberOfObjectives()];
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            g_[i] = 0;
            rank_[i] = 0;
        }
        int order_ = 0;

        individual.setAttribute("g_", g_);
        individual.setAttribute("rank_", rank_);
        individual.setAttribute("order_", order_);
    }

    protected int getG(S individual, int index) {
        int[] g_ = (int[]) individual.getAttribute("g_");
        return g_[index];
    }

    protected int getRank(S individual, int index) {
        int[] rank_ = (int[]) individual.getAttribute("rank_");
        return rank_[index];
    }

    protected int getOrder(S individual) {
        return (int) individual.getAttribute("order_");
    }

    protected void setG(S individual, int index, int value) {
        int[] g_ = (int[]) individual.getAttribute("g_");
        g_[index] = value;
        individual.setAttribute("g_", g_);
    }

    protected void setRank(S individual, int index, int value) {
        int[] rank_ = (int[]) individual.getAttribute("rank_");
        rank_[index] = value;
        individual.setAttribute("rank_", rank_);
    }

    protected void setOrder(S individual, int value) {
        individual.setAttribute("order_", value);
    }

    protected void updateNeighborhood() {
        if (this.problem.getNumberOfObjectives() == 2) {
            this.initializeSubP2();
            this.group2();
            this.initializeNeighborhoodGrid();
        } else if (this.problem.getNumberOfObjectives() == 3) {
            this.initializeSubP3();
            this.group3();
            this.initializeNeighborhoodGrid();
        } else {
            this.initializeNeighborhood();
        }
    }

    /**
     * Initialize cdg neighborhoods
     */
    protected void initializeNeighborhood() {

        for (int i = 0; i < this.populationSize; i++) {
            this.neighborhoodNum[i] = 0;
        }

        for (int i = 0; i < this.populationSize; i++)
            for (int j = 0; j < this.populationSize; j++) {
                int gridDistance = 0;
                for (int k = 0; k < this.problem.getNumberOfObjectives(); k++) {
                    int g1 = this.getG(this.population.get(i), k);
                    int g2 = this.getG(this.population.get(j), k);

                    int tempGridDistance = Math.abs(g1 - g2);
                    if (tempGridDistance > gridDistance)
                        gridDistance = tempGridDistance;
                }
                if (gridDistance < this.t_) {
                    this.neighborhood[i][this.neighborhoodNum[i]] = j;
                    this.neighborhoodNum[i]++;
                }
            }

    }

    protected void initializeSubP2() {
        int[] left = new int[this.problem.getNumberOfObjectives()];
        int[] right = new int[this.problem.getNumberOfObjectives()];
        int s;
        int ns;

        for (int i = 1; i < this.childGridNum_; i++)
            this.subPNum[i] = 0;

        for (int i = 1; i <= this.childGrid_; i++)
            for (int j = 1; j <= this.childGrid_; j++) {

                s = this.getPos(i, j, 1);

                left[0] = i - this.t_;
                right[0] = i + this.t_;
                left[1] = j - this.t_;
                right[1] = j + this.t_;

                for (int d = 0; d < this.problem.getNumberOfObjectives(); d++) {
                    if (left[d] < 1) {
                        left[d] = 1;
                        right[d] = 2 * this.t_ + 1;
                    }
                    if (right[d] > this.childGrid_) {
                        right[d] = this.childGrid_;
                        left[d] = this.childGrid_ - 2 * this.t_;
                    }
                }
                for (int ni = left[0]; ni <= right[0]; ni++)
                    for (int nj = left[1]; nj <= right[1]; nj++) {

                        ns = this.getPos(ni, nj, 1);

                        this.subP[s][this.subPNum[s]] = ns;
                        this.subPNum[s]++;
                    }

            }
    }

    protected void initializeSubP3() {
        int[] left = new int[this.problem.getNumberOfObjectives()];
        int[] right = new int[this.problem.getNumberOfObjectives()];
        int s;
        int ns;

        for (int i = 1; i < this.childGridNum_; i++)
            this.subPNum[i] = 0;

        for (int i = 1; i <= this.childGrid_; i++)
            for (int j = 1; j <= this.childGrid_; j++) {
                for (int k = 1; k <= this.childGrid_; k++) {

                    s = this.getPos(i, j, k);

                    left[0] = i - this.t_;
                    right[0] = i + this.t_;
                    left[1] = j - this.t_;
                    right[1] = j + this.t_;
                    left[2] = k - this.t_;
                    right[2] = k + this.t_;

                    for (int d = 0; d < this.problem.getNumberOfObjectives(); d++) {
                        if (left[d] < 1) {
                            left[d] = 1;
                            right[d] = 2 * this.t_ + 1;
                        }
                        if (right[d] > this.childGrid_) {
                            right[d] = this.childGrid_;
                            left[d] = this.childGrid_ - 2 * this.t_;
                        }
                    }
                    for (int ni = left[0]; ni <= right[0]; ni++)
                        for (int nj = left[1]; nj <= right[1]; nj++)
                            for (int nk = left[2]; nk <= right[2]; nk++) {
                                ns = this.getPos(ni, nj, nk);
                                this.subP[s][this.subPNum[s]] = ns;
                                this.subPNum[s]++;
                            }
                }
            }
    }

    protected int getPos(int i, int j, int k) {
        int s;
        int l = (int) Math.pow(this.childGrid_, 2);
        l *= (k - 1);
        if (i >= j) {
            s = (i - 1) * (i - 1) + j;
        } else {
            s = j * j - i + 1;
        }
        s += l;
        return s;
    }

    protected void group2() {
        double[] childDelta = new double[this.problem.getNumberOfObjectives()];
        double[] maxFunValue = new double[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            maxFunValue[i] = 0;

        for (S s : this.population) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (s.getObjective(j) > maxFunValue[j])
                    maxFunValue[j] = s.getObjective(j);
            }
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            childDelta[i] = (maxFunValue[i] - this.idealPoint[i]) / this.childGrid_;
        }

        for (int i = 1; i < this.childGridNum_; i++)
            this.team.get(i).clear();

        int grid;
        double childSigma = 1.0e-10;
        int[] pos = new int[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.populationSize; i++) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                double nornalObj = this.population.get(i).getObjective(j) - this.idealPoint[j];
                pos[j] = (int) Math.ceil(nornalObj / childDelta[j] - childSigma);
            }
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                if (pos[j] < 1)
                    pos[j] = 1;

            grid = this.getPos(pos[0], pos[1], 1);

            if (grid < 1)
                grid = 1;
            if (grid > this.childGridNum_ - 1)
                grid = this.childGridNum_ - 1;
            this.team.get(grid).add(i);
        }
    }

    protected void group3() {
        double[] childDelta = new double[this.problem.getNumberOfObjectives()];
        double[] maxFunValue = new double[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            maxFunValue[i] = 0;

        for (S s : this.population) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (s.getObjective(j) > maxFunValue[j])
                    maxFunValue[j] = s.getObjective(j);
            }
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            childDelta[i] = (maxFunValue[i] - this.idealPoint[i]) / this.childGrid_;
        }

        for (int i = 1; i < this.childGridNum_; i++)
            this.team.get(i).clear();

        int grid;
        double childSigma = 1.0e-10;
        int[] pos = new int[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.populationSize; i++) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                double nornalObj = this.population.get(i).getObjective(j) - this.idealPoint[j];
                pos[j] = (int) Math.ceil(nornalObj / childDelta[j] - childSigma);
            }
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                if (pos[j] < 1)
                    pos[j] = 1;

            grid = this.getPos(pos[0], pos[1], pos[2]);

            if (grid < 1)
                grid = 1;
            if (grid > this.childGridNum_ - 1)
                grid = this.childGridNum_ - 1;
            this.team.get(grid).add(i);
        }
    }

    protected void initializeNeighborhoodGrid() {

        for (int i = 0; i < this.populationSize; i++) {
            this.neighborhoodNum[i] = 0;
        }

        for (int i = 1; i < this.childGridNum_; i++) {
            for (int j = 0; j < this.team.get(i).size(); j++) {
                int parentIndex = this.team.get(i).get(j);
                if (this.subP.length < i || this.subPNum[i] == 0) {
                    this.neighborhoodNum[parentIndex] = 0;
                } else {
                    for (int ni = 0; ni < this.subPNum[i]; ni++) {
                        for (int nj = 0; nj < this.team.get(this.subP[i][ni]).size(); nj++) {
                            this.neighborhood[parentIndex][this.neighborhoodNum[parentIndex]] = this.team.get(this.subP[i][ni]).get(nj);
                            this.neighborhoodNum[parentIndex]++;
                        }
                    }
                }
            }
        }
    }

    protected void updateBorder() {
        this.getBorder();

        this.paretoFilter();

        double coefficient = 1 + (1 - this.evaluations / this.maxEvaluations) * 0.15;
        double borderCoef = 1 + (coefficient - 1) / 4;
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            for (int j = 0; j < this.border.get(i).size(); j++)
                for (int k = 0; k < this.problem.getNumberOfObjectives(); k++)
                    if (i != k) {
                        double funValue = this.border.get(i).get(j).getObjective(k);
                        this.border.get(i).get(j).setObjective(k, funValue * borderCoef);
                    }

    }

    protected void getBorder() {
        int[] flag = new int[this.problem.getNumberOfObjectives()];
        double[] minFunValue = new double[this.problem.getNumberOfObjectives()];

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.tempBorder.get(i).clear();
            minFunValue[i] = 1.0e+30;
        }

        for (S value : this.population)
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                if (value.getObjective(j) < minFunValue[j])
                    minFunValue[j] = value.getObjective(j);

        int sum;
        int od;
        for (S s : this.population) {
            sum = 0;
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (s.getObjective(j) < minFunValue[j] + this.nadirPoint[j] / 100)
                    flag[j] = 1;
                else
                    flag[j] = 0;
                sum += flag[j];
            }
            if (sum == 1) {
                od = 0;
                for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                    if (flag[j] == 1) {
                        od = j;
                        break;
                    }
                this.tempBorder.get(od).add(s);
            }
        }
    }

    protected void paretoFilter() {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            this.border.get(i).clear();

        boolean tag;
        int nmbOfObjs = this.problem.getNumberOfObjectives() - 1;
        int sum1, sum2;

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            for (int p = 0; p < this.tempBorder.get(i).size(); p++) {
                tag = false;
                for (int q = 0; q < this.tempBorder.get(i).size(); q++) {
                    sum1 = 0;
                    sum2 = 0;
                    for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                        if (i != j) {
                            if (this.tempBorder.get(i).get(p).getObjective(j)
                                    <= this.tempBorder.get(i).get(q).getObjective(j))
                                sum1++;
                            if (this.tempBorder.get(i).get(p).getObjective(j)
                                    < this.tempBorder.get(i).get(q).getObjective(j))
                                sum2++;
                        }
                    }
                    if (sum1 == nmbOfObjs && sum2 > 0) {
                        tag = true;
                        break;
                    }
                }
                if (tag) {
                    this.border.get(i).add(this.tempBorder.get(i).get(p));
                }
            }

    }

    protected void initializeIdealPoint() {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.idealPoint[i] = 1.0e+30;
        }

        for (int i = 0; i < this.populationSize; i++) {
            this.updateIdealPoint(this.population.get(i));
        }
    }

    // initialize the nadir point
    protected void initializeNadirPoint() {
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            this.nadirPoint[i] = -1.0e+30;
        this.updateNadirPoint();
    }

    // update the current nadir point
    void updateNadirPoint() {
        Ranking<S> ranking = new DominanceRanking<>();
        ranking.computeRanking(this.population);
        List<S> nondominatedPopulation = ranking.getSubfront(0);

        for (S individual : nondominatedPopulation) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (individual.getObjective(j) > this.nadirPoint[j]) {
                    this.nadirPoint[j] = individual.getObjective(j);
                }
            }
        }
    }

    protected void updateIdealPoint(S individual) {
        for (int n = 0; n < this.problem.getNumberOfObjectives(); n++) {
            if (individual.getObjective(n) < this.idealPoint[n]) {
                this.idealPoint[n] = individual.getObjective(n);
            }
        }
    }

    protected void gridSystemSetup() {
        double coefficient = 1 + (1 - this.evaluations / this.maxEvaluations) * 0.15;
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            this.d_[i] = (this.nadirPoint[i] - this.idealPoint[i]) * coefficient / this.k_;
        }
        for (S s : this.population) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                int g = (int) Math.ceil((s.getObjective(j) - this.idealPoint[j]) / this.d_[j]);
                if (g < 0)
                    g = 0;
                if (g >= this.k_)
                    g = this.k_ - 1;
                this.setG(s, j, g);
            }
        }
    }

    protected void gridSystemSetup3() {
        this.initialGridDetal();
        for (S s : this.population) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                int g = this.getGridPos(j, s.getObjective(j));
                this.setG(s, j, g);
            }
        }
    }

    protected void initialGridDetal() {
        int detalSum = 0;
        this.gridDetal_[0] = -1;
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            this.gridDetalSum_[i][0] = this.gridDetal_[0];

        for (int i = 1; i < this.slimDetal_; i++) {
            this.gridDetal_[i] = 2;
            detalSum += this.gridDetal_[i];
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                this.gridDetalSum_[j][i] = detalSum;
        }

        for (int i = this.slimDetal_; i < this.k_; i++) {
            this.gridDetal_[i] = 1;
            detalSum += this.gridDetal_[i];
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                this.gridDetalSum_[j][i] = detalSum;
        }

        double coefficient = 1 + (1 - this.evaluations / this.maxEvaluations) * 0.15;

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++)
            this.d_[i] = (this.nadirPoint[i] - this.idealPoint[i]) * coefficient / this.k_;

        for (int i = 0; i < this.k_; i++)
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++)
                this.gridDetalSum_[j][i] = this.gridDetalSum_[j][i] / detalSum * (this.d_[j] * this.k_);

    }

    protected int getGridPos(int j, double funValue) {
        int g = 0;
        for (int i = 0; i < this.k_; i++)
            if (funValue > this.gridDetalSum_[j][i])
                g++;
        if (g == this.k_)
            g = this.k_ - 1;
        return g;
    }

    protected void chooseSpecialPopulation() {
        this.spPopulationOrder.clear();
        this.specialPopulation.clear();

        Map<Integer, S> specialSolution = new HashMap<>();

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            for (int j = 0; j < this.population.size(); j++) {
                if (this.population.get(j).getObjective(i) == this.idealPoint[i]) {
                    if (!specialSolution.containsKey(j)) {
                        specialSolution.put(j, this.population.get(j));
                    }
                }
            }
        }

        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            for (int j = 0; j < this.population.size(); j++) {
                if (this.population.get(j).getObjective(i) == this.nadirPoint[i]) {
                    if (!specialSolution.containsKey(j)) {
                        specialSolution.put(j, this.population.get(j));
                    }
                }
            }
        }


        for (Map.Entry<Integer, S> entry : specialSolution.entrySet()) {
            this.specialPopulation.add(entry.getValue());
            this.spPopulationOrder.add(entry.getKey());
        }

        if (this.specialPopulation.size() > 2 * this.problem.getNumberOfObjectives()) {
            for (int i = this.specialPopulation.size() - 1; i > (2 * this.problem.getNumberOfObjectives() - 1); i--) {
                this.specialPopulation.remove(i);
                this.spPopulationOrder.remove(i);
            }
        }

    }

    protected void excludeBadSolution() {
        this.badPopulation.clear();
        int length = this.population.size();
        for (int i = length - 1; i >= 0; i--) {
            S individual = this.population.get(i);
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                if (individual.getObjective(j) > this.nadirPoint[j]) {
                    this.badPopulation.add(individual);
                    this.population.remove(i);
                    break;
                }
            }
        }
    }

    protected void excludeBadSolution3() {
        this.badSolutionNum = 0;
        for (int i = 0; i < this.population.size(); i++) {
            S individual = this.population.get(i);
            if (individual.getObjective(0) > this.nadirPoint[0] ||
                    individual.getObjective(1) > this.nadirPoint[1] ||
                    individual.getObjective(2) > this.nadirPoint[2] ||
                    !this.isInner(individual)) {
                this.badSolution[this.badSolutionNum] = i;
                this.badSolutionNum++;
            }
        }

        if (this.population.size() - this.badSolutionNum < this.populationSize) {
            this.excludeBadSolution();
        } else {
            for (int i = this.badSolutionNum - 1; i >= 0; i--) {
                this.population.remove(this.badSolution[i]);
            }
        }
    }

    protected boolean isInner(S individual) {
        boolean flag = true;
        for (int i = 0; i < this.problem.getNumberOfObjectives(); i++) {
            if (this.border.get(i).isEmpty()) {
                flag = false;
                break;
            }
            if (this.paretoDom(individual, i)) {
                flag = false;
                break;
            }
        }
        return flag;
    }

    protected boolean paretoDom(S individual, int i) {
        boolean flag = false;
        int m = this.problem.getNumberOfObjectives() - 1;
        int sum1, sum2;
        for (int j = 0; j < this.border.get(i).size(); j++) {
            sum1 = 0;
            sum2 = 0;
            for (int k = 0; k < this.problem.getNumberOfObjectives(); k++)
                if (k != i) {
                    if (this.border.get(i).get(j).getObjective(k) <= individual.getObjective(k))
                        sum1++;
                    if (this.border.get(i).get(j).getObjective(k) < individual.getObjective(k))
                        sum2++;
                }
            if (sum1 == m && sum2 > 0) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    protected void supplyBadSolution() {
        Random rand = new Random();
        do {
            int i = rand.nextInt(this.badPopulation.size());
            this.population.add(this.badPopulation.get(i));
        } while (this.population.size() < this.populationSize);
    }

    protected void rankBasedSelection() {
        for (int i = 0; i < this.subproblemNum_; i++)
            this.subproblem.get(i).clear();

        this.allocateSolution();

        this.subproblemSortl();

        this.setIndividualObjRank();

        this.setSpIndividualRank();

        this.individualObjRankSort();

        this.lexicographicSort();

        this.chooseSolution();

    }

    protected void allocateSolution() {
        for (int i = 0; i < this.population.size(); i++) {
            this.setOrder(this.population.get(i), i);

            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                int objBasedAddress = (int) Math.pow(this.k_, this.problem.getNumberOfObjectives() - 1);
                objBasedAddress = j * objBasedAddress;
                int objOffset = 0;
                int bitIndex = 0;
                int bitWeight;
                for (int k = this.problem.getNumberOfObjectives() - 1; k >= 0; k--) {
                    if (j != k) {
                        bitWeight = (int) Math.pow(this.k_, bitIndex);
                        int g = this.getG(this.population.get(i), k);
                        objOffset += g * bitWeight;
                        bitIndex++;
                    }
                }

                this.subproblem.get(objBasedAddress + objOffset).add(this.population.get(i));

            }
        }
    }

    protected void subproblemSortl() {
        for (int i = 0; i < this.subproblemNum_; i++) {
            int perObjSubproblemNum = (int) Math.pow(this.k_, this.problem.getNumberOfObjectives() - 1);
            int objD = i / perObjSubproblemNum;

            this.subproblem.get(i).sort((o1, o2) -> {
                double x = o1.getObjective(objD);
                double y = o2.getObjective(objD);
                return Double.compare(x, y);
            });
        }
    }

    protected void setIndividualObjRank() {
        for (int i = 0; i < this.subproblemNum_; i++) {

            int perObjSubproblemNum = (int) Math.pow(this.k_, this.problem.getNumberOfObjectives() - 1);
            int objD = i / perObjSubproblemNum;

            for (int j = 0; j < this.subproblem.get(i).size(); j++) {
                int order = this.getOrder(this.subproblem.get(i).get(j));

                double objValue = this.subproblem.get(i).get(j).getObjective(objD);
                double firstValue = this.subproblem.get(i).get(0).getObjective(objD);

                int gridRank = (int) Math.ceil((objValue - firstValue) / this.d_[objD]);
                int objRank = Math.max(gridRank + 1, j + 1);
                this.setRank(this.population.get(order), objD, objRank);

            }
        }
    }

    protected void setSpIndividualRank() {
        for (Integer integer : this.spPopulationOrder) {
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                this.setRank(this.population.get(integer), j, 1000);
            }
        }
    }

    protected void individualObjRankSort() {
        for (S s : this.population) {
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                int rank = this.getRank(s, j);
                list.add(rank);
            }

            list.sort(Integer::compareTo);

            for (int j = 0; j < this.problem.getNumberOfObjectives(); j++) {
                this.setRank(s, j, list.get(j));
            }
        }
    }

    protected void lexicographicSort() {
        this.population.sort((o1, o2) -> {
            for (int i = 0; i < AbstractCDG.this.problem.getNumberOfObjectives(); i++) {
                int x = AbstractCDG.this.getRank(o1, i);
                int y = AbstractCDG.this.getRank(o2, i);
                if (y < x)
                    return 1;
                if (x < y)
                    return -1;
            }
            return 0;
        });
    }

    protected void chooseSolution() {
        if (this.population.size() < this.populationSize) {
            this.supplyBadSolution();
        } else {
            int length = this.population.size();
            if (length > this.populationSize - this.specialPopulation.size()) {
                this.population.subList(this.populationSize - this.specialPopulation.size(), length).clear();
            }
            this.population.addAll(this.specialPopulation);
        }
    }

    protected NeighborType chooseNeighborType(int i) {
        double rnd = this.randomGenerator.nextDouble();
        NeighborType neighborType;

        if (rnd < this.neighborhoodSelectionProbability && this.neighborhoodNum[i] > 2) {
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
    protected List<Integer> matingSelection(int subproblemId, int numberOfSolutionsToSelect,
                                            NeighborType neighbourType) {
        int neighbourSize;
        int selectedSolution;

        List<Integer> listOfSolutions = new ArrayList<>(numberOfSolutionsToSelect);

        while (listOfSolutions.size() < numberOfSolutionsToSelect) {
            int random;
            if (neighbourType == NeighborType.NEIGHBOR) {
                neighbourSize = this.neighborhoodNum[subproblemId];
                random = this.randomGenerator.nextInt(0, neighbourSize - 1);
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

    @Override
    public List<S> getResult() {
        return this.population;
    }

    protected enum NeighborType {
        NEIGHBOR, POPULATION
    }


}
