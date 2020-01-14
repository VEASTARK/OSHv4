package osh.comdriver.simulation.cruisecontrol;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.ui.RectangleInsets;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.TimeZone;

/**
 * @author Till Schuberth
 */
abstract class AbstractDrawer extends JPanel {

    private static final Dimension preferredSize = new Dimension(500, 270);
    private static final long serialVersionUID = 1L;

    static {
        // set a theme using the new shadow generator feature available in
        // 1.0.14 - for backwards compatibility it is not enabled by default
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", true));
    }

    private ChartPanel panel;
    private String name;
    private boolean showPast;
    private int showDays = 2;


    /**
     * CONSTRUCTOR
     */
    public AbstractDrawer(String name, boolean showPast) {
        super(new BorderLayout());
        this.name = name;
        this.showPast = showPast;
        this.panel = this.createDemoPanel();
        this.panel.setPreferredSize(preferredSize);
        this.add(this.panel, BorderLayout.CENTER);
        this.panel.setVisible(true);
    }

    public String getName() {
        return this.name;
    }

    /**
     * Creates a chart.
     *
     * @param dataset a dataset.
     * @return A chart.
     */
    private JFreeChart createChart(XYDataset dataset, long lastEntry) {

        JFreeChart chart = ChartFactory.createTimeSeriesChart(
                this.name,  // title
                "time",      // x-axis label
                "temperature",     // y-axis label
                dataset,     // data
                true,        // create legend?
                true,               // generate tooltips?
                false               // generate URLs?
        );

        chart.setBackgroundPaint(Color.white);

        XYPlot plot = (XYPlot) chart.getPlot();

        NumberAxis axis1 = new NumberAxis(this.getAxisName());
        axis1.setAutoRangeIncludesZero(this.isIncludeZero());
        plot.setRangeAxis(0, axis1);

        plot.setDataset(0, dataset);
        plot.mapDatasetToRangeAxis(1, 0);

        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setAxisOffset(new RectangleInsets(5.0, 5.0, 5.0, 5.0));
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        //TODO: SHADOWS OFF

        final StandardXYItemRenderer r1 = new StandardXYItemRenderer();
        plot.setRenderer(0, r1);
        r1.setSeriesPaint(0, Color.BLUE);
        r1.setSeriesPaint(1, Color.RED);
        r1.setSeriesPaint(2, Color.GREEN);
        r1.setSeriesPaint(3, Color.BLACK);
        r1.setSeriesPaint(4, Color.ORANGE);

        //plot.setDomainAxis(new NumberAxis("time"));
        plot.setDomainAxis(new DateAxis());
        ((DateAxis) plot.getDomainAxis()).setTimeZone(TimeZone.getTimeZone("GMT"));
        plot.getDomainAxis().setAutoRange(false);

        long begin = this.getRangeBegin(lastEntry);
        long end = this.getRangeEnd(lastEntry);

        plot.getDomainAxis().setRange(begin, end);

        return chart;
    }

    protected boolean isIncludeZero() {
        return false;
    }

    protected abstract String getAxisName();

    protected long getRangeBegin(long lastEntry) {
        int daysIntoPast = 0;
        if (this.showPast) daysIntoPast = this.showDays - 1;

        long ret = (lastEntry / 86400 - daysIntoPast) * 86400 * 1000;
        if (ret < 0) ret = 0;

        return ret;
    }

    protected long getRangeEnd(long lastEntry) {
        int daysIntoFuture = 1;
        if (!this.showPast) daysIntoFuture = this.showDays + 1;

        return (lastEntry / 86400 + daysIntoFuture) * 86400 * 1000;
    }

    protected abstract List<XYSeries> getSeries(long begin, long end);

    protected abstract long getNumberOfEntries();

    private XYDataset createDataset() {
        XYSeriesCollection dataset = new XYSeriesCollection();

        for (XYSeries s : this.getSeries(this.getRangeBegin(this.getNumberOfEntries()), this.getRangeEnd(this.getNumberOfEntries()))) {
            dataset.addSeries(s);
        }

        return dataset;
    }

    private ChartPanel createDemoPanel() {
        ChartPanel panel = new ChartPanel(this.createStuffForPanel(true));
        panel.setFillZoomRectangle(true);
        panel.setMouseWheelEnabled(true);
        return panel;
    }

    public void refreshDiagram() {
        JFreeChart chart = this.createStuffForPanel(false);
        this.panel.setChart(chart);

    }

    private JFreeChart createStuffForPanel(boolean empty) {
        if (empty) {
            return this.createChart(new XYSeriesCollection(), 0);
        } else {
            XYDataset dataset = this.createDataset();
            return this.createChart(dataset, this.getNumberOfEntries());
        }
    }

    public int getShowDays() {
        return this.showDays;
    }

    public void setShowDays(int showDays) {
        this.showDays = showDays;
        this.refreshDiagram();
    }

}