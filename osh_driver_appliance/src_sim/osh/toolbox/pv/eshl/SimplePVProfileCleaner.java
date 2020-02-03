package osh.toolbox.pv.eshl;

import osh.utils.csv.CSVImporter;
import osh.utils.time.TimeConversion;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.time.ZonedDateTime;


/**
 * IMPORTANT: first day of input file must be completely available and valid!
 *
 * @author Ingo Mauser
 */
public class SimplePVProfileCleaner {

    // ### configuration ###

    static final String inputFileName = "C:/pv/raw_3_20112012_0.csv";

    static final String outputFileName = "C:/pv/cleaned_20112012_";
    static final String outputFileExtension = ".csv";

    static final String delimiter = ";";

    static int columnTimestamp;
    static int columnActivePower = 1;
    static int columnReactivePower = -1;

    static boolean outputFileHasHeader;

    static final boolean inputPowerIsNegative = true;
    static final boolean outputPowerIsNegative = true;

    static int timeStep = 1; // in seconds
    static final int days = 365;

    static int inputFirstTimestamp; // first second of year
    static int outputFirstTimestamp;

    static final int firstHourWithSun = 5;
    static final int lastHourWithSun = 21;

    // ### for later usage ###

    static int[] lastDayPower;
    static int[] currentDayPower;

    static int[][] inputArray;
    static Integer[][][] outputArray;

    static int[] biggestGapAtDay;

