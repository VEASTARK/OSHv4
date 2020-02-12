package osh.esc.grid.instances;

import osh.datatypes.commodity.Commodity;
import osh.esc.grid.EnergyRelation;
import osh.esc.grid.EnergySourceSink;
import osh.esc.grid.carrier.ElectricalConnection;

import java.util.*;


/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public class ESHLElectricalEnergyGrid extends GridInstance {


    // ### Devices ###

    // Virtual Smart Meter
    private final EnergySourceSink meter =
            new EnergySourceSink(UUID.fromString("00000000-0000-0000-0000-000000000000"));

    // BASELOAD
    private final EnergySourceSink baseload =
            new EnergySourceSink(UUID.fromString("00000000-0000-5348-424c-000000000000"));

    // PVESHL
    private final EnergySourceSink pvESHL =
            new EnergySourceSink(UUID.fromString("7fc1f1d9-39c3-4e5f-8907-aeb0cd1ee84c"));

    // Battery
    private final EnergySourceSink battery =
            new EnergySourceSink(UUID.fromString("42415454-4552-5900-0000-000000000000"));

    // A/C inverter
    private final EnergySourceSink acInverter =
            new EnergySourceSink(UUID.fromString("61c8cf99-3321-4fc4-bc50-24289f62a49c"));

    // Dishwasher
    private final EnergySourceSink applianceDW =
            new EnergySourceSink(UUID.fromString("a01021ca-4d49-4d49-0000-5601c0a80114"));

    // Dryer
    private final EnergySourceSink applianceTD =
            new EnergySourceSink(UUID.fromString("a0102154-4d49-4d49-0000-5602c0a80114"));

    // Cooktop
    private final EnergySourceSink applianceIH =
            new EnergySourceSink(UUID.fromString("a010338a-4d49-4d49-0000-5e09c0a80114"));

    // Oven
    private final EnergySourceSink applianceOV =
            new EnergySourceSink(UUID.fromString("a0102159-4d49-4d49-0000-5e06c0a80114"));

    // Washer
    private final EnergySourceSink applianceWM =
            new EnergySourceSink(UUID.fromString("a0102151-4d49-4d49-0000-5604c0a80114"));

    // Coffeesystem
    private final EnergySourceSink applianceCS =
            new EnergySourceSink(UUID.fromString("a0103230-4d49-4d49-0000-5e0ac0a80114"));

    // Dachs CHP
    private final EnergySourceSink chp =
            new EnergySourceSink(UUID.fromString("e83c5db0-93d9-4a24-9e7a-c756b67e0802"));

    //	// Insert Heating Element
    private final EnergySourceSink ihe =
            new EnergySourceSink(UUID.fromString("d23f44bc-1b3e-4e38-a8e8-17bd618b4fe0"));


    /**
     * CONSTRUCTOR
     */
    public ESHLElectricalEnergyGrid() {
        super();

        // ### Build sourceSinkList ###

        this.sourceSinkList.add(this.meter);
        this.sourceSinkList.add(this.baseload);
//		sourceSinkList.add(pv);
        this.sourceSinkList.add(this.pvESHL);
        this.sourceSinkList.add(this.applianceDW);
        this.sourceSinkList.add(this.applianceTD);
        this.sourceSinkList.add(this.applianceWM);
        this.sourceSinkList.add(this.applianceIH);
        this.sourceSinkList.add(this.applianceOV);
        this.sourceSinkList.add(this.applianceCS);
        this.sourceSinkList.add(this.chp);
        this.sourceSinkList.add(this.ihe);
//		sourceSinkList.add(ghd);
        this.sourceSinkList.add(this.battery);
        this.sourceSinkList.add(this.acInverter);

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

//		// Meter <-> PV
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					pv,
//					meter,
//					new Electrical(Commodity.ACTIVEPOWER), 
//					new Electrical(Commodity.ACTIVEPOWER)
//					);
//			relationList.add(relation);
//		}
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					pv,
//					meter,
//					new Electrical(Commodity.REACTIVEPOWER), 
//					new Electrical(Commodity.REACTIVEPOWER));
//			relationList.add(relation);
//		}

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

        // Meter <-> A/C inverter
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.acInverter,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.acInverter,
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

//		// Meter <-> Gas Heating
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					ghd,
//					meter,
//					new Electrical(Commodity.ACTIVEPOWER), 
//					new Electrical(Commodity.ACTIVEPOWER));
//			relationList.add(relation);
//		}
//		{
//			EnergyRelation<Electrical> relation = new EnergyRelation<Electrical>(
//					ghd,
//					meter,
//					new Electrical(Commodity.REACTIVEPOWER), 
//					new Electrical(Commodity.REACTIVEPOWER));
//			relationList.add(relation);
//		}


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

        // Meter <-> CoffeeSystem
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceCS,
                    this.meter,
                    new ElectricalConnection(Commodity.ACTIVEPOWER),
                    new ElectricalConnection(Commodity.ACTIVEPOWER));
            this.relationList.add(relation);
        }
        {
            EnergyRelation<ElectricalConnection> relation = new EnergyRelation<>(
                    this.applianceCS,
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

        pvs.add(this.pvESHL);
        chps.add(this.chp);
        batteries.add(this.battery);

        spec.put("pv", pvs);
        spec.put("chp", chps);
        spec.put("battery", batteries);

        this.specialSnowflakes.put(this.meter, spec);
    }
}
