package constructsimulation.configuration.general;

import constructsimulation.configuration.CAL.GenerateCAL;
import constructsimulation.configuration.EAL.GenerateEAL;
import constructsimulation.configuration.OC.GenerateOC;
import constructsimulation.configuration.OSH.GenerateOSH;
import osh.utils.xml.XMLSerialization;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;

/**
 * Generation class for the whole simulation configuration.
 *
 * @author Sebastian Kramer
 */
public class Generate {

    private static void deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        path.delete();
    }

    public static void main(String[] args) {

        long startTimeStamp = ZonedDateTime.now().toEpochSecond();

        String filePath = FileReferenceStorage.configFilesPath + "simulationPackages/" + startTimeStamp + "/";

        System.out.println("Generating ..... please wait");

        //check package path
        File fPackagePath = new File(filePath);

        if (fPackagePath.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do {
                System.out.println();
                System.out.println("ERROR: package already exists. Delete (y/n)?");
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (!line.equals("y") && !line.equals("n"));

            if (line.equals("y")) {
                deleteDirectory(fPackagePath);
                if (fPackagePath.exists()) throw new RuntimeException("It still exists!");
            } else {
                System.out.println("Aborting...");
                System.exit(1);
            }
        }

        // create paths
        fPackagePath.mkdirs();
        File fSystem = new File(filePath + FileReferenceStorage.systemPath);
        fSystem.mkdir();


        generate(filePath, true);
    }

    public static void generate(String filePath) {
        //check package path
        File fPackagePath = new File(filePath);

        if (fPackagePath.exists()) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String line;
            do {
                System.out.println();
                System.out.println("ERROR: package already exists. Delete (y/n)?");
                try {
                    line = reader.readLine();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } while (!line.equals("y") && !line.equals("n"));

            if (line.equals("y")) {
                deleteDirectory(fPackagePath);
                if (fPackagePath.exists()) throw new RuntimeException("It still exists!");
            } else {
                System.out.println("Aborting...");
                System.exit(1);
            }
        }

        // create paths
        fPackagePath.mkdirs();
        File fSystem = new File(filePath + FileReferenceStorage.systemPath);
        fSystem.mkdir();

        generate(filePath, false);
    }

    private static void generate(String filePath, boolean applyConfigurations) {
        String fileSuffix = ".xml";

        if (applyConfigurations) AllComponents.applyConfigurations();

        try {
            XMLSerialization.marshal2File(
                    filePath + FileReferenceStorage.systemPath + FileReferenceStorage.EALConfigFileName +
                            fileSuffix, GenerateEAL.generateEAL(applyConfigurations));
            XMLSerialization.marshal2File(
                    filePath + FileReferenceStorage.systemPath + FileReferenceStorage.OCConfigFileName +
                            fileSuffix, GenerateOC.generateOCConfig());
            XMLSerialization.marshal2File(
                    filePath + FileReferenceStorage.systemPath + FileReferenceStorage.CALConfigFileName +
                            fileSuffix, GenerateCAL.generateCAL(applyConfigurations));
            XMLSerialization.marshal2File(
                    filePath + FileReferenceStorage.systemPath + FileReferenceStorage.OSHConfigFileName +
                            fileSuffix, GenerateOSH.generateOSH());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
