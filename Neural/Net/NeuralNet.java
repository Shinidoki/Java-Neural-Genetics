package Neural.Net;

import Help.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class NeuralNet implements Serializable {

    private int numInputs;
    private List<NeuronLayer> layers;

    public NeuralNet(int numInputs, int numOutputs, int numHiddenLayers, int neuronsPerHiddenLayer) {
        this.numInputs = numInputs;
        layers = new ArrayList<>();

        if (numHiddenLayers > 0) {
            layers.add(new NeuronLayer(neuronsPerHiddenLayer, numInputs));

            for (int i = 0; i < numHiddenLayers; i++) {
                layers.add(new NeuronLayer(neuronsPerHiddenLayer, neuronsPerHiddenLayer));
            }

            layers.add(new NeuronLayer(numOutputs, numInputs));
        }
    }

    /**
     * @return an array with the weights
     */
    public List<List<Double>> getWeights() {
        List<List<Double>> weights = new ArrayList<>();

        for (NeuronLayer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                weights.add(neuron.getWeights());
            }
        }

        return weights;
    }

    /**
     * Replaces the weights of the net with the new values
     *
     * @param weights new Weights for the net
     */
    public void putWeights(List<Double> weights) {
        int weightIndex = 0;

        for (NeuronLayer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                List<Double> neuronWeights = neuron.getWeights();
                for (int i = 0; i < neuronWeights.size(); i++) {
                    neuron.setWeight(i, weights.get(weightIndex));
                    weightIndex++;
                }
            }
        }
    }

    /**
     * Returns the total number of weights needed for the net
     *
     * @return amount of weights in the net
     */
    public int getNumberOfWeights() {
        int weights = 0;

        for (NeuronLayer layer : layers) {
            for (Neuron neuron : layer.getNeurons()) {
                weights += neuron.getWeights().size();
            }
        }

        return weights;
    }

    /**
     * Calculates the output values for an input
     *
     * @param inputs inputs for the net
     * @return output of the net
     */
    public List<Double> update(List<Double> inputs) {
        List<Double> outputs = new ArrayList<>();
        double netInput;
        int neuronInputs;
        if (inputs.size() != numInputs) {
            return outputs;
        }

        for (int i = 0; i < layers.size(); i++) {
            if (i > 0) {
                Collections.copy(inputs, outputs);
            }
            outputs.clear();

            for (Neuron neuron : layers.get(i).getNeurons()) {
                netInput = 0;
                neuronInputs = neuron.getNumInputs();

                for (int j = 0; j < neuron.getWeights().size(); j++) {
                    if (j < neuronInputs) {
                        netInput += neuron.getWeight(j) * inputs.get(j);
                    }
                }

                netInput += neuron.getWeight(neuronInputs - 1) * NetSettings.BIAS;
                outputs.add(Helper.sigmoid(netInput, NetSettings.ACTIVATION_RESPONSE));
            }
        }

        return outputs;
    }
}
