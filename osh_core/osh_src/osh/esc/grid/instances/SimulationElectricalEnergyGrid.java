package osh.esc.grid.instances;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.ElectricalConnection;

import java.util.*;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class SimulationElectricalEnergyGrid extends GridInstance {


    // ### Devices ###

    // Adsorption Chiller
    final EnergySourceSink adsorptionChiller =
            new EnergySourceSink(UUID.fromString("66a74134-4e47-4fc3-8519-acb2817ecd1a"));
    // Virtual Smart Meter
    private final EnergySourceSink meter =
            new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));
    // BASELOAD
    private final EnergySourceSink baseload =
            new EnergySourceSink(UUID.fromString("00000000-0000-5348-424c-000000000000"));
    // PV
    private final EnergySourceSink pv =
            new EnergySourceSink(UUID.fromString("484F4C4C-0000-0000-5056-000000000000"));
    // PVESHL
    private final EnergySourceSink pvESHL =
            new EnergySourceSink(UUID.fromString("7fc1f1d9-39c3-4e5f-8907-aeb0cd1ee84c"));
    // Battery
    private final EnergySourceSink battery =
            new EnergySourceSink(UUID.fromString("42415454-4552-5900-0000-000000000000"));
    // Dishwasher
    private final EnergySourceSink applianceDW =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4457-000000000000"));
    // Dryer
    private final EnergySourceSink applianceTD =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-5444-000000000000"));
    // Cooktop
    private final EnergySourceSink applianceIH =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4948-000000000000"));
    // Oven
    private final EnergySourceSink applianceOV =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-4F56-000000000000"));
    // Washer
    private final EnergySourceSink applianceWM =
            new EnergySourceSink(UUID.fromString("00000000-4D49-4D49-574D-000000000000"));
    // Dachs CHP
    private final EnergySourceSink chp =
            new EnergySourceSink(UUID.fromString("44414348-5300-0043-4850-000000000000"));
    // E.G.O. Insert Heating Element
    private final EnergySourceSink ihe =
            new EnergySourceSink(UUID.fromString("45474F00-0000-0049-4845-000000000000"));
    // Gas Heating
    private final EnergySourceSink ghd =
            new EnergySourceSink(UUID.fromString("00000000-0000-5748-4748-000000000000"));


    /**
     * CONSTRUCTOR
     */
    public SimulationElectricalEnergyGrid() {
        super();

        // ### Build sourceSinkList ###

        this.sourceSinkList.add(this.meter);
        this.sourceSinkList.add(this.baseload);
        this.sourceSinkList.add(this.pv);
        this.sourceSinkList.add(this.pvESHL);
        this.sourceSinkList.add(this.applianceDW);
        this.sourceSinkList.add(this.applianceTD);
        this.sourceSinkList.add(this.applianceWM);
        this.sourceSinkList.add(this.applianceIH);
        this.sourceSinkList.add(this.applianceOV);
        this.sourceSinkList.add(this.chp);
        this.sourceSinkList.add(this.ihe);
        this.sourceSinkList.add(this.ghd);
        this.sourceSinkList.add(this.battery);
        this.sourceSinkList.add(this.adsorptionChiller);

        // ### Relations ###

        // Meter <-> baseload
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.baseload,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.baseload,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> PV
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.pv,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER)
            );
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.pv,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> PVESHL
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.pvESHL,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER)
            );
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.pvESHL,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> battery
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.battery,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.battery,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> IHE
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.ihe,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.ihe,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> Gas Heating
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.ghd,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.ghd,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }


        // Meter <-> Dachs CHP
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.chp,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.chp,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }


        // Meter <-> Dishwasher
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceDW,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceDW,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> Cooktop / Induction Hob
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceIH,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceIH,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> Oven
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceOV,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceOV,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> Dryer
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceTD,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceTD,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> Washer
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceWM,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceWM,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        // Meter <-> AdsorptionChiller
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.adsorptionChiller,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.adsorptionChiller,
                    this.meter,
                    new ElectricalConnection(Commodity.REACTIVEPOWER),
                    new ElectricalConnection(Commodity.REACTIVEPOWER));
            this.relationList.add(relation);
        }

        this.meters.add(this.meter);

        Map<String, List<EnergySourceSink>> spec = new HashMap<>();
        List<EnergySourceSink> pvs = new ArrayList<>();
        List<EnergySourceSink> chps = new ArrayList<>();
        List<EnergySourceSink> batteries = new ArrayList<>();

        pvs.add(this.pv);
        pvs.add(this.pvESHL);
        chps.add(this.chp);
        batteries.add(this.battery);

        spec.put("pv", pvs);
        spec.put("chp", chps);
        spec.put("battery", batteries);

        this.specialSnowflakes.put(this.meter, spec);
    }
}
