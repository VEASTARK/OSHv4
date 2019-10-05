package osh.toolbox.pv.eshl;

import java.io.*;

public class SplitFiles {
	
	static String inputFileName = "C:/pv/wiz_pv_all_asc.csv";

	static String delimeter = ";";
	
//	static int y2011 = 1293836400;
	
	static int year = 2011;
	
	static int y2012 = 1325372400;
	static int y2013 = 1356994800;
	
	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		
		String line = null;
		
		BufferedReader csvReader = new BufferedReader(new FileReader(new File(inputFileName)));
		
		File outputFile1 = new File("C:/pv/raw_2011.csv");
		PrintWriter p2011 = new PrintWriter( new BufferedWriter( new FileWriter(outputFile1) ) );
		
		File outputFile2 = new File("C:/pv/raw_2012.csv");
		PrintWriter p2012 = new PrintWriter( new BufferedWriter( new FileWriter(outputFile2) ) );
		
		
		while ((line = csvReader.readLine()) != null) {
			String replacedLine = line.replaceAll("\"", "");
			String[] splittedLine = replacedLine.split(";");
			int time = Integer.valueOf(splittedLine[0]);
			
			String newLine = time + delimeter + splittedLine[1];
			
			if (time < 1325372400) {
				p2011.println(newLine);
			}
			else {
				p2012.println(newLine);
			}
			
		}
		
		csvReader.close();
		p2011.close();
		p2012.close();
	}

	
}
