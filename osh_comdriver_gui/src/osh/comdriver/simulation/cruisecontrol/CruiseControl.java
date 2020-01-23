package osh.comdriver.simulation.cruisecontrol;

import osh.utils.time.TimeConversion;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * @author Till Schuberth, Ingo Mauser
 */
class CruiseControl extends JPanel {

    //    private final DateFormat timeFormatter;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final boolean waitAllowed;
    private final JLabel l_time = new JLabel();
    private final Lock goWriteLock = new ReentrantReadWriteLock().writeLock();
    private boolean wait = true;
    private boolean update = true;
    private long go;
    private volatile long currentTime;


    /**
     * CONSTRUCTOR
     */
    public CruiseControl(boolean waitAllowed) {
        super();

        this.waitAllowed = waitAllowed;

        FlowLayout layout = new FlowLayout();
        this.setLayout(layout);

        this.l_time.setText("-- time --");
        this.add(this.l_time);

        final JCheckBox  c_wait = new JCheckBox ("wait (global scheduler)");
        c_wait.setSelected(true);
        c_wait.addItemListener(e -> this.wait = c_wait.isSelected());
        if (!waitAllowed) {
            c_wait.setEnabled(false);
            c_wait.setSelected(false);
            this.wait = false;
        }
        this.add(c_wait);


        final JCheckBox  c_update = new JCheckBox ("update");
        c_update.setSelected(true);
        c_update.addItemListener(e -> {
            this.update = c_update.isSelected();
            if (this.update) {
                c_wait.setSelected(true);
                this.wait = true;
            }
        });
        this.add(c_update);

        JButton b = new JButton("go");
        b.addActionListener(e -> {
            this.goWriteLock.lock();
            try {
                this.go = -1;
            } finally {
                this.goWriteLock.unlock();
            }
        });
        this.add(b);

        final JTextField tf = new JTextField(10);
        final Color normalColor = tf.getForeground();
        tf.getDocument().addDocumentListener(new DocumentListener() {

            private void checkInput() {
                try {
                    Long.parseLong(tf.getText());
                    //parsed correctly
                    tf.setForeground(normalColor);
                } catch (NumberFormatException e) {
                    tf.setForeground(Color.RED);
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                this.checkInput();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                this.checkInput();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                this.checkInput();
            }
        });
        this.add(tf);

        b = new JButton("go to time");
        b.addActionListener(e -> {
            this.goWriteLock.lock();
            try {
                this.go = Long.parseLong(tf.getText());
            } catch (NumberFormatException ignored) {
            } finally {
                this.goWriteLock.unlock();
            }
            c_wait.setEnabled(true);
        });
        this.add(b);

        this.setPreferredSize(new Dimension(300, 200));
    }

    public boolean isWait() {
        if (!this.waitAllowed) return false;
        return this.wait;
    }

    public boolean isUpdate() {
        return this.update;
    }

    public void waitForGo() {
        while (true) {
            this.goWriteLock.lock();
            try {
                if (this.go < 0 || this.go > this.currentTime) {
                    if (this.go < 0) this.go = 0; // wait next time again
                    return;
                }
            } finally {
                this.goWriteLock.unlock();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }

    public void updateTime(long timestamp) {
        this.l_time.setText(timestamp + " (" +
                TimeConversion.convertUnixTimeToZonedDateTime(timestamp).format(this.timeFormatter) + " UTC)");
        this.currentTime = timestamp;
    }

}
