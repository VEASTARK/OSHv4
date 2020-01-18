package osh.driver.chp.model;

import osh.utils.physics.ComplexPowerUtil;

import java.io.Serializable;

/**
 * @author Sebastian Kramer, Ingo Mauser
 */
public class GenericChpModel implements Serializable {

    private static final long serialVersionUID = -8465930221284802329L;

    private static final double timeForFullActivePower = 300.0;
    private static final double timeForFullThermalPower = 600.0;
    private static final double timeForFullThermalShutdown = 300.0;

    private double typicalActivePower;
    private double typicalReactivePower;
    private double typicalThermalPower;
    private int typicalGasPower;
    private double cosPhi;
    private boolean isOn;

//	private boolean printDebug = false;

    private int actualActivePower;
    private int actualReactivePower;
    private int actualThermalPower;
    private int actualGasPower;

    private int avgActualActivePower;
    private int avgActualReactivePower;
    private int avgActualThermalPower;
    private int avgActualGasPower;

    private double lastThermalPower;

    private long runningSince = -1;
    private long stoppedSince = -1;

    /**
     * CONSTRUCTOR for cloning
     */
    protected GenericChpModel() {
        // NOTHING
    }

    /**
     * CONSTRUCTOR
     */
    public GenericChpModel(
            double typicalActivePower,
            double typicalReactivePower,
            double typicalThermalPower,
            double typicalGasPower,
            double cosPhi,
            boolean isOn,
            int lastThermalPower,
            Long runningSince,
            Long stoppedSince) {
        this.typicalActivePower = typicalActivePower;
        this.typicalReactivePower = typicalReactivePower;
        this.typicalThermalPower = typicalThermalPower;
        this.typicalGasPower = (int) Math.round(typicalGasPower);
        this.cosPhi = cosPhi;
        this.isOn = isOn;
        this.lastThermalPower = lastThermalPower;
        this.runningSince = runningSince != null ? runningSince : Long.MAX_VALUE;
        this.stoppedSince = stoppedSince != null ? stoppedSince : Long.MAX_VALUE;
    }

    public void setRunning(boolean isRunning, long timeStamp) {
        if (isRunning != this.isOn) {
//			System.out.println("[" + timeStamp + "] Change State to: " + isRunning);
            this.isOn = isRunning;
            if (isRunning) {
                this.runningSince = timeStamp;
            } else {
                this.stoppedSince = timeStamp;
                /*
                 * if chp is turned off we have to update the lastThermalPower,
                 * otherwise if the chp was turned off at T, it will be the
                 * thermal power the chp was outputting at T-1 because it is
                 * only updated when power values are questioned
                 */
                double runTime = timeStamp - this.runningSince;
                if (runTime < timeForFullThermalPower)
                    this.lastThermalPower = (int) Math.round((runTime / timeForFullThermalPower) * this.typicalThermalPower);
                else
                    this.lastThermalPower = this.typicalThermalPower;
            }
        }
    }

//	public void setPrintDebug() {
//		this.printDebug = true;
//	}

