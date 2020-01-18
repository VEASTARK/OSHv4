package osh.comdriver.simulation.cruisecontrol;

import org.jfree.data.xy.XYSeries;

import java.util.*;

/**
 * @author Sebastian Kramer
 */
public abstract class AbstractWaterDrawer extends Abstract2AxisDrawer {
    private List<XYSeries> currentSeries1;
    private List<XYSeries> currentSeries2;
    private long lastEntry;

    public AbstractWaterDrawer(String name, boolean showPast) {
        super(name, showPast);
    }

    @Override
    protected String getAxisName() {
        return "temperature";
    }

    @Override
    protected String getAxisName2() {
        return "watt";
    }

    @Override
    protected List<XYSeries> getSeries1(long begin, long end) {
        return (this.currentSeries1 == null ? new LinkedList<>() : this.currentSeries1);
    }

    @Override
    protected List<XYSeries> getSeries2(long begin, long end) {
        return (this.currentSeries2 == null ? new LinkedList<>() : this.currentSeries2);
    }

    @Override
    protected long getNumberOfEntries() {
        return this.lastEntry;
    }

    public void refreshDiagram(TreeMap<Long, Double> tankTemperature,
                               TreeMap<Long, Double> waterDemand,
                               TreeMap<Long, Double> waterSupply) {

        List<XYSeries> series1 = new ArrayList<>();
        List<XYSeries> series2 = new ArrayList<>();

        XYSeries minTemp = new XYSeries("minTemp");
        XYSeries maxTemp = new XYSeries("maxTemp");
        XYSeries temp = new XYSeries("predTemp");

        XYSeries demand = new XYSeries("hotWaterDemand");
        XYSeries supply = new XYSeries("hotWaterSupply");

        for (Map.Entry<Long, Double> ex : tankTemperature.entrySet()) {
            minTemp.add(ex.getKey().doubleValue() * 1000, 60);
            maxTemp.add(ex.getKey().doubleValue() * 1000, 80);
            temp.add(ex.getKey().doubleValue() * 1000, ex.getValue());
        }

        this.processDemandSupply(waterDemand, demand);
        this.processDemandSupply(waterSupply, supply);

        series1.add(minTemp);
        series1.add(maxTemp);
        series1.add(temp);

        series2.add(supply);
        series2.add(demand);

        this.currentSeries1 = series1;
        this.currentSeries2 = series2;
        if (!tankTemperature.isEmpty()) {
            this.lastEntry = tankTemperature.lastKey();
        }
        super.refreshDiagram();
    }

    private void processDemandSupply(TreeMap<Long, Double> water, XYSeries toWrite) {

        for (Map.Entry<Long, Double> en : water.entrySet()) {
            toWrite.add(en.getKey().doubleValue() * 1000, en.getValue());
        }
    }
}
