package osh.util;

import osh.configuration.appliance.XsdApplianceProgramConfigurations;
import osh.datatypes.appliance.future.ApplianceProgramConfigurations;
import osh.datatypes.commodity.Commodity;
import osh.datatypes.power.SparseLoadProfile;
import osh.eal.hal.exceptions.HALException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provider and translator class for {@link ApplianceProgramConfigurations}.
 *
 * @author Sebastian Kramer
 */
public class ApplianceConfigurationProviderSingleton {

    private static final Map<String, ApplianceProgramConfigurations> configurationMap = new ConcurrentHashMap<>();

    /**
     * Reads in, transforms and stores the {@link XsdApplianceProgramConfigurations} at the given path.
     *
     * @param profilePath the path to the xml-configurations
     * @param hybridFactor the multiplication factor for hybrid-profiles
     *
     * @return the transformed and stored configurations profiles
     * @throws HALException when no xml-configurations could be found on the path
     * @throws JAXBException when the found files could not be unmarshalled
     */
    public static synchronized ApplianceProgramConfigurations readInConfiguration(String profilePath, double hybridFactor) throws HALException,
            JAXBException {

        String key = profilePath + "_factor=" + hybridFactor;

        if (!configurationMap.containsKey(key)) {
            System.out.println("[---- Reading in new file: " + key + " ----]");
            if (profilePath != null) {
                JAXBContext jaxbWMParameters = JAXBContext.newInstance("osh.configuration.appliance");
                Unmarshaller unmarshallerConfigurations = jaxbWMParameters.createUnmarshaller();
                Object unmarshalledConfigurations = unmarshallerConfigurations.unmarshal(new File(profilePath));
                if (unmarshalledConfigurations instanceof XsdApplianceProgramConfigurations) {
                    ApplianceProgramConfigurations applianceConfigurations = new ApplianceProgramConfigurations(
                            (XsdApplianceProgramConfigurations) unmarshalledConfigurations);

                    for (int i = 0; i < applianceConfigurations.getNumberOfConfigurations(); i++) {
                        SparseLoadProfile[][] loadProfiles = applianceConfigurations.getLoadProfilesOfConfiguration(i);
                        for (SparseLoadProfile[] phases : loadProfiles) {
                            for (SparseLoadProfile phase : phases) {
                                //correct the hybrid profiles
                                phase.multiplyLoadsWithFactor(hybridFactor, Commodity.HEATINGHOTWATERPOWER);
                                phase.multiplyLoadsWithFactor(hybridFactor, Commodity.NATURALGASPOWER);
                            }
                        }
                    }

                    configurationMap.put(key, applianceConfigurations);
                } else {
                    throw new HALException("No valid configurations file found!");
                }
            } else {
                throw new HALException("Appliance configurations are missing!");
            }
        }

        return configurationMap.get(key);
    }
}
