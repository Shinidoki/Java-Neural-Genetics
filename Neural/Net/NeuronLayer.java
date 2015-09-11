package Neural.Net;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NeuronLayer implements Serializable {

    private int numNeurons;
    private List<Neuron> neurons;

    public NeuronLayer(int numNeurons, int inputsPerNeuron) {
        this.numNeurons = numNeurons;
        neurons = new ArrayList<>();

        for (int i = 0; i < numNeurons; i++) {
            neurons.add(new Neuron(inputsPerNeuron));
        }
    }

    public int getNumNeurons() {
        return numNeurons;
    }

    public List<Neuron> getNeurons() {
        return neurons;
    }

    public Neuron getNeuron(int index) {
        return neurons.get(index);
    }
}
