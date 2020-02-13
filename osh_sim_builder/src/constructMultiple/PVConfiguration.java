package constructMultiple;

public class PVConfiguration {


    public final int pvComplexPowerMax;
    public final double pvCosPhiMax;
    public final int pvNominalPower;
    public final boolean usePVRealHOLL;

    public PVConfiguration(int pvComplexPowerMax, double pvCosPhiMax,
                           int pvNominalPower, boolean usePVRealESHL, boolean usePVRealHOLL) {
        this.pvComplexPowerMax = pvComplexPowerMax;
        this.pvCosPhiMax = pvCosPhiMax;
        this.pvNominalPower = pvNominalPower;
        this.usePVRealHOLL = usePVRealHOLL;
    }

    public String toShortName() {
        if (!this.usePVRealHOLL)
            return "npv";
        String name = "pv";
        name += "HOLL";
        name += this.pvNominalPower;
        return name;
    }

}
