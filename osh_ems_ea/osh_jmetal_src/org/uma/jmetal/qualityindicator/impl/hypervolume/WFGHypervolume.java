package org.uma.jmetal.qualityindicator.impl.hypervolume;

import org.uma.jmetal.qualityindicator.impl.Hypervolume;
import org.uma.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeFront;
import org.uma.jmetal.qualityindicator.impl.hypervolume.util.WfgHypervolumeVersion;
import org.uma.jmetal.solution.Solution;
import org.uma.jmetal.util.comparator.HypervolumeContributionComparator;
import org.uma.jmetal.util.comparator.ObjectiveComparator;
import org.uma.jmetal.util.front.Front;
import org.uma.jmetal.util.point.Point;
import org.uma.jmetal.util.point.impl.ArrayPoint;
import org.uma.jmetal.util.solutionattribute.impl.HypervolumeContributionAttribute;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Created by ajnebro on 2/2/15.
 */
@SuppressWarnings("serial")
public class WFGHypervolume<S extends Solution<?>> extends Hypervolume<S> {

    private static final double DEFAULT_OFFSET = 100.0;
    private Point referencePoint;
    private int numberOfObjectives;
    private double offset = DEFAULT_OFFSET;

    /**
     * Default constructor
     */
    public WFGHypervolume() {
    }

    /**
     * Constructor
     *
     * @param referenceParetoFrontFile
     * @throws FileNotFoundException
     */
    public WFGHypervolume(String referenceParetoFrontFile) throws FileNotFoundException {
        super(referenceParetoFrontFile);
        this.numberOfObjectives = this.referenceParetoFront.getPointDimensions();
        this.referencePoint = null;
        this.updateReferencePoint(this.referenceParetoFront);
    }

    /**
     * Constructor
     *
     * @param referenceParetoFront
     * @throws FileNotFoundException
     */
    public WFGHypervolume(Front referenceParetoFront) {
        super(referenceParetoFront);
        this.numberOfObjectives = referenceParetoFront.getPointDimensions();
        this.referencePoint = null;
        this.updateReferencePoint(referenceParetoFront);
    }

    @Override
    public Double evaluate(List<S> solutionList) {
        double hv;
        if (solutionList.isEmpty()) {
            hv = 0.0;
        } else {
            this.numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
            this.referencePoint = new ArrayPoint(this.numberOfObjectives);
            this.updateReferencePoint(solutionList);

            if (this.numberOfObjectives == 2) {
                solutionList.sort(new ObjectiveComparator<Solution<?>>(this.numberOfObjectives - 1,
                        ObjectiveComparator.Ordering.DESCENDING));
                hv = this.get2DHV(solutionList);
            } else {
                this.updateReferencePoint(solutionList);
                WfgHypervolumeVersion wfgHv = new WfgHypervolumeVersion(this.numberOfObjectives, solutionList.size());
                hv = wfgHv.getHV(new WfgHypervolumeFront(solutionList));
            }
        }

        return hv;
    }

    public double computeHypervolume(List<S> solutionList, Point referencePoint) {
        double hv;
        if (solutionList.isEmpty()) {
            hv = 0.0;
        } else {
            this.numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
            this.referencePoint = referencePoint;

            if (this.numberOfObjectives == 2) {
                solutionList.sort(new ObjectiveComparator<>(1,
                        ObjectiveComparator.Ordering.DESCENDING));
                hv = this.get2DHV(solutionList);
            } else {
                WfgHypervolumeVersion wfgHv = new WfgHypervolumeVersion(this.numberOfObjectives, solutionList.size());
                hv = wfgHv.getHV(new WfgHypervolumeFront(solutionList));
            }
        }

        return hv;
    }

