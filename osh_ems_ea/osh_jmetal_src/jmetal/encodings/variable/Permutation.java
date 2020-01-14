//  Permutation.java
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

package jmetal.encodings.variable;

import jmetal.core.Variable;

/**
 * Class implementing a permutation of integer decision encodings.variable
 */
public class Permutation extends Variable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    /**
     * Stores a permutation of <code>int</code> values
     */
    public final int[] vector_;

    /**
     * Stores the length of the permutation
     */
    public final int size_;

    /**
     * Constructor
     */
    public Permutation() {
        this.size_ = 0;
        this.vector_ = null;

    } //Permutation

    /**
     * Constructor
     * @param size Length of the permutation
     */
  /*
  public Permutation(int size) {
      setVariableType(VariableType_.Permutation) ;

      size_   = size;
    vector_ = new int[size_];
    
    int [] randomSequence = new int[size_];
    
    for(int k = 0; k < size_; k++){
      int num           = PseudoRandom.randInt();
      randomSequence[k] = num;
      vector_[k]        = k;
    } 

    // sort value and store index as fragment order
    for(int i = 0; i < size_-1; i++){
      for(int j = i+1; j < size_; j++) {
        if(randomSequence[i] > randomSequence[j]){
          int temp          = randomSequence[i];
          randomSequence[i] = randomSequence[j];
          randomSequence[j] = temp;

          temp       = vector_[i];
          vector_[i] = vector_[j];
          vector_[j] = temp;
        }
      }
    }
  } //Permutation
   * */

    /**
     * Constructor
     *
     * @param size Length of the permutation
     *             This constructor has been contributed by Madan Sathe
     */
    public Permutation(int size) {
        this.size_ = size;
        this.vector_ = new int[this.size_];

        java.util.ArrayList<Integer> randomSequence = new
                java.util.ArrayList<>(this.size_);

        for (int i = 0; i < this.size_; i++)
            randomSequence.add(i);

        java.util.Collections.shuffle(randomSequence);

        for (int j = 0; j < randomSequence.size(); j++)
            this.vector_[j] = randomSequence.get(j);
    } // Constructor


    /**
     * Copy Constructor
     *
     * @param permutation The permutation to copy
     */
    public Permutation(Permutation permutation) {
        this.size_ = permutation.size_;
        this.vector_ = new int[this.size_];

        System.arraycopy(permutation.vector_, 0, this.vector_, 0, this.size_);
    } //Permutation


    /**
     * Create an exact copy of the <code>Permutation</code> object.
     *
     * @return An exact copy of the object.
     */
    @Override
    public Variable deepCopy() {
        return new Permutation(this);
    } //deepCopy

    /**
     * Returns the length of the permutation.
     *
     * @return The length
     */
    public int getLength() {
        return this.size_;
    } //getNumberOfBits

    /**
     * Returns a string representing the object
     *
     * @return The string
     */
    @Override
    public String toString() {
        StringBuilder string;

        string = new StringBuilder();
        for (int i = 0; i < this.size_; i++)
            string.append(this.vector_[i]).append(" ");

        return string.toString();
    } // toString
} // Permutation
