package osh.esc;

import osh.configuration.system.GridConfig;
import osh.eal.hal.exceptions.HALManagerException;
import osh.esc.grid.ElectricalEnergyGrid;
import osh.esc.grid.EnergySimulationTypes;
import osh.esc.grid.IEnergyGrid;
import osh.esc.grid.ThermalEnergyGrid;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Ingo Mauser, Sebastian Kramer
 */
public abstract class EnergySimulationCore {

    protected Map<EnergySimulationTypes, IEnergyGrid> grids = new EnumMap<>(EnergySimulationTypes.class);
    protected UUID meterUUID;


    /**
     * CONSTRUCTOR with GridConfigs and String-UUID
     */
    public EnergySimulationCore(
            List<GridConfig> grids,
            String meterUUID) throws HALManagerException {
        try {
            for (GridConfig singleGrid : grids) {
                if (singleGrid.getGridType().equals("thermal"))
                    this.grids.put(EnergySimulationTypes.THERMAL, new ThermalEnergyGrid(singleGrid.getGridLayoutSource()));
                else
                    this.grids.put(EnergySimulationTypes.ELECTRICAL, new ElectricalEnergyGrid(singleGrid.getGridLayoutSource()));
            }
        } catch (FileNotFoundException | JAXBException e) {
            throw new HALManagerException(e);
        }
        this.meterUUID = UUID.fromString(meterUUID);
    }

    /**
     * CONSTRUCTOR with grids and UUID
     */
    public EnergySimulationCore(Map<EnergySimulationTypes, IEnergyGrid> grids, UUID meterUUID) {
        super();
        this.grids = grids;
        this.meterUUID = meterUUID;
    }

    public Map<EnergySimulationTypes, IEnergyGrid> getGrids() {
        return this.grids;
    }

    public UUID getMeterUUID() {
        return this.meterUUID;
    }
}
