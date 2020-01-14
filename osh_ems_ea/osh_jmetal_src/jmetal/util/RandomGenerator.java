//  RandomGenerator.java
//
//  Author:
//       Antonio J. Nebro <antonio@lcc.uma.es>
//       Juan J. Durillo <durillo@lcc.uma.es>
//
//  Copyright (c) 2011 Antonio J. Nebro, Juan J. Durillo
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

import java.util.Random;

/**
 * This code has been taken from deb NSGA-II implementation
 * The code is available to download from
 * http://www.iitk.ac.in/kangal/codes.shtml
 */

public class RandomGenerator implements IRandomGenerator {

    /* Definition of random number generation routines */
    final double seed;
    final double[] oldRand = new double[55];
    int jRand;

    /**
     * Constructor
     */
    public RandomGenerator() {
        this.seed = (new Random(System.nanoTime())).nextDouble();
        this.randomize();
    } // RandomGenerator

    /* Get seed number for random and start it up */
    synchronized void randomize() {
        int j1;
        for (j1 = 0; j1 <= 54; j1++) {
            this.oldRand[j1] = 0.0;
        }
        this.jRand = 0;
        this.warmup_random(this.seed);
    } // randomize

    /* Get randomize off and running */
    synchronized void warmup_random(double seed) {
        int j1, ii;
        double new_random, prev_random;
        this.oldRand[54] = seed;
        new_random = 0.000000001;
        prev_random = seed;
        for (j1 = 1; j1 <= 54; j1++) {
            ii = (21 * j1) % 54;
            this.oldRand[ii] = new_random;
            new_random = prev_random - new_random;
            if (new_random < 0.0) {
                new_random += 1.0;
            }
            prev_random = this.oldRand[ii];
        }
        this.advance_random();
        this.advance_random();
        this.advance_random();
        this.jRand = 0;
    } // warmup_random

    /* Create next batch of 55 random numbers */
    synchronized void advance_random() {
        int j1;
        double new_random;

        for (j1 = 0; j1 < 24; j1++) {
            new_random = this.oldRand[j1] - this.oldRand[j1 + 31];
            if (new_random < 0.0) {
                new_random += 1.0;
            }
            this.oldRand[j1] = new_random;
        }
        for (j1 = 24; j1 < 55; j1++) {
            new_random = this.oldRand[j1] - this.oldRand[j1 - 24];
            if (new_random < 0.0) {
                new_random += 1.0;
            }
            this.oldRand[j1] = new_random;
        }
    } //advance_ramdom

    /* Fetch a single random number between 0.0 and 1.0 */
    synchronized double randomPercent() {
        this.jRand++;
        if (this.jRand >= 55) {
            this.jRand = 1;
            this.advance_random();
        }
        return this.oldRand[this.jRand];
    } //randomPerc

    /* Fetch a single integer between 0 and upperbound */
    public int nextInt(int upperBound) {
        return this.rndInt(0, upperBound);
    }

    /* Fetch a single double between 0.0 and 1.0 */
    public double nextDouble() {
        return this.randomPercent();
    }

    /* Fetch a single random integer between low and high including the bounds */
    public int rndInt(int low, int high) {
        int res;
        if (low >= high) {
            res = low;
        } else {
            res = low + (int) (this.randomPercent() * (high - low + 1));
            if (res > high) {
                res = high;
            }
        }
        return (res);
    } // rnd

    /* Fetch a single random real number between low and high including the */
    /* bounds */
    synchronized public double rndReal(double low, double high) {
        return (low + (high - low) * this.randomPercent());
    } //rndreal
} // RandomGenerator
