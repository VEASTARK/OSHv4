package osh.comdriver.simulation.cruisecontrol;

import org.jfree.data.xy.XYSeries;
import osh.datatypes.cruisecontrol.GUIBatteryStorageStateExchange;

import java.util.*;
import java.util.Map.Entry;


/**
 * @author Jan Mueller
 */
class BatteryStateOfChargeDrawer extends AbstractDrawer {

    private List<XYSeries> currentSeries;
    private long lastEntry;

    public BatteryStateOfChargeDrawer(UUID id) {
        super("StateOfCharge " + id.toString().substring(0, 6), true);
    }

    @Override
    protected String getAxisName() {
        return "StateOfCharge [W/s]";
    }

    @Override
    protected List<XYSeries> getSeries(long begin, long end) {
        return (this.currentSeries == null ? new LinkedList<>() : this.currentSeries);
    }

    @Override
    protected long getNumberOfEntries() {
        return this.lastEntry;
    }

    public void refreshDiagram(TreeMap<Long, GUIBatteryStorageStateExchange> batteryStorageHistory) {
        List<XYSeries> series = new ArrayList<>();

        XYSeries minStateOfCharge = new XYSeries("minStateOfCharge");
        XYSeries maxStateOfCharge = new XYSeries("maxStateOfCharge");
        XYSeries stateOfCharge = new XYSeries("StateOfCharge");

        for (Entry<Long, GUIBatteryStorageStateExchange> ex : batteryStorageHistory.entrySet()) {
            minStateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getMinStateOfCharge());
            maxStateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getMaxStateOfCharge());
            stateOfCharge.add(ex.getKey().doubleValue() * 1000, ex.getValue().getStateOfCharge());
        }

        series.add(minStateOfCharge);
        series.add(maxStateOfCharge);
        series.add(stateOfCharge);

        this.currentSeries = series;
        if (!batteryStorageHistory.isEmpty()) {
            this.lastEntry = batteryStorageHistory.lastKey();
        }
        super.refreshDiagram();
    }


}