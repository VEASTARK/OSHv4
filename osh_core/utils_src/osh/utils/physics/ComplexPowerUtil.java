package osh.utils.physics;

/**
 * complexPower S in [VA]: S^2 = P^2 + Q^2<br>
 * cosPhi = P / S (Active Factor, DE: Wirkfaktor)
 *
 * @author Ingo Mauser
 */
public class ComplexPowerUtil {
    /**
     * EN: Active Power ^= Real Power P in [W]<br>
     * Value > 0 : energy consumption<br>
     * Value < 0 : energy generation<br>
     * DE: Wirkleistung
     */
    private double activePower;

    /**
     * EN: Reactive Power Q in [VAR]<br>
     * Value > 0 : inductive<br>
     * Value < 0 : capacitive<br>
     * DE: Blindleistung
     */
    private double reactivePower;

    /**
     * EN: Complex Power S in [VA]: S^2 = P^2 + Q^2<br>
     * DE: Scheinleistung
     */
    private double complexPower;

    /**
     * EN: Active Factor cosPhi = P / S<br>
     * DE: Wirkfaktor<br>
     * ^= Power Factor (DE: Leistungsfaktor) in case of no harmonics (DE: Oberschwingungen)<br>
     * (Note: Displacement Factor, DE: Verschiebungsfaktor, |P| / S = |cos(Phi)|)
     */
    private double cosPhi;

    /**
     * EN: Phase difference angle phi in [rad]<br>
     * DE: Phasenverschiebungswinkel
     */
    private final double phiInRadian;


    /**
     * EN: inductive true/false positive/negative Reactive Power (false ^= capacitive)
     * DE: induktiv true/false positive/negative Blindleistung (false ^= kapazitiv)
     */
    private boolean inductive;

