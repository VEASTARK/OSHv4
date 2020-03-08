package constructMultiple;

import constructsimulation.configuration.EAL.electric.consumers.ApplianceType;

public enum DeviceConfiguration {

    NORMAL,
    DELAYABLE,
    INTERRUPTIBLE,
    HYBRID,
    HYBRID_DELAYABLE,
    HYBRID_INTERRUPTIBLE,
    HYBRID_SINGLE;

    //col:
    //  DE,    IH     OV     TD     WM
    private static final ApplianceType[] normalConf = {
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
    };
    private static final ApplianceType[] delayableConf = {
            ApplianceType.DELAYABLE,
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
            ApplianceType.DELAYABLE,
            ApplianceType.DELAYABLE,
    };
    private static final ApplianceType[] interrConf = {
            ApplianceType.INTERRUPTIBLE,
            ApplianceType.STANDARD,
            ApplianceType.STANDARD,
            ApplianceType.INTERRUPTIBLE,
            ApplianceType.INTERRUPTIBLE,
    };

    private static final ApplianceType[] hybrid = {
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
    };

    private static final ApplianceType[] hybridDelayable = {
            ApplianceType.HYBRID_DELAYABLE,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID_DELAYABLE,
            ApplianceType.HYBRID_DELAYABLE,
    };

    private static final ApplianceType[] hybridInterr = {
            ApplianceType.HYBRID_INTERRUPTIBLE,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID,
            ApplianceType.HYBRID_INTERRUPTIBLE,
            ApplianceType.HYBRID_INTERRUPTIBLE,
    };

    private static final ApplianceType[] hybridSingle = {
            ApplianceType.HYBRID_SINGLE,
            ApplianceType.HYBRID_SINGLE,
            ApplianceType.HYBRID_SINGLE,
            ApplianceType.HYBRID_SINGLE,
            ApplianceType.HYBRID_SINGLE,
    };

    public static ApplianceType[] getAppliancesValues(DeviceConfiguration config) {
        switch (config) {
            case NORMAL:
                return normalConf;
            case DELAYABLE:
                return delayableConf;
            case INTERRUPTIBLE:
                return interrConf;
            case HYBRID:
                return hybrid;
            case HYBRID_DELAYABLE:
                return hybridDelayable;
            case HYBRID_INTERRUPTIBLE:
                return hybridInterr;
            case HYBRID_SINGLE:
                return hybridSingle;
            default:
                return null;
        }
    }

    //col:
    //  IH,    DW     OV     TD     WM

    public static String toShortName(DeviceConfiguration config) {
        switch (config) {
            case NORMAL:
                return "app";
            case DELAYABLE:
                return "dapp";
            case INTERRUPTIBLE:
                return "iapp";
            case HYBRID:
                return "happ";
            case HYBRID_DELAYABLE:
                return "hdapp";
            case HYBRID_INTERRUPTIBLE:
                return "hiapp";
            case HYBRID_SINGLE:
                return "hsapp";
            default:
                return null;
        }
    }

    //col:
    //  IH,    DW     OV     TD     WM

    public ApplianceType[] getApplianceValues() {
        switch (this) {
            case NORMAL:
                return normalConf;
            case DELAYABLE:
                return delayableConf;
            case INTERRUPTIBLE:
                return interrConf;
            case HYBRID:
                return hybrid;
            case HYBRID_DELAYABLE:
                return hybridDelayable;
            case HYBRID_INTERRUPTIBLE:
                return hybridInterr;
            case HYBRID_SINGLE:
                return hybridSingle;
            default:
                return null;
        }
    }
}
