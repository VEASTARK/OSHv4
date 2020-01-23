package osh.comdriver.simulation.cruisecontrol.stateviewer;

import osh.datatypes.registry.AbstractExchange;
import osh.datatypes.registry.StateExchange;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author Till Schuberth
 */
@SuppressWarnings("rawtypes")
class StatesTableModel implements TableModel {

    public static final Entry[] DEFAULT_DATA = new Entry[0];

    private final Set<TableModelListener> modelListeners = new HashSet<>();
    private final ReentrantReadWriteLock modelLock = new ReentrantReadWriteLock();
    private Entry<UUID, StateExchange>[] data;


    /**
     * CONSTRUCTOR
     */
    @SuppressWarnings("unchecked")
    public StatesTableModel() {
        this.data = DEFAULT_DATA;
    }


    @Override
    public int getRowCount() {
        return this.data.length;
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return "Sender";
            case 1:
                return "Timestamp";
            case 2:
                return "Value";
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return UUID.class;
            case 1:
                return Long.class;
            case 2:
                return String.class;
            default:
                return null;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= this.data.length) return null;
        switch (columnIndex) {
            case 0:
                return this.data[rowIndex].getKey();
            case 1:
                return this.data[rowIndex].getValue().getTimestamp();
            case 2:
                return this.data[rowIndex].getValue().toString();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
        this.modelLock.writeLock().lock();
        try {
            if (l != null) this.modelListeners.add(l);
        } finally {
            this.modelLock.writeLock().unlock();
        }
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
        this.modelLock.writeLock().lock();
        try {
            if (l != null) this.modelListeners.remove(l);
        } finally {
            this.modelLock.writeLock().unlock();
        }
    }

    private void notifyTableModelListener() {
        HashSet<TableModelListener> listeners;

        this.modelLock.readLock().lock();
        try {
            listeners = new HashSet<>(this.modelListeners);
        } finally {
            this.modelLock.readLock().unlock();
        }

        for (TableModelListener l : listeners) {
            l.tableChanged(new TableModelEvent(this));
        }
    }

    @SuppressWarnings("unchecked")
    public void setData(Map<UUID, ? extends AbstractExchange> data) {

        if (data == null) {
            this.data = DEFAULT_DATA;
        } else {
            Map<UUID, AbstractExchange> strData = new HashMap<>();
            for (Entry<UUID, ? extends AbstractExchange> e : data.entrySet()) {
                if (e.getValue() instanceof StateExchange) strData.put(e.getKey(), e.getValue());
            }
            this.data = strData.entrySet().toArray(DEFAULT_DATA);
            Arrays.sort(this.data, Entry.comparingByKey());
        }
        this.notifyTableModelListener();
    }
}