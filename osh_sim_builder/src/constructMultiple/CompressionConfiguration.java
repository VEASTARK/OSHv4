package constructMultiple;

import osh.datatypes.power.LoadProfileCompressionTypes;

public class CompressionConfiguration {

    public final LoadProfileCompressionTypes compressionType;
    public final int compressionValue;
    /**
     * @param compressionType
     * @param compressionValue
     */
    public CompressionConfiguration(LoadProfileCompressionTypes compressionType, int compressionValue) {
        super();
        this.compressionType = compressionType;
        this.compressionValue = compressionValue;
    }

    public String toShortName() {
        if (this.compressionType == LoadProfileCompressionTypes.DISCONTINUITIES)
            return "disc-" + this.compressionValue + "W";
        else
            return "slots-" + this.compressionValue + "s";
    }

}
