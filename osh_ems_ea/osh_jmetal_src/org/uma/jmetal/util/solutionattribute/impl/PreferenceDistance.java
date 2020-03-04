package org.uma.jmetal.util.solutionattribute.impl;

import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.pseudorandom.JMetalRandom;
import org.uma.jmetal.util.solutionattribute.DensityEstimator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

@SuppressWarnings("serial")
public class PreferenceDistance<S extends Solution<?>> extends GenericSolutionAttribute<S, Double> implements DensityEstimator<S> {
    private final double epsilon;
    private List<Double> interestPoint;
    private List<Double> weights;

    public PreferenceDistance(List<Double> interestPoint, double epsilon) {
        this.epsilon = epsilon;
        this.interestPoint = interestPoint;

    }


    public void updatePointOfInterest(List<Double> newInterestPoint) {
        this.interestPoint = newInterestPoint;
    }


    public int getSize() {
        return this.weights.size();
    }


    @Override
    public void computeDensityEstimator(List<S> solutionList) {
        int size = solutionList.size();

        if (size == 0) {
            return;
        }

        if (size == 1) {
            solutionList.get(0).setAttribute(this.getAttributeIdentifier(), Double.POSITIVE_INFINITY);
            return;
        }

        if (size == 2) {
            solutionList.get(0).setAttribute(this.getAttributeIdentifier(), Double.POSITIVE_INFINITY);
            solutionList.get(1).setAttribute(this.getAttributeIdentifier(), Double.POSITIVE_INFINITY);

            return;
        }

        // Use a new SolutionSet to avoid altering the original solutionSet
        List<S> front = new ArrayList<>(size);
        front.addAll(solutionList);

        for (int i = 0; i < size; i++) {
            front.get(i).setAttribute(this.getAttributeIdentifier(), 0.0);
        }

        double objetiveMaxn;
        double objetiveMinn;
        double distance;

        int numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
        this.weights = new ArrayList<>();
        for (int i = 0; i < numberOfObjectives; i++) {
            this.weights.add(1.0d / numberOfObjectives);

        }


        for (int i = 0; i < front.size() - 1; i++) {
            double normalizeDiff;
            distance = 0.0D;
            for (int j = 0; j < numberOfObjectives; j++) {
                // Sort the population by Obj n
                front.sort(new ObjectiveComparator<>(j));
                objetiveMinn = front.get(0).getObjective(j);
                objetiveMaxn = front.get(front.size() - 1).getObjective(j);
                normalizeDiff = (front.get(i).getObjective(j) - this.interestPoint.get(j)) /
                        (objetiveMaxn - objetiveMinn);
                distance += this.weights.get(j) * Math.pow(normalizeDiff, 2.0D);
            }
            distance = Math.sqrt(distance);
            front.get(i).setAttribute(this.getAttributeIdentifier(), distance);

        }


        //solutionList = epsilonClean(front);

    }

    public List<S> epsilonClean(List<S> solutionList) {
        List<S> preference = new ArrayList<>();
        List<S> temporalList = new LinkedList<>(solutionList);
        int numerOfObjectives = solutionList.get(0).getNumberOfObjectives();

        while (!temporalList.isEmpty()) {
            int indexRandom = JMetalRandom.getInstance().nextInt(0, temporalList.size() - 1);//0

            S randomSolution = temporalList.get(indexRandom);

            preference.add(randomSolution);
            temporalList.remove(indexRandom);

            for (int indexOfSolution = 0; indexOfSolution < temporalList.size(); indexOfSolution++) {
                double sum = 0;

                for (int indexOfObjective = 0; indexOfObjective < numerOfObjectives; indexOfObjective++) {
                    temporalList.sort(new ObjectiveComparator<>(indexOfObjective));
                    double objetiveMinn = temporalList.get(0).getObjective(indexOfObjective);
                    double objetiveMaxn = temporalList.get(temporalList.size() - 1).getObjective(indexOfObjective);
                    sum += ((Math.abs(randomSolution.getObjective(indexOfObjective) - temporalList.get(indexOfSolution).getObjective(indexOfObjective))) / (objetiveMaxn - objetiveMinn));

                }

                if (sum < this.epsilon) {
                    temporalList.get(indexOfSolution).setAttribute(this.getAttributeIdentifier(), Double.MAX_VALUE);
                    preference.add(temporalList.get(indexOfSolution));
                    temporalList.remove(indexOfSolution);
                }
            }
        }
        return preference;
    }


}