    /**
     * Updates the reference point
     */
    private void updateReferencePoint(List<? extends Solution<?>> solutionList) {
        double[] maxObjectives = new double[this.numberOfObjectives];
        for (int i = 0; i < this.numberOfObjectives; i++) {
            maxObjectives[i] = 0;
        }

        for (Solution<?> solution : solutionList) {
            for (int j = 0; j < this.numberOfObjectives; j++) {
                if (maxObjectives[j] < solution.getObjective(j)) {
                    maxObjectives[j] = solution.getObjective(j);
                }
            }
        }

        if (this.referencePoint == null) {
            this.referencePoint = new ArrayPoint(this.numberOfObjectives);
            for (int i = 0; i < this.numberOfObjectives; i++) {
                this.referencePoint.setValue(i, Double.MAX_VALUE);
            }
        }

        for (int i = 0; i < this.referencePoint.getDimension(); i++) {
            this.referencePoint.setValue(i, maxObjectives[i] + this.offset);
        }
    }

    /**
     * Updates the reference point
     */
    private void updateReferencePoint(Front front) {
        double[] maxObjectives = new double[this.numberOfObjectives];
        for (int i = 0; i < this.numberOfObjectives; i++) {
            maxObjectives[i] = 0;
        }

        for (int i = 0; i < front.getNumberOfPoints(); i++) {
            for (int j = 0; j < this.numberOfObjectives; j++) {
                if (maxObjectives[j] < front.getPoint(i).getValue(j)) {
                    maxObjectives[j] = front.getPoint(i).getValue(j);
                }
            }
        }

        if (this.referencePoint == null) {
            this.referencePoint = new ArrayPoint(this.numberOfObjectives);
            for (int i = 0; i < this.numberOfObjectives; i++) {
                this.referencePoint.setValue(i, Double.MAX_VALUE);
            }
        }

        for (int i = 0; i < this.referencePoint.getDimension(); i++) {
            this.referencePoint.setValue(i, maxObjectives[i] + this.offset);
        }
    }

    /**
     * Computes the HV of a solution list.
     * REQUIRES: The problem is bi-objective
     * REQUIRES: The setArchive is ordered in descending order by the second objective
     *
     * @return
     */
    public double get2DHV(List<? extends Solution<?>> solutionSet) {
        double hv = 0.0;
        if (!solutionSet.isEmpty()) {
            hv = Math.abs((solutionSet.get(0).getObjective(0) - this.referencePoint.getValue(0)) *
                    (solutionSet.get(0).getObjective(1) - this.referencePoint.getValue(1)));

            for (int i = 1; i < solutionSet.size(); i++) {
                double tmp;
                tmp = Math.abs((solutionSet.get(i).getObjective(0) - this.referencePoint.getValue(0)) * (solutionSet.get(i).getObjective(1) - solutionSet.get(i - 1).getObjective(1)));
                hv += tmp;
            }
        }
        return hv;
    }

    @Override
    public List<S> computeHypervolumeContribution(List<S> solutionList, List<S> referenceFrontList) {
        this.numberOfObjectives = solutionList.get(0).getNumberOfObjectives();
        this.updateReferencePoint(referenceFrontList);
        if (solutionList.size() > 1) {
            double[] contributions = new double[solutionList.size()];
            double solutionSetHV;

            solutionSetHV = this.evaluate(solutionList);

            for (int i = 0; i < solutionList.size(); i++) {
                S currentPoint = solutionList.get(i);
                solutionList.remove(i);

                if (this.numberOfObjectives == 2) {
                    contributions[i] = solutionSetHV - this.get2DHV(solutionList);
                } else {
                    //Front front = new Front(solutionSet.size(), numberOfObjectives, solutionSet);
                    WfgHypervolumeFront front = new WfgHypervolumeFront(solutionList);
                    double hv = new WfgHypervolumeVersion(this.numberOfObjectives, solutionList.size()).getHV(front);
                    contributions[i] = solutionSetHV - hv;
                }

                solutionList.add(i, currentPoint);
            }

            HypervolumeContributionAttribute<Solution<?>> hvContribution = new HypervolumeContributionAttribute<>();
            for (int i = 0; i < solutionList.size(); i++) {
                hvContribution.setAttribute(solutionList.get(i), contributions[i]);
            }

            solutionList.sort(new HypervolumeContributionComparator<>());
        }

        return solutionList;
    }

    @Override
    public double getOffset() {
        return this.offset;
    }

    @Override
    public void setOffset(double offset) {
        this.offset = offset;
    }

    @Override
    public String getDescription() {
        return "WFG implementation of the hypervolume quality indicator";
    }

}
