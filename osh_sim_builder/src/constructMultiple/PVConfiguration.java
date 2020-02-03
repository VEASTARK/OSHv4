package constructMultiple;

public class PVConfiguration {


    public final String pvComplexPowerMax;
    public final String pvCosPhiMax;
    public final int pvNominalPower;
    public final boolean usePVRealHOLL;

    public PVConfiguration(String pvComplexPowerMax, String pvCosPhiMax,
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
