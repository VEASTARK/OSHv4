package osh.busdriver.mielegateway.data;

import org.eclipse.persistence.oxm.annotations.XmlPath;

import javax.xml.bind.annotation.XmlType;

@XmlType
public class MieleDuration {
    /**
     * Duration in minutes
     */
    private int duration;


    public MieleDuration() {
        super();
    }

    public MieleDuration(int duration) {
        super();
        this.duration = duration;
    }

    private static int parseString(String mieleTimeString) {
        // strip "h" suffix and spaces at end
        String timeString = mieleTimeString;
        while (timeString.endsWith("h") || timeString.endsWith(" ")) {
            // strip one character
            timeString = timeString.substring(0, timeString.length() - 1);
        }

        String[] timeParts = timeString.split(":");
        if (timeParts.length == 2) {
            return Integer.parseInt(timeParts[0]) * 60 + Integer.parseInt(timeParts[1]);
        } else {
            return -1;
        }
    }

    @XmlPath("text()")
    public void setDuration(String mieleTimeString) {
        this.duration = parseString(mieleTimeString);
    }

    /**
     * Returns MieleTime interpreted as duration in minutes
     *
     * @return MieleTime interpreted as duration in minutes
     */
    public int duration() {
        return this.duration;
    }

    public int hour() {
        return this.duration / 60;
    }

    public int minute() {
        return this.duration % 60;
    }

    @Override
    public String toString() {
        if (this.duration >= 0)
            return this.duration + "m";
        else
            return "invalid duration";
    }
}
