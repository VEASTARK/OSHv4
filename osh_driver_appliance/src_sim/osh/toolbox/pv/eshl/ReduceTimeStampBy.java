package osh.toolbox.pv.eshl;

import osh.utils.csv.CSVImporter;

import java.io.*;


public class ReduceTimeStampBy {

//	static String inputFileName = "C:/pv/raw_2011.csv";
//	static String outputFileName = "C:/pv/raw_2_2011_0.csv";

    static final String inputFileName = "C:/pv/raw_2012.csv";
    static final String outputFileName = "C:/pv/raw_2_2012_0.csv";

    static final String delimiter = ";";

    static int y2011 = 1293836400;
    static final int y2012 = 1325372400;
    static int y2013 = 1356994800;

//	static boolean leapYear = false;

    //	static int yearCorrection = y2011;
    static final int yearCorrection = y2012;

    static int[][] inputArray;

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {

        inputArray = loadProfile(inputFileName);

        File outputFile = new File(outputFileName);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

        for (int[] ints : inputArray) {
            String line = "" + (ints[0] - yearCorrection) + delimiter + ints[1];
            writer.println(line);
        }

        writer.close();

    }


    private static int[][] loadProfile(String fileName) {
        return CSVImporter.readInteger2DimArrayFromFile(fileName, delimiter, "\\\"");
    }

}
