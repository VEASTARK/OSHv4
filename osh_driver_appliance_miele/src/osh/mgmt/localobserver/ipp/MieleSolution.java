package osh.mgmt.localobserver.ipp;

import osh.datatypes.ea.interfaces.ISolution;


/**
 * @author Florian Allerding, Till Schuberth
 */

public class MieleSolution implements ISolution {

    public long startTime;
    public boolean isPredicted;


    /**
     * for JAXB
     */
    @SuppressWarnings("unused")
    @Deprecated
    private MieleSolution() {
    }

    /**
     * CONSTRUCTOR
     *
     * @param startTime
     * @param isPredicted
     */
    public MieleSolution(long startTime, boolean isPredicted) {
        super();

        this.startTime = startTime;
        this.isPredicted = isPredicted;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.isPredicted ? 1231 : 1237);
        result = prime * result + (int) (this.startTime ^ (this.startTime >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (this.getClass() != obj.getClass())
            return false;
        MieleSolution other = (MieleSolution) obj;
        if (this.isPredicted != other.isPredicted)
            return false;
        return this.startTime == other.startTime;
    }

    @Override
    public MieleSolution clone() {
        return new MieleSolution(this.startTime, this.isPredicted);
    }

}