package osh.toolbox.pv.eshl;

import java.io.*;

public class CombineFiles {

    static final String inputFileName1 = "C:/pv/wiz_3_3_asc.csv";
    static final String inputFileName2 = "C:/pv/wiz_pv_asc.csv";

    static final int upTo = 1342777564;

    static final String outputFileName = "C:/pv/wiz_pv_all_asc.csv";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        String line;

        BufferedReader csvReader = new BufferedReader(new FileReader(new File(inputFileName1)));

        File outputFile = new File(outputFileName);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

        while ((line = csvReader.readLine()) != null) {
            writer.println(line);
        }

        csvReader = new BufferedReader(new FileReader(new File(inputFileName2)));

        while ((line = csvReader.readLine()) != null) {
            int zahl = Integer.parseInt(line.replaceAll("\"", "").split(";")[0]);
            if (zahl >= upTo) {
                break;
            }
            writer.println(line);
        }

        writer.close();

    }

}
