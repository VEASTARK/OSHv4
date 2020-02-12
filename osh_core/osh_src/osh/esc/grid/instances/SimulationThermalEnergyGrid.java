package osh.esc.grid.instances;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.ThermalConnection;

import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SimulationThermalEnergyGrid extends GridInstance {

    // ### Devices ###

    // Dishwasher
    final EnergySourceSink applianceDW =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4457-000000000000"));
    // Dryer
    final EnergySourceSink applianceTD =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-5444-000000000000"));
    // Washer
    final EnergySourceSink applianceWM =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-574D-000000000000"));
    // Hot Water Storage
    final EnergySourceSink hotWaterStorage =
            new EnergySourceSink(UUID.fromString("00000000-0000-4857-4853-000000000000"));
    // Dachs CHP
    final EnergySourceSink chp =
            new EnergySourceSink(UUID.fromString("44414348-5300-0043-4850-000000000000"));
    // E.G.O. Insert Heating Element
    final EnergySourceSink ihe =
            new EnergySourceSink(UUID.fromString("45474F00-0000-0049-4845-000000000000"));
    // Gas Heating
    final EnergySourceSink ghd =
            new EnergySourceSink(UUID.fromString("00000000-0000-5748-4748-000000000000"));

//	// Cold Water Storage
    //	// Adsorption Chiller
final EnergySourceSink adsorptionChiller =
            new EnergySourceSink(UUID.fromString("66a74134-4e47-4fc3-8519-acb2817ecd1a"));
    //	// Cold Water Usage
    final EnergySourceSink coldWaterStorage =
            new EnergySourceSink(UUID.fromString("441c234e-d340-4c85-b0a0-dbac182b8f81"));
    //Space Cooling
    final EnergySourceSink spaceCooling =
            new EnergySourceSink(UUID.fromString("0121431a-6960-46d7-b8e9-337f7135cb4d"));
    // Domestic Hot Water Usage
    final EnergySourceSink dhwUsage =
            new EnergySourceSink(UUID.fromString("00000000-0000-5348-4448-000000000000"));
    // Space Heating
    final EnergySourceSink spaceHeating =
            new EnergySourceSink(UUID.fromString("00000000-0000-5348-5348-000000000000"));
    // Virtual Smart Meter
    private final EnergySourceSink meter =
            new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    // Cooktop / Induction Hob
    private final EnergySourceSink applianceIH =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4948-000000000000"));
    // Oven
    private final EnergySourceSink applianceOV =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4F56-000000000000"));


    /**
     * CONSTRUCTOR
     */
    public SimulationThermalEnergyGrid() {

        // ### Build sourceSinkList ###

        this.sourceSinkList.add(this.meter);
        this.sourceSinkList.add(this.applianceDW);
        this.sourceSinkList.add(this.applianceTD);
        this.sourceSinkList.add(this.applianceWM);
        this.sourceSinkList.add(this.applianceIH);
        this.sourceSinkList.add(this.applianceOV);
        this.sourceSinkList.add(this.hotWaterStorage);
        this.sourceSinkList.add(this.chp);
        this.sourceSinkList.add(this.ihe);
        this.sourceSinkList.add(this.ghd);
        this.sourceSinkList.add(this.dhwUsage);
        this.sourceSinkList.add(this.spaceHeating);
        this.sourceSinkList.add(this.coldWaterStorage);
        this.sourceSinkList.add(this.adsorptionChiller);
        this.sourceSinkList.add(this.spaceCooling);

        // ### Relations ###

        // GHD <-> Hot Water Storage
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.ghd,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.ghd,
                    this.meter,
                    new ThermalConnection(Commodity.NATURALGASPOWER),
                    new ThermalConnection(Commodity.NATURALGASPOWER));
            this.relationList.add(relation);
        }


        // IHE <-> Hot Water Storage
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.ihe,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }


        // Dachs CHP <-> Hot Water Storage
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.chp,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.chp,
                    this.meter,
                    new ThermalConnection(Commodity.NATURALGASPOWER),
                    new ThermalConnection(Commodity.NATURALGASPOWER));
            this.relationList.add(relation);
        }


        // Hot Water Storage <-> Space Heating
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.spaceHeating,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Hot Water Storage <-> Domestic Hot Water
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.dhwUsage,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.DOMESTICHOTWATERPOWER),
                    new ThermalConnection(Commodity.DOMESTICHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Hot Water Storage <-> Dishwasher
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.applianceDW,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Hot Water Storage <-> Dryer
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.applianceTD,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Hot Water Storage <-> Washer
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.applianceWM,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        // Cooktop
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.applianceIH,
                    this.meter,
                    new ThermalConnection(Commodity.NATURALGASPOWER),
                    new ThermalConnection(Commodity.NATURALGASPOWER));
            this.relationList.add(relation);
        }

        // Oven
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.applianceOV,
                    this.meter,
                    new ThermalConnection(Commodity.NATURALGASPOWER),
                    new ThermalConnection(Commodity.NATURALGASPOWER));
            this.relationList.add(relation);
        }

        // Cold Water Storage <-> Space Cooling
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.spaceCooling,
                    this.coldWaterStorage,
                    new ThermalConnection(Commodity.COLDWATERPOWER),
                    new ThermalConnection(Commodity.COLDWATERPOWER));
            this.relationList.add(relation);
        }

        // Cold Water Storage <-> Adsorption Chiller
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.adsorptionChiller,
                    this.coldWaterStorage,
                    new ThermalConnection(Commodity.COLDWATERPOWER),
                    new ThermalConnection(Commodity.COLDWATERPOWER));
            this.relationList.add(relation);
        }

        // Hot Water Storage <-> Adsorption Chiller
        {
            EnergyRelation<ThermalConnection> relation = new EnergyRelation<>(
                    this.adsorptionChiller,
                    this.hotWaterStorage,
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER),
                    new ThermalConnection(Commodity.HEATINGHOTWATERPOWER));
            this.relationList.add(relation);
        }

        this.meters.add(this.meter);
    }
}
