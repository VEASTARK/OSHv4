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

//        this.timeFormatter = DateFormat.getDateTimeInstance();
//        this.timeFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));

        FlowLayout layout = new FlowLayout();
        this.setLayout(layout);

        this.l_time.setText("-- time --");
        this.add(this.l_time);

        final Checkbox c_wait = new Checkbox("wait (global scheduler)");
        c_wait.setState(true);
        c_wait.addItemListener(e -> CruiseControl.this.wait = c_wait.getState());
        if (!waitAllowed) {
            c_wait.setEnabled(false);
            c_wait.setState(false);
            this.wait = false;
        }
        this.add(c_wait);


        final Checkbox c_update = new Checkbox("update");
        c_update.setState(true);
        c_update.addItemListener(e -> {
            CruiseControl.this.update = c_update.getState();
            if (CruiseControl.this.update) {
                c_wait.setState(true);
                CruiseControl.this.wait = true;
            }
        });
        this.add(c_update);

        JButton b = new JButton("go");
        b.addActionListener(e -> {
            this.goWriteLock.lock();
            CruiseControl.this.go = -1;
            this.goWriteLock.unlock();
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
                CruiseControl.this.go = Long.parseLong(tf.getText());
            } catch (NumberFormatException ignored) {
            }
            c_wait.setEnabled(true);
            this.goWriteLock.unlock();
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
            if (this.go < 0 || this.go > this.currentTime) {
                if (this.go < 0) this.go = 0; // wait next time again
                this.goWriteLock.unlock();
                return;
            }
            this.goWriteLock.unlock();

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
