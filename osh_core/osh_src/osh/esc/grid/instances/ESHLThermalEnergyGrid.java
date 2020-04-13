package osh.esc.grid.instances;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.ThermalConnection;

import java.util.UUID;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ESHLThermalEnergyGrid extends GridInstance {

    /**
     * CONSTRUCTOR
     */
    public ESHLThermalEnergyGrid() {
        // ### Devices ###

        // Virtual Smart Meter
        EnergySourceSink eshlMeter =
                new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));

        // Combined Hot Water Storage
        EnergySourceSink combinedHotWaterStorage =
                new EnergySourceSink(UUID.fromString("268ea9bd-572c-46dd-a383-960b4ed65337"));

        // CHP
        EnergySourceSink chp =
                new EnergySourceSink(UUID.fromString("e83c5db0-93d9-4a24-9e7a-c756b67e0802"));

        // Electrical Insert Heating Element
        EnergySourceSink electricHeater =
                new EnergySourceSink(UUID.fromString("d23f44bc-1b3e-4e38-a8e8-17bd618b4fe0"));

        // Domestic Hot Water Usage
        EnergySourceSink dhwu =
                new EnergySourceSink(UUID.fromString("db56fbb0-c305-4361-839a-9f2ba5809611"));

        // Space Heating Hot Water Usage
        EnergySourceSink spaceHeating =
                new EnergySourceSink(UUID.fromString("2a6e51d7-2f18-4034-bd9a-4ed68acc7bfe"));


        // Chilled Water Storage
        EnergySourceSink chilledWaterStorage =
                new EnergySourceSink(UUID.fromString("bc2b5c73-ca4d-42c3-ac5a-56064e0d8112"));

        // Space Cooling Hot Water Usage
        EnergySourceSink spaceCooling =
                new EnergySourceSink(UUID.fromString("a3f762e1-373d-4aa2-b1c3-44967646762a"));


        // ### Relations ###

        // Combined Hot Water Storage <-> CHP
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    chp,
                    combinedHotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // CHP <-> Meter
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    chp,
                    eshlMeter,
                    new ThermalConnection(Commodity.NATURALGASPOWER),
                    new ThermalConnection(Commodity.NATURALGASPOWER));
            this.relationList.add(relation);
        }

        // Combined Hot Water Storage <-> Electric Heater
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    electricHeater,
                    combinedHotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Combined Hot Water Storage <-> Domestic Hot Water Usage
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    dhwu,
                    combinedHotWaterStorage,
                    new ThermalConnection(Commodity.DOMESTICHOTWATERPOWER),
                    new ThermalConnection(Commodity.DOMESTICHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Combined Hot Water Storage <-> Heating Hot Water Usage
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    spaceHeating,
                    combinedHotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Combined Hot Water Storage <-> Space Heating
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    spaceHeating,
                    combinedHotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }


        // Chilled Water Storage <-> Space Cooling
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    spaceCooling,
                    chilledWaterStorage,
                    new ThermalConnection(Commodity.COLDWATERPOWER),
                    new ThermalConnection(Commodity.COLDWATERPOWER));
            this.relationList.add(relation);
        }

        this.meter = eshlMeter;
    }
}
