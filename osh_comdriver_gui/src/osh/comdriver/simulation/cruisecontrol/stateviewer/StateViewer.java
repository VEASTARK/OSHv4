package osh.comdriver.simulation.cruisecontrol.stateviewer;

import osh.datatypes.registry.StateExchange;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.*;


/**
 * @author Till Schuberth
 */
public class StateViewer extends JPanel implements ItemListener {
    private static final long serialVersionUID = 2299684852859082725L;

    private final JComboBox<String> registryCombo;
    private final JComboBox<String> typeCombo;
    private final DefaultComboBoxModel<String> typesModel;
    private final TreeSet<Class<? extends StateExchange>> oldTypesList = new TreeSet<>(new ClassNameComparator());
    private final JTable table;
    private final StatesTableModel model;
    private final Set<StateViewerListener> listeners = new HashSet<>();


    /**
     * CONSTRUCTOR
     */
    public StateViewer() {
        super();
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.registryCombo = new JComboBox<>(toStringArray(StateViewerRegistryEnum.values()));
        this.registryCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, this.registryCombo.getPreferredSize().height));
        this.registryCombo.setSelectedIndex(0);
        this.registryCombo.addItemListener(this);
        this.add(this.registryCombo);
        this.typesModel = new DefaultComboBoxModel<>();
        this.typeCombo = new JComboBox<>(this.typesModel);
        this.typeCombo.setMaximumSize(new Dimension(Short.MAX_VALUE, this.typeCombo.getPreferredSize().height));
        this.typeCombo.addItemListener(this);
        this.add(this.typeCombo);
        this.model = new StatesTableModel();
        this.table = new JTable(this.model);
        this.table.getColumnModel().getColumn(0).setPreferredWidth(150);
        this.table.getColumnModel().getColumn(1).setPreferredWidth(50);
        this.table.getColumnModel().getColumn(2).setPreferredWidth(500);
        JScrollPane sp = new JScrollPane(this.table);
        sp.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        sp.setAlignmentX(CENTER_ALIGNMENT);
        this.add(sp);

        this.setPreferredSize(new Dimension(200, 200));
    }

    private static String[] toStringArray(Object[] arr) {
        if (arr == null) throw new NullPointerException("argument is null");

        String[] ret = new String[arr.length];
        for (int i = 0; i < arr.length; i++) {
            ret[i] = arr[i].toString();
        }

        return ret;
    }


    public void showTypes(Set<Class<? extends StateExchange>> types) {
        synchronized (this.oldTypesList) {

            for (Class<? extends StateExchange> c : types) {
                if (!this.oldTypesList.contains(c)) {
                    this.oldTypesList.add(c);
                    this.typesModel.insertElementAt(c.getName(), this.getIndexOfInOldList(c));
                }
            }

            Set<Class<? extends StateExchange>> toRemove = new HashSet<>();
            for (Class<? extends StateExchange> c : this.oldTypesList) {
                if (!types.contains(c)) {
                    this.typesModel.removeElement(c.getName());
                    toRemove.add(c);
                }
            }
            this.oldTypesList.removeAll(toRemove);
        }
    }

    private int getIndexOfInOldList(Class<? extends StateExchange> type) {
        int index = 0;
        synchronized (this.oldTypesList) {
            for (Class<? extends StateExchange> c : this.oldTypesList) {
                if (c.equals(type)) return index;
                index++;
            }
        }

        return -1;
    }

    public void showStates(Map<UUID, ? extends StateExchange> entries) {
        this.model.setData(entries);
    }

    public void registerListener(StateViewerListener l) {
        synchronized (this.listeners) {
            if (l != null) this.listeners.add(l);
        }
    }

    public void unregisterListener(StateViewerListener l) {
        synchronized (this.listeners) {
            if (l != null) this.listeners.remove(l);
        }
    }

    private void notifyListenersState(Class<? extends StateExchange> cls) {
        synchronized (this.listeners) {
            for (StateViewerListener l : this.listeners) {
                l.stateViewerClassChanged(cls);
            }
        }
    }

    private void notifyListenersRegistry(StateViewerRegistryEnum registry) {
        synchronized (this.listeners) {
            for (StateViewerListener l : this.listeners) {
                l.stateViewerRegistryChanged(registry);
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getSource() == this.registryCombo) {
            this.notifyListenersState(null);
            this.showTypes(new HashSet<>());
            this.notifyListenersRegistry(StateViewerRegistryEnum.findByString(e.getItem().toString()));
        } else if (e.getSource() == this.typeCombo) {
            Class<? extends StateExchange> cls = null;

            synchronized (this.oldTypesList) {
                for (Class<? extends StateExchange> c : this.oldTypesList) {
                    if (c.getName().equals(e.getItem())) {
                        cls = c;
                        break;
                    }
                }
            }

            synchronized (this) {
                this.notifyListenersState(cls);
            }
        }
    }

}
