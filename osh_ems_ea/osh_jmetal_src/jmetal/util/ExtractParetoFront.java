//  ExtractParetoFront.java
//
//  Author:
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2012 Juan J. Durillo
//
//  This program is free software: you can redistribute it and/or modify
//  it under the terms of the GNU Lesser General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU Lesser General Public License for more details.
// 
//  You should have received a copy of the GNU Lesser General Public License
//  along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jmetal.util;

import java.io.*;
import java.util.*;

/**
 * This class extract the Pareto front among a set of dominated and
 * non-dominated solutions
 */

public class ExtractParetoFront {

    final String fileName_;
    final int dimensions_;
    final List<Point> points_ = new LinkedList<>();

    /**
     * @param name: the name of the file
     * @author Juan J. Durillo
     * Creates a new instance
     */
    public ExtractParetoFront(String name, int dimensions) {
        this.fileName_ = name;
        this.dimensions_ = dimensions;
        this.loadInstance();
    } // ReadInstance

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Wrong number of arguments: ");
            System.out.println("Sintaxt: java ExtractParetoFront <file> <dimensions>");
            System.out.println("\t<file> is a file containing points");
            System.out.println("\t<dimensions> represents the number of dimensions of the problem");
            System.exit(-1);
        }

        ExtractParetoFront epf = new ExtractParetoFront(args[0], Integer.parseInt(args[1]));

        epf.writeParetoFront();
    }

    /**
     * Read the points instance from file
     */
    public void loadInstance() {

        try {
            File archivo = new File(this.fileName_);
            FileReader fr;
            BufferedReader br;
            fr = new FileReader(archivo);
            br = new BufferedReader(fr);

            // File reading
            String line;
            int lineCnt = 0;
            line = br.readLine(); // reading the first line (special case)

            while (line != null) {
                StringTokenizer st = new StringTokenizer(line);
                try {
                    Point auxPoint = new Point(this.dimensions_);
                    for (int i = 0; i < this.dimensions_; i++) {
                        auxPoint.vector_[i] = Double.parseDouble(st.nextToken());
                    }

                    this.add(auxPoint);

                    line = br.readLine();
                    lineCnt++;
                } catch (NumberFormatException e) {
                    System.err.println("Number in a wrong format in line " + lineCnt);
                    System.err.println(line);
                    line = br.readLine();
                    lineCnt++;
                } catch (NoSuchElementException e2) {
                    System.err.println("Line " + lineCnt + " does not have the right number of objectives");
                    System.err.println(line);
                    line = br.readLine();
                    lineCnt++;
                }
            }
            br.close();
        } catch (IOException e3) {
            System.err.println("The file " + this.fileName_ + " has not been found in your file system");
        }

    } // loadInstance


    public void add(Point point) {
        Iterator<Point> iterator = this.points_.iterator();


        while (iterator.hasNext()) {
            Point auxPoint = iterator.next();
            int flag = this.compare(point, auxPoint);

            if (flag == -1) {  // A solution in the list is dominated by the new one
                iterator.remove();

            } else if (flag == 1) { // The solution is dominated
                return;
            }
        } // while
        this.points_.add(point);

    } // add


    public int compare(Point one, Point two) {
        int flag1 = 0, flag2 = 0;
        for (int i = 0; i < this.dimensions_; i++) {
            if (one.vector_[i] < two.vector_[i])
                flag1 = 1;

            if (one.vector_[i] > two.vector_[i])
                flag2 = 1;
        }

        // one dominates
        return Integer.compare(flag2, flag1);

        // two dominates

        // both are non dominated
    }


    public void writeParetoFront() {
        try {
            /* Open the file */
            FileOutputStream fos = new FileOutputStream(this.fileName_ + ".pf");
            OutputStreamWriter osw = new OutputStreamWriter(fos);
            BufferedWriter bw = new BufferedWriter(osw);

            for (Point auxPoint : this.points_) {
                StringBuilder aux = new StringBuilder();

                for (int i = 0; i < auxPoint.vector_.length; i++) {
                    aux.append(auxPoint.vector_[i]).append(" ");

                }
                bw.write(aux.toString());
                bw.newLine();
            }

            /* Close the file */
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class Point {
        final double[] vector_;

        @SuppressWarnings("unused")
        public Point(double[] vector) {
            this.vector_ = vector;
        }

        public Point(int size) {
            this.vector_ = new double[size];
            for (int i = 0; i < size; i++)
                this.vector_[i] = 0.0f;
        }

    }
}
