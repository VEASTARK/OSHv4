package osh.comdriver.simulation.cruisecontrol;

import osh.datatypes.gui.DeviceTableEntry;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author Till Schuberth, Ingo Mauser
 */
class DeviceTable extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JTable table;
    private final MyModel model;
    /**
     * CONSTRUCTOR
     */
    public DeviceTable() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.model = new MyModel();
        this.table = new JTable(this.model);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(20);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(150);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(150);
        this.table.getColumnModel().getColumn(3).setPreferredWidth(40);
        this.table.getColumnModel().getColumn(4).setPreferredWidth(400);
        JScrollPane sp = new JScrollPane(this.table);
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        sp.setAlignmentX(CENTER_ALIGNMENT);
        this.add(sp);
        this.setPreferredSize(new Dimension(200, 200));
    }


    public DeviceTable(Set<DeviceTableEntry> entries) {
        this();

        this.model.setData(entries);
    }

    public void refreshDeviceTable(Set<DeviceTableEntry> entries) {
        this.model.setData(entries);
    }

    /**
     * inner class
     */
    private static class MyModel implements TableModel {

        public static final DeviceTableEntry[] DATA = new DeviceTableEntry[0];
        private final Set<TableModelListener> modelListeners = new HashSet<>();
        private final Lock listenerWriteLock = new ReentrantReadWriteLock().writeLock();

        private DeviceTableEntry[] data;

        public MyModel() {
            this.data = DATA;
        }

        @SuppressWarnings("unused")
        public MyModel(Collection<DeviceTableEntry> data) {
            if (data == null) {
                this.data = DATA;
            } else {
                this.data = new DeviceTableEntry[data.size()];
                this.data = new TreeSet<>(data).toArray(this.data);
            }
        }

        @Override
        public int getRowCount() {
            return this.data.length;
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return "Index";
                case 1:
                    return "UUID";
                case 2:
                    return "Name";
                case 3:
                    return "Reschedule";
                case 4:
                    return "String representation";
                default:
                    return null;
            }
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                case 1:
                    return UUID.class;
                case 2:
                case 3:
                case 4:
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
                    return this.data[rowIndex].getEntry();
                case 1:
                    return this.data[rowIndex].getId();
                case 2:
                    return this.data[rowIndex].getName();
                case 3:
                    return this.data[rowIndex].getReschedule();
                case 4:
                    return this.data[rowIndex].getRepresentation();
                default:
                    return null;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            this.listenerWriteLock.lock();
            try {
                if (l != null) this.modelListeners.add(l);
            } finally {
                this.listenerWriteLock.unlock();
            }
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            this.listenerWriteLock.lock();
            try {
                if (l != null) this.modelListeners.remove(l);
            } finally {
                this.listenerWriteLock.unlock();
            }
        }

        private void notifyTableModelListener() {
            HashSet<TableModelListener> listeners;

            this.listenerWriteLock.lock();
            try {
                listeners = new HashSet<>(this.modelListeners);
            } finally {
                this.listenerWriteLock.unlock();
            }

            for (TableModelListener l : listeners) {
                l.tableChanged(new TableModelEvent(this));
            }
        }

        @SuppressWarnings("unused")
        public DeviceTableEntry[] getData() {
            return this.data;
        }

        public void setData(Collection<DeviceTableEntry> data) {
            if (data == null) {
                this.data = DATA;
            } else {
                this.data = new DeviceTableEntry[data.size()];
                this.data = new TreeSet<>(data).toArray(this.data);
            }
            this.notifyTableModelListener();
        }

    }

}
