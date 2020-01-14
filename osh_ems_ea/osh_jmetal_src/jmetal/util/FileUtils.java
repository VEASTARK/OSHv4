package jmetal.util;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class FileUtils {
    static public void appendObjectToFile(String fileName, Object object) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName, true);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.write(object.toString());
            bw.newLine();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static public void createEmptyFile(String fileName) {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(fileName, false);
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
