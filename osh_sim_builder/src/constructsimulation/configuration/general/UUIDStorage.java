package constructsimulation.configuration.general;

import java.util.UUID;

/**
 * Static storage for uuids.
 *
 * @author Sebastian Kramer
 */
public class UUIDStorage {

    /**
     * UUID of Global O/C Unit
     */
    public static UUID globalOCUuid = UUID.fromString("e5ad4b36-d417-4be6-a1c8-c3ad68e52977");

    /* #########################
     * # Communication Devices #
     * ######################### */
    public static UUID epsSignalUuid = UUID.fromString("0909624f-c281-4713-8bbf-a8eaf3f8e7d6");
    public static UUID plsSignalUuid = UUID.fromString("99999999-c281-4713-8bbf-a8eaf3f8e7d6");
    public static UUID comDeviceIdDoF = UUID.fromString("32c8b193-6c86-4abd-be5a-2e49fee11535");
    public static UUID comDeviceIdGui = UUID.fromString("6e95bc70-57cb-11e1-b86c-0800200c9a66");

    // ### Baseload
    public static UUID baseloadUUID = UUID.fromString("00000000-0000-5348-424C-000000000000"); // SH-BL

    // ### Big 5 ###
    public static UUID[] applianceUUID = {
            UUID.fromString("00000000-4D49-4D49-4457-000000000000"), // DW
            UUID.fromString("00000000-4D49-4D49-4948-000000000000"), // IH
            UUID.fromString("00000000-4D49-4D49-4F56-000000000000"), // OV
            UUID.fromString("00000000-4D49-4D49-5444-000000000000"), // TD
            UUID.fromString("00000000-4D49-4D49-574D-000000000000"), // WM
    };

    // ### FreezerRefrigerator (FR) ###
    public static UUID freezerRefrigeratorUUID = UUID.fromString("00000000-0000-0000-4652-000000000000"); //FR

    // ### PV System ###
    public static UUID pvUUID = UUID.fromString("484F4C4C-0000-0000-5056-000000000000"); //HOLL...PV

    // ### CHP ###
    public static UUID chpUUID = UUID.fromString("44414348-5300-0043-4850-000000000000"); //DACH-S...C-HP

    // ### Battery Storage ###
    public static UUID batteryUUID = UUID.fromString("42415454-4552-5900-0000-000000000000"); //BATT-ER-Y...

    // ### IHE ###
    public static UUID iheUUID = UUID.fromString("45474F00-0000-0049-4845-000000000000"); //EGO...I-HE

    // ### Gas Heating / Boiler ###
    public static UUID gasHeatingUUID = UUID.fromString("00000000-0000-5748-4748-000000000000"); // WH-GH

    // ### Adsorption Chiller ###
    public static UUID adCHUUID = UUID.fromString("66a74134-4e47-4fc3-8519-acb2817ecd1a");

    // ### Hot WaterStorage ###
    public static UUID hotWaterTankUUID = UUID.fromString("00000000-0000-4857-4853-000000000000"); //HW-HS

    // ### Chilled WaterStorage ###
    public static UUID coldWaterTankUUID = UUID.fromString("441c234e-d340-4c85-b0a0-dbac182b8f81"); //HW-HS

    // ### Domestic Hot Water Usage ###
    public static UUID dhwUsageUUID = UUID.fromString("00000000-0000-5348-4448-000000000000"); //SH-DH

    // ### Space Heating ###
    public static UUID spaceHeatingUUID = UUID.fromString("00000000-0000-5348-5348-000000000000"); //SH-SH

    // ### Space Cooling ###
    public static UUID spaceCoolingUUID = UUID.fromString("00000000-0000-5348-5343-000000000000"); //SH-SC

    // ### Virtual Smart Meter ###
    public static UUID meterUUID = UUID.fromString("00000000-0000-0000-0000-000000000000"); //SH-SC
}
