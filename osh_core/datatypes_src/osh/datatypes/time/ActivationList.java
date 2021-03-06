package osh.datatypes.time;

import osh.datatypes.ea.interfaces.ISolution;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ingo Mauser
 */
public class ActivationList implements ISolution {

    List<Activation> list;

    public ActivationList() {
        this.list = new ArrayList<>();
    }

    public List<Activation> getList() {
        return this.list;
    }

    public void setList(List<Activation> list) {
        this.list = list;
    }

}