    /**
     * @param args
     */
    public static void main(String[] args) {

        try {

            lastDayPower = new int[86400];
            currentDayPower = new int[86400];

            inputArray = loadProfile(inputFileName);
            outputArray = new Integer[86400][days][2];

            biggestGapAtDay = new int[days];

            int currentDay;
            int currentSecondOfDay;

            System.out.println("START: fill existing data");
            // fill existing data
            for (int[] ints : inputArray) {
                ZonedDateTime currentInputTime = TimeConversion.convertUnixTimeToZonedDateTime(ints[0]);
                currentDay = TimeConversion.getCorrectedDayOfYear(currentInputTime);
                currentSecondOfDay = (int) TimeConversion.getSecondsSinceDayStart(currentInputTime);
                outputArray[currentSecondOfDay][currentDay][0] =
                        outputFirstTimestamp + currentDay * 86400 + currentSecondOfDay;
                outputArray[currentSecondOfDay][currentDay][1] = ints[1];
            }
            System.out.println("END: fill existing data");

            inputArray = null;

            System.out.println("START: fill new array with timestamps (if no already filled in)");
            // fill new array with timestamps (if no already filled in)
            for (int i = 0; i < days; i++) {
                for (int j = 0; j < 86400; j++) {
                    if (outputArray[j][i][0] == null) {
                        outputArray[j][i][0] = i * 86400 + j;
                    }
                }
            }
            System.out.println("END: fill new array with timestamps (if no already filled in)");

            System.out.println("START: set values bigger 0 to 0 or v.v.");
            // set values bigger 0 to 0 or v.v.
            for (int i = 0; i < days; i++) {
                for (int j = 0; j < 86400; j++) {
                    if (outputArray[j][i][1] != null) {
                        if (inputPowerIsNegative) {
                            if (outputArray[j][i][1] > 0) {
//								outputArray[j][i][1] = 0;
                                outputArray[j][i][1] = null;
                            }
                        } else {
                            if (outputArray[j][i][1] < 0) {
//								outputArray[j][i][1] = 0;
                                outputArray[j][i][1] = null;
                            }
                        }
                    }
                }
            }
            System.out.println("END: set values bigger 0 to 0 or v.v.");

//			// at midnight : always no power
//			for (int i = 0; i < days; i++) {
//				outputArray[0][i][1] = 0;
//				outputArray[86400 - 1][i][1] = 0;
//			}

            System.out.println("START: set time without pv power to 0");
            // set time without pv power to 0
            for (int i = 0; i < days; i++) {
                for (int j = 0; j < firstHourWithSun; j++) {
                    outputArray[j][i][1] = 0;
                }
                for (int j = lastHourWithSun * 3600; j < 86400; j++) {
                    outputArray[j][i][1] = 0;
                }
            }
            System.out.println("END: set time without pv power to 0");

            System.out.println("START: calculate max gaps");
            // calculate max gaps
            for (int i = 0; i < days; i++) {
                System.out.println("START: calculate max gaps. day: " + i);
                int counter = 0;
                for (int j = 0; j < 86400; j++) {
                    if (outputArray[j][i][1] != null) {
                        counter = 0;
                    } else {
                        counter++;
                        biggestGapAtDay[i] = Math.max(counter, biggestGapAtDay[i]);
                    }
                }
            }
            System.out.println("END: calculate max gaps");

            System.out.println("START: data cleansing");
            // data cleansing
            for (int i = 0; i < days; i++) {
                System.out.println("START: data cleansing. day: " + i);
                // interpolate if max gap is < 1 * 3600 seconds
                if (biggestGapAtDay[i] < 3600) {

//					boolean gapLeft = false;
//					for (int j = 0; j < 86400; j++) {
//						if (outputArray[j][i][1] == null) {
//							gapLeft = true;
//							break;
//						}
//					}

//					while (gapLeft) {

//						// get first null value
//						int firstNull = 86400;
//						for (int j = 0; j < 86400; j++) {
//							if (outputArray[j][i][1] == null) {
//								firstNull = j;
//								break;
//							}
//						}

                    // get first null value

                    int firstNull = 86400;
                    for (int j = 0; j < 86400; j++) {
                        if (outputArray[j][i][1] == null) {
                            firstNull = j;
                            break;
                        }
                    }

                    while (firstNull < 86400) {

                        // get first number after null value
                        int firstNotNullAfterNull = 86400;
                        for (int j = firstNull; j < 86400; j++) {
                            if (outputArray[j][i][1] != null) {
                                firstNotNullAfterNull = j;
                                break;
                            }
                        }

                        int divisor = firstNotNullAfterNull - firstNull + 1;

                        // interpolate
                        for (int j = firstNull; j < firstNotNullAfterNull; j++) {
                            outputArray[j][i][1] =
                                    (outputArray[firstNull - 1][i][1] * (firstNotNullAfterNull - j)
                                            + outputArray[firstNotNullAfterNull][i][1] * (j - firstNull + 1))
                                            / divisor;
                        }

//						gapLeft = false;
//						// check if gap left
//						for (int j = firstNotNullAfterNull; j < 86400; j++) {
//							if (outputArray[j][i][1] == null) {
//								gapLeft = true;
//								break;
//							}
//						}

                        // get first null value
                        firstNull = 86400;
                        for (int j = firstNotNullAfterNull; j < 86400; j++) {
                            if (outputArray[j][i][1] == null) {
                                firstNull = j;
                                break;
                            }
                        }

                    }

                } else {
                    // use previous day
                    for (int j = 0; j < 86400; j++) {
                        outputArray[j][i][1] = outputArray[j][i - 1][1];
                    }
                }
            }

            // reverse sign of power value if necessary
            if (inputPowerIsNegative != outputPowerIsNegative) {
                for (int i = 0; i < days; i++) {
                    for (int j = 0; j < 86400; j++) {
                        outputArray[j][i][1] = (-1) * outputArray[j][i][1];
                    }
                }
            }

            // data output
            for (int i = 0; i < days; i++) {
                File outputFile = new File(outputFileName + i + outputFileExtension);
                PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));

                for (int j = 0; j < 86400; j++) {
//					String line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
                    String line;

                    if (j > 10000 && j < 45000) {
                        line = "" + outputArray[j][i][0] + delimiter + outputArray[45000 + (45000 - j)][i][1];
                    } else {
                        line = "" + outputArray[j][i][0] + delimiter + outputArray[j][i][1];
                    }

                    writer.println(line);
                }

                writer.close();
            }

            // full
            File outputFile = new File(outputFileName + "full" + outputFileExtension);
            PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            for (int i = 0; i < days; i++) {
                for (int j = 0; j < 86400; j++) {
                    String line;
//					line = "" + outputArray[j][i][0] + delimeter + outputArray[j][i][1];
                    if (j > 10000 && j < 45000) {
                        line = "" + outputArray[j][i][0] + delimiter + outputArray[45000 + (45000 - j)][i][1];
                    } else {
                        line = "" + outputArray[j][i][0] + delimiter + outputArray[j][i][1];
                    }
                    writer.println(line);
                }
            }
            writer.close();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private static int[][] loadProfile(String fileName) {
        return CSVImporter.readInteger2DimArrayFromFile(fileName, delimiter, "\"");
    }

}