    /**
     * You need sometimes more info than 2 values!<br>
     * a) activePower and cosPhi is NOT sufficient -> needs inductive/capacitive<br>
     * b) activePower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
     * <b>c) reactivePower and cosPhi IS sufficient (IF reactivePower & cosPhi != 0)</b><br>
     * d) reactivePower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
     * e) complexPower and cosPhi is NOT sufficient -> needs inductive/capacitive<br>
     * f) complexPower and inductive/capacitive is NOT sufficient -> needs cosPhi<br>
     *
     * @param power     (0) activePower or (1) reactivePower or (2) complexPower
     * @param cosPhi    Active Factor cosPhi (DE: Wirkfaktor)
     * @param powerType 0: activePower 1: reactivePower 2: complexPower else: activePower
     * @param inductive true: inductive, false: capacitive
     * @throws Exception
     */
    public ComplexPowerUtil(double power, int powerType, double cosPhi, boolean inductive) throws Exception {
        double internalPower = power;
        double checkedCosPhi = checkCosPhi(cosPhi);

        this.cosPhi = checkedCosPhi;
        this.inductive = inductive;

        if (inductive) {
            this.phiInRadian = Math.acos(checkedCosPhi);
        } else {
            this.phiInRadian = 2 * Math.PI - Math.acos(checkedCosPhi);
        }

        // 0: activePower
        if (powerType == 0) {
            if (checkedCosPhi == 0 || internalPower == 0) {
                throw new Exception("ERROR: Impossible to compute reactivePower and complexPower with activePower = 0 or cosPhi = 0! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
            } else {
                this.activePower = internalPower;
                this.reactivePower = convertActiveToReactivePower(internalPower, checkedCosPhi, inductive); //???
                this.complexPower = convertActiveToComplexPower(internalPower, checkedCosPhi); //???
            }
        }
        // 1: reactivePower
        else if (powerType == 1) {
            this.activePower = convertReactiveToActivePower(internalPower, checkedCosPhi);
            if (!(this.activePower == this.activePower)) { //IMPORTANT: NaN!!!
                throw new Exception("ERROR: Impossible to compute activePower and complexPower with given input values! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
            } else {
                this.reactivePower = internalPower; //???
                this.complexPower = convertReactiveToComplexPower(internalPower, checkedCosPhi); //???
            }
        }
        // 2: complexPower
        else if (powerType == 2) {
            internalPower = checkComplexPower(internalPower);

            this.activePower = convertComplexToActivePower(internalPower, checkedCosPhi);
            this.reactivePower = convertComplexToReactivePower(internalPower, checkedCosPhi, inductive); //???
            this.complexPower = internalPower; //???
        }
        // else: activePower
        else {
            if (checkedCosPhi == 0 || internalPower == 0) {
                throw new Exception("ERROR: Impossible to compute reactivePower and complexPower with activePower = 0 or cosPhi = 0! Setting activePower, reactivePower, complexPower and cosPhi to 0.");
//				this.activePower = 0;
//				this.reactivePower = 0;
//				this.complexPower = 0;
//				this.cosPhi = 0;
            } else {
                this.activePower = internalPower;
                this.reactivePower = convertActiveToReactivePower(internalPower, checkedCosPhi, inductive); //???
                this.complexPower = convertActiveToComplexPower(internalPower, checkedCosPhi); //???
            }
        }
    }

    /**
     * Default: Power=activePower (needs always 3 parameters for full information)
     *
     * @param activePower ^= Real Power P in [W] (DE: Wirkleistung)
     * @param cosPhi      Active Factor cosPhi (DE: Wirkfaktor)
     * @param inductive   true: inductive, false: capacitive
     * @throws Exception
     */
    public ComplexPowerUtil(double activePower, double cosPhi, boolean inductive) throws Exception {
        this(activePower, 0, cosPhi, inductive);
    }

    /**
     * <b>Reactive Power only!</b><br>
     * Power = reactivePower Q, cosPhi && Q != 0 (otherwise not computable)<br>
     * activePower = 0 if cosPhi = 0<br>
     * activePower = Double.POSITIVE_INFINITY if cosPhi = 1 & reactivePower != 0<br>
     * activePower = Double.NEGATIVE_INFINITY if cosPhi = -1 & reactivePower != 0<br>
     * activePower = Double.NaN if cosPhi = 1 & reactivePower == 0<br>
     * activePower = Double.NaN if cosPhi = -1 & reactivePower == 0
     *
     * @param reactivePower Q in [VAR] (DE: Blindleistung)
     * @param cosPhi        Active Factor cosPhi (DE: Wirkfaktor)
     * @throws Exception
     */
    public ComplexPowerUtil(double reactivePower, double cosPhi) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);
        this.cosPhi = checkedCosPhi;

        if (reactivePower >= 0) {
            this.inductive = true;
            this.phiInRadian = Math.acos(checkedCosPhi);
        } else {
            this.inductive = false;
            this.phiInRadian = 2 * Math.PI - Math.acos(checkedCosPhi);
        }

        this.activePower = convertReactiveToActivePower(reactivePower, checkedCosPhi);
        this.reactivePower = reactivePower; //???
        this.complexPower = convertReactiveToComplexPower(reactivePower, checkedCosPhi); //???
    }

