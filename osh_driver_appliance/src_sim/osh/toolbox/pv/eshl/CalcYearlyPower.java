package osh.toolbox.pv.eshl;

import osh.utils.physics.PhysicalConstants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class CalcYearlyPower {

    static long longPowerInWs;
    static double doublePowerInKWH;

    static int intKWPeak;

    static final int[] kennLinie = new int[4578 + 1];


    static final String inputFileName = "C:/pv/cleaned_20112012_full.csv";

    static String outputFileName = "C:/pv/kennlinie.csv";

    static int numberOfDaysWithPowerGreater3500;

    /**
     * @param args
     * @throws IOException
     * @throws NumberFormatException
     */
    public static void main(String[] args) throws NumberFormatException, IOException {

        BufferedReader csvReader = new BufferedReader(new FileReader(new File(inputFileName)));
        String line;

        int lastDayWithPowerGreater3000 = -1;

        while ((line = csvReader.readLine()) != null) {
            int value = Integer.parseInt(line.split(";")[1]);

            int time = Integer.parseInt(line.split(";")[0]);
            int day = time / 86400;

            if (value < -3700) {
                if (lastDayWithPowerGreater3000 != day) {
                    numberOfDaysWithPowerGreater3500++;
                    System.out.println("day: " + day + " " + numberOfDaysWithPowerGreater3500 + "rd!");
                }
                lastDayWithPowerGreater3000 = day;
            } else {
                if (time % 86400 == 0 && day % 7 == 0)
                    System.out.println(day + " Ätsch!");
            }

            longPowerInWs += value;
            intKWPeak = Math.min(intKWPeak, value);
            kennLinie[-1 * value]++;
        }

        csvReader.close();

        doublePowerInKWH = longPowerInWs / PhysicalConstants.factor_wsToKWh;

        System.out.println("Ws:  " + longPowerInWs);
        System.out.println("kWh: " + doublePowerInKWH);
        System.out.println("kWp: " + intKWPeak);

        System.out.println("numberOfDaysWithPowerGreater3500: " + numberOfDaysWithPowerGreater3500);

//		File outputFile = new File(outputFileName);
//		PrintWriter writer = new PrintWriter( new BufferedWriter( new FileWriter(outputFile) ) );
//		
//		for (int i = 0; i < kennlinie.length; i++) {
//			writer.println(i + ";" + kennlinie[i]);
//		}
//		
//		writer.close();


    }

}