    public void calcPower(long timeStamp) {
        if (this.isOn) {
            double runTime = timeStamp - this.runningSince;
            this.actualActivePower = (int) Math.round(this.typicalActivePower);
            this.actualReactivePower = (int) Math.round(this.typicalReactivePower);
            this.actualThermalPower = (int) Math.round(this.typicalThermalPower);
            this.actualGasPower = Math.round(this.typicalGasPower);
//			String prString = "Startup, time=" + timeStamp + ", runTime=" + runTime;
            if (runTime < timeForFullActivePower) {
                this.actualActivePower = (int) Math.round((0.9 + (0.1 * runTime / timeForFullActivePower)) * this.typicalActivePower);
                try {
                    this.actualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(this.actualActivePower, this.cosPhi, true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
//				prString += ", power: act=" + actualActivePower + " react=" + actualReactivePower;
            }
            if (runTime < timeForFullThermalPower) {
                this.actualThermalPower = (int) Math.round((runTime / timeForFullThermalPower) * this.typicalThermalPower);
//				prString += ", therm=" + actualThermalPower;
            }
//			if (prString.length() > 40 && printDebug)
//				System.out.println(prString);
            this.lastThermalPower = this.actualThermalPower;
        } else {
            double stopTime = timeStamp - this.stoppedSince;
            this.actualActivePower = 0;
            this.actualReactivePower = 0;
            this.actualThermalPower = 0;
            this.actualGasPower = 0;

            double timeCorrected = (this.lastThermalPower / this.typicalThermalPower) * timeForFullThermalShutdown;

            if (stopTime < timeCorrected) {
                this.actualThermalPower = (int) Math.round(((timeCorrected - stopTime) / timeCorrected) * this.lastThermalPower);
//				if (printDebug)
//					System.out.println("Stop, time=" + timeStamp + ", stopTime=" + stopTime + ", therm=" + actualThermalPower);
            }
        }
    }

    public void calcPowerAvg(long start, long end) {
        double timeSpan = end - start;
        if (this.isOn) {
            double runTimeStart = start - this.runningSince;
            double runTimeEnd = end - this.runningSince;

//			actualGasPower = typicalGasPower;
            this.avgActualGasPower = this.typicalGasPower;
            if (runTimeStart < timeForFullActivePower) {
                double startActivePowerPercent = (runTimeStart / timeForFullActivePower);

                if (runTimeEnd <= timeForFullActivePower) {
                    double endActivePowerPercent = (runTimeEnd / timeForFullActivePower);
                    this.avgActualActivePower = (int) Math.round(((((startActivePowerPercent + endActivePowerPercent) / 2.0) * 0.1) + 0.9) * this.typicalActivePower);
                } else {
                    double timeNotOnFullPower = (timeForFullActivePower - runTimeStart);
                    this.avgActualActivePower = (int) Math.round(
                            (((((1.0 + (startActivePowerPercent * 0.1 + 0.9)) / 2.0) * timeNotOnFullPower)
                                    + (timeSpan - timeNotOnFullPower)) / timeSpan) * this.typicalActivePower);
                }
//				actualActivePower = (int) Math.round(startActivePower);

                try {
//					actualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(actualActivePower, cosPhi, true));
                    this.avgActualReactivePower = (int) Math.round(ComplexPowerUtil.convertActiveToReactivePower(this.avgActualActivePower, this.cosPhi, true));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
//				actualActivePower = (int) Math.round(typicalActivePower);
                this.avgActualActivePower = (int) Math.round(this.typicalActivePower);
//				actualReactivePower = (int) Math.round(typicalReactivePower);
                this.avgActualReactivePower = (int) Math.round(this.typicalReactivePower);
            }

            if (runTimeStart < timeForFullThermalPower) {
                double startThermalPowerPercent = (runTimeStart / timeForFullThermalPower);

                if (runTimeEnd <= timeForFullThermalPower) {
                    double endThermalPowerPerc = (runTimeEnd / timeForFullThermalPower);
                    this.avgActualThermalPower = (int) Math.round(((startThermalPowerPercent + endThermalPowerPerc) / 2.0) * this.typicalThermalPower);
                } else {
                    double timeNotOnFullPower = (timeForFullThermalPower - runTimeStart);
                    this.avgActualThermalPower = (int) Math.round(
                            (((((1.0 + startThermalPowerPercent) / 2.0) * timeNotOnFullPower)
                                    + (timeSpan - timeNotOnFullPower)) / timeSpan) * this.typicalThermalPower);
                }
            } else {
//				actualThermalPower = (int) Math.round(typicalThermalPower);
                this.avgActualThermalPower = (int) Math.round(this.typicalThermalPower);
            }

            this.lastThermalPower = this.avgActualThermalPower;
        } else {
            double stopTimeStart = start - this.stoppedSince;
            double stopTimeEnd = end - this.stoppedSince;
//			actualActivePower = 0;
//			actualReactivePower = 0;
//			actualThermalPower = 0;
//			actualGasPower = 0;
            this.avgActualActivePower = 0;
            this.avgActualReactivePower = 0;
            this.avgActualGasPower = 0;

            double timeCorrected = (this.lastThermalPower / this.typicalThermalPower) * timeForFullThermalShutdown;

            if (stopTimeStart < timeCorrected) {
                double startThermalPower = ((timeCorrected - stopTimeStart) / timeCorrected) * this.lastThermalPower;

                if (stopTimeEnd < timeCorrected) {
                    double endThermalPower = ((timeCorrected - stopTimeEnd) / timeCorrected) * this.lastThermalPower;
                    this.avgActualThermalPower = (int) Math.round((startThermalPower + endThermalPower) / 2.0);
                } else {
                    double timeOnPower = (long) (timeCorrected - stopTimeStart);
                    this.avgActualThermalPower = (int) Math.round(((startThermalPower * timeOnPower) / 2.0) / timeSpan);
                }

//				actualThermalPower = (int) Math.round(startThermalPower);
            } else {
                this.avgActualThermalPower = 0;
            }
        }
    }

    public long getRunningForAtTimestamp(long timestamp) {
        if (this.isOn) {
            return timestamp - this.runningSince;
        } else {
            return 0;
        }
    }

    public int getActivePower() {
        return this.actualActivePower;
    }

    public int getReactivePower() {
        return this.actualReactivePower;
    }

    public int getThermalPower() {
        return this.actualThermalPower;
    }

    public int getGasPower() {
        return this.actualGasPower;
    }

    public Long getRunningSince() {
        return this.runningSince;
    }

    public Long getStoppedSince() {
        return this.stoppedSince;
    }

    public int getAvgActualActivePower() {
        return this.avgActualActivePower;
    }

    public int getAvgActualReactivePower() {
        return this.avgActualReactivePower;
    }

    public int getAvgActualThermalPower() {
        return this.avgActualThermalPower;
    }

    public int getAvgActualGasPower() {
        return this.avgActualGasPower;
    }

    public GenericChpModel clone() {
        GenericChpModel clone = new GenericChpModel();
        clone.typicalActivePower = this.typicalActivePower;
        clone.typicalReactivePower = this.typicalReactivePower;
        clone.typicalThermalPower = this.typicalThermalPower;
        clone.typicalGasPower = this.typicalGasPower;
        clone.cosPhi = this.cosPhi;
        clone.isOn = this.isOn;
        clone.actualActivePower = this.actualActivePower;
        clone.actualReactivePower = this.actualReactivePower;
        clone.actualThermalPower = this.actualThermalPower;
        clone.actualGasPower = this.actualGasPower;
        clone.avgActualActivePower = this.avgActualActivePower;
        clone.avgActualReactivePower = this.avgActualReactivePower;
        clone.avgActualThermalPower = this.avgActualThermalPower;
        clone.avgActualGasPower = this.avgActualGasPower;
        clone.lastThermalPower = this.lastThermalPower;
        clone.runningSince = this.runningSince;
        clone.stoppedSince = this.stoppedSince;
        return clone;
    }

}