    /**
     * @param activePower P (^= Real Power) in [W] (DE: Wirkleistung)
     * @param cosPhi      Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return reactivePower Q in [VAR] (DE: Blindleistung)<br>
     * @throws Exception
     */
    public static double convertActiveToReactivePower(double activePower, double cosPhi, boolean inductive) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);

        double reactivePower;

        if (checkedCosPhi == 0) {
            throw new Exception("cosPhi=0 is not possible!");
        } else if (checkedCosPhi == 1 || checkedCosPhi == -1) {
            reactivePower = 0;
        } else {
            double complexPower = convertActiveToComplexPower(activePower, checkedCosPhi);
            reactivePower = Math.sqrt((complexPower * complexPower) - (activePower * activePower));

            if (!inductive) {
                reactivePower = (-1) * reactivePower; // negative reactivePower
            }
        }

        return reactivePower;
    }

    /**
     * @param reactivePower Q in [VAR] (DE: Blindleistung)
     * @param cosPhi        Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return activePower P (^= Real Power) in [W] (DE: Wirkleistung)<br>
     * @throws Exception
     */
    public static double convertReactiveToActivePower(double reactivePower, double cosPhi) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);

        double activePower;

        if (checkedCosPhi == 0) {
            activePower = 0;
        } else if (checkedCosPhi == 1 && reactivePower != 0) {
            throw new Exception("ERROR: CosPhi = 1 and reactivePower != 0 is impossible!");
        } else if (checkedCosPhi == -1 && reactivePower != 0) {
            throw new Exception("ERROR: CosPhi = -1 and reactivePower != 0 is impossible!");
        } else if (checkedCosPhi == 1 && reactivePower == 0) {
            throw new Exception("ERROR: Impossible to calculate activePower from cosPhi = 1 and reactivePower == 0!");
        } else if (checkedCosPhi == -1 && reactivePower == 0) {
            throw new Exception("ERROR: Impossible to calculate activePower from cosPhi = -1 and reactivePower == 0!");
        } else {
            double activePower2 = (reactivePower * reactivePower) / ((1 / (checkedCosPhi * checkedCosPhi)) - 1);
            activePower = Math.sqrt(activePower2);

            if (checkedCosPhi < 0) {
                // Depends on when power is negative
                activePower = (-1) * activePower;
            }
        }

        return activePower;
    }

    /**
     * @param activePower P (^= Real Power) in [W] (DE: Wirkleistung)
     * @param cosPhi      Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return complexPower S in [VA]: S^2 = P^2 + Q^2
     * @throws Exception
     */
    public static double convertActiveToComplexPower(double activePower, double cosPhi) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);

        double complexPower;

        if (checkedCosPhi != 0) {
            complexPower = activePower / checkedCosPhi;
        } else {
            throw new Exception("ERROR: ComplexPower not computeable from activePower for cosPhi = 0! Returning Double.NaN.");
        }
        return complexPower;
    }

    /**
     * @param complexPower S in [VA]: S^2 = P^2 + Q^2
     * @param cosPhi       Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return activePower P (^= Real Power) in [W] (DE: Wirkleistung)
     * @throws Exception
     */
    public static double convertComplexToActivePower(double complexPower, double cosPhi) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);
        double checkedComplexPower = checkComplexPower(complexPower);

        double activePower;
        activePower = checkedComplexPower * checkedCosPhi;
        return activePower;
    }

    /**
     * @param reactivePower Q in [VAR] (DE: Blindleistung)
     * @param cosPhi        Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return complexPower S in [VA]: S^2 = P^2 + Q^2<br>
     * returns Double.NaN if activePower = NaN || POSITIVE_INFINITY || NEGATIVE_INFINITY
     * @throws Exception
     */
    public static double convertReactiveToComplexPower(double reactivePower, double cosPhi) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);

        double activePower = convertReactiveToActivePower(reactivePower, checkedCosPhi);
        double complexPower;

        if (!(activePower == activePower)) { //ACHTUNG: NaN!!!!
            complexPower = Double.NaN;
        } else {
            complexPower = convertActiveToComplexPower(activePower, checkedCosPhi);
        }

        return complexPower;
    }

    /**
     * @param complexPower S in [VA]: S^2 = P^2 + Q^2
     * @param cosPhi       Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return reactivePower Q in [VAR] (DE: Blindleistung)
     * @throws Exception
     */
    public static double convertComplexToReactivePower(double complexPower, double cosPhi, boolean inductive) throws Exception {
        double checkedComplexPower = checkComplexPower(complexPower);
        double checkedCosPhi = checkCosPhi(cosPhi);

        double reactivePower;

        if (checkedCosPhi == 0) {
            reactivePower = checkedComplexPower;
        } else {
            double activePower = convertComplexToActivePower(checkedComplexPower, checkedCosPhi);
            reactivePower = convertActiveToReactivePower(activePower, checkedCosPhi, inductive);
        }

        return reactivePower;
    }

    /**
     * @param activePower
     * @param reactivePower
     * @return
     */
    public static double convertActiveAndReactivePowerToComplexPower(double activePower, double reactivePower) {
        return Math.sqrt(activePower * activePower + reactivePower * reactivePower);
    }

    /**
     * @param activePower
     * @param reactivePower
     * @return
     * @throws Exception
     */
    public static double convertActiveAndReactivePowerToCosPhi(double activePower, double reactivePower) throws Exception {
        double complexPower = convertActiveAndReactivePowerToComplexPower(activePower, reactivePower);
        double cosPhi = activePower / complexPower;
        cosPhi = checkCosPhi(cosPhi);
        return cosPhi;
    }

    /**
     * Check whether cosPhi is in [-1,1], otherwise set to border value
     *
     * @param cosPhi Active Factor: cosPhi = P / S (DE: Wirkfaktor)
     * @return Corrected cosPhi
     * @throws Exception
     */
    public static double checkCosPhi(double cosPhi) throws Exception {
        if (cosPhi > 1) {
            throw new Exception("ERROR: CosPhi is NEVER > 1! Adjusted to 1.");
        } else if (cosPhi < -1) {
            throw new Exception("ERROR: CosPhi is NEVER <-1! Adjusted to 1.");
        }
        return cosPhi;
    }

    /**
     * Check whether phi could reduced
     *
     * @param phiInRadian
     * @return
     */
    public static double checkPhiRadian(double phiInRadian) {
        double reducedPhiInRadian = phiInRadian;
        while (reducedPhiInRadian >= 2 * Math.PI) {
            reducedPhiInRadian -= 2 * Math.PI;
        }
        while (reducedPhiInRadian < 0) {
            reducedPhiInRadian += 2 * Math.PI;
        }
        return reducedPhiInRadian;
    }

    /**
     * Check whether complexPower is >= 0, otherwise multiply with (-1)
     *
     * @param complexPower S in [VA]: S^2 = P^2 + Q^2
     * @return Corrected complexPower
     * @throws Exception
     */
    public static double checkComplexPower(double complexPower) throws Exception {
        if (complexPower < 0) {
            throw new Exception("ERROR: ComplexPower is NEVER <0! Multiplied with (-1).");
        }
        return complexPower;
    }

    /**
     * Get the Active Power (DE: Wirkleistung)
     *
     * @return Active Power ^= Real Power P in [W] (DE: Wirkleistung)
     */
    public double getActivePower() {
        return this.activePower;
    }

    /**
     * Adjust activePower P, KEEP cosPhi, set whether the state (inductive/capacitive) should remain or change
     *
     * @param activePower        ^= Real Power P in [W] (DE: Wirkleistung)
     * @param keepInductiveState true: inductive, false: !inductive
     * @throws Exception
     */
    public void adjustActivePower(double activePower, boolean keepInductiveState) throws Exception {
        this.activePower = activePower;
        if (keepInductiveState) {
            this.reactivePower = convertActiveToReactivePower(activePower, this.cosPhi, this.inductive); //???
        } else {
            this.reactivePower = convertActiveToReactivePower(activePower, this.cosPhi, !this.inductive); //???
        }
        this.complexPower = convertActiveToComplexPower(activePower, this.cosPhi); //???
    }

    /**
     * Get the<br>
     * EN: Active Factor cosPhi = P / S<br>
     * DE: Wirkfaktor<br>
     * ^= Power Factor (DE: Leistungsfaktor) in case of no harmonics (DE: Oberschwingungen)<br>
     * (Note: Displacement Factor, DE: Verschiebungsfaktor, |P| / S = |cos(Phi)|)
     *
     * @return Active Factor cosPhi (DE: Wirkfaktor)
     */
    public double getCosPhi() {
        return this.cosPhi;
    }

    /**
     * Adjust cosPhi, KEEP the powerType set in powerTypeToKeep, set whether the state (inductive/capacitive) should remain or change
     *
     * @param cosPhi             New Active Factor cosPhi (DE: Wirkfaktor)
     * @param powerTypeToKeep    0: activePower 1: reactivePower 2: complexPower else: activePower
     * @param keepInductiveState true: inductive, false: !inductive
     * @throws Exception
     */
    public void adjustCosPhi(double cosPhi, int powerTypeToKeep, boolean keepInductiveState) throws Exception {
        double checkedCosPhi = checkCosPhi(cosPhi);
        this.cosPhi = checkedCosPhi;

        if (!keepInductiveState) {
            this.inductive = !this.inductive;
        }

        if (powerTypeToKeep == 0) {
            if (checkedCosPhi == 0) {
                if (this.activePower != 0) {
                    throw new Exception("ERROR: Impossible to keep activePower with cosPhi = 0! Setting activePower to 0. Keeping complexPower. Adjusting reactivePower to complexPower.");
//					this.activePower = 0;
//					this.reactivePower = this.complexPower;
                } else {
                    throw new Exception("ERROR: Impossible to keep activePower with cosPhi = 0! Keeping complexPower. Adjusting reactivePower to complexPower.");
//					this.reactivePower = this.complexPower;
                }
            } else {
                this.reactivePower = convertActiveToReactivePower(this.activePower, this.cosPhi, this.inductive);
                this.complexPower = convertActiveToComplexPower(this.activePower, this.cosPhi);
            }
        } else if (powerTypeToKeep == 1) {
            this.activePower = convertReactiveToActivePower(this.reactivePower, this.cosPhi);
            this.complexPower = convertReactiveToComplexPower(this.reactivePower, this.cosPhi);
        } else if (powerTypeToKeep == 2) {
            this.activePower = convertComplexToActivePower(this.complexPower, this.cosPhi);
            this.reactivePower = convertComplexToReactivePower(this.complexPower, this.cosPhi, this.inductive);
        } else {
            this.reactivePower = convertActiveToReactivePower(this.activePower, this.cosPhi, this.inductive);
            this.complexPower = convertActiveToComplexPower(this.activePower, this.cosPhi);
        }
    }

    /**
     * Get the<br>
     * EN: Phase difference angle phi in [rad]<br>
     * DE: Phasenverschiebungswinkel
     *
     * @return Phase difference angle phi in [rad] (DE: Phasenverschiebungswinkel)
     */
    public double getPhiRadian() {
        return this.phiInRadian;
    }

    /**
     * @param phi
     * @param powerTypeToKeep
     * @throws Exception
     */
    public void adjustPhi(double phi, int powerTypeToKeep) throws Exception {
        double checkedPhi = checkPhiRadian(phi);

        if (checkedPhi <= Math.PI) {
            if (this.inductive) {
                this.adjustCosPhi(Math.cos(checkedPhi), powerTypeToKeep, true);
            } else {
                this.adjustCosPhi(Math.cos(checkedPhi), powerTypeToKeep, false);
            }
        } else {
            if (!this.inductive) {
                this.adjustCosPhi(Math.cos(checkedPhi), powerTypeToKeep, true);
            } else {
                this.adjustCosPhi(Math.cos(checkedPhi), powerTypeToKeep, false);
            }
        }
    }

    /**
     * @return Reactive power Q in [VAR] (DE: Blindleistung)
     */
    public double getReactivePower() {
        return this.reactivePower;
    }

    /**
     * @return Complex power S in [VA] (DE: Scheinleistung)
     */
    public double getComplexPower() {
        return this.complexPower;
    }

    /**
     * Get the<br>
     * EN: Phase difference angle phi in [degree]<br>
     * DE: Phasenverschiebungswinkel
     *
     * @return Phase difference angle phi in [degree] (DE: Phasenverschiebungswinkel)
     */
    public double getPhiDegrees() {
        return Math.toDegrees(this.phiInRadian);
    }

    /**
     * Get the Quadrant in Coordinate System
     *
     * @return returns: 1, 2, 3, 4, 12, 23, 34, 41 or 0
     */
    public int getQuadrant() {
        if (this.activePower > 0 && this.reactivePower > 0) {
            return 1;
        } else if (this.activePower < 0 && this.reactivePower > 0) {
            return 2;
        } else if (this.activePower < 0 && this.reactivePower < 0) {
            return 3;
        } else if (this.activePower > 0 && this.reactivePower < 0) {
            return 4;
        } else if (this.activePower == 0 && this.reactivePower > 0) {
            return 12;
        } else if (this.activePower < 0 && this.reactivePower == 0) {
            return 23;
        } else if (this.activePower == 0 && this.reactivePower < 0) {
            return 34;
        } else if (this.activePower > 0 && this.reactivePower == 0) {
            return 41;
        } else {
            return 0;
        }
    }

    /**
     * @return true if Reactive Power > 0
     */
    public boolean isInductive() {
        return (this.reactivePower > 0);
    }

    /**
     * @return true if Reactive Power < 0
     */
    public boolean isCapacitive() {
        return (this.reactivePower < 0);
    }
}
