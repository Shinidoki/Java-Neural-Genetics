package Neural.Net;

import Help.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Neuron implements Serializable {

    private int numInputs;
    private List<Double> weights;

    public Neuron(int numInputs) {
        this.numInputs = numInputs;
        weights = new ArrayList<>();
        for (int i = 0; i < numInputs + 1; i++) {
            weights.add(Helper.randomDouble());
        }
    }

    public int getNumInputs() {
        return numInputs;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public double getWeight(int index) {
        return weights.get(index);
    }

    public void setWeight(int index, double value) {
        weights.set(index, value);
    }
}
