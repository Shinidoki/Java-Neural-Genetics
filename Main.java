import Genetic.Algorithm.GeneticAlgorithm;
import Neural.Net.NeuralNet;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Main {
    public static int numHiddenLayers = 3;
    public static int neuronsPerHiddenLayer = 3;

    public static void main(String[] args) {
        NeuralNet net = new NeuralNet(9, 2, numHiddenLayers, neuronsPerHiddenLayer);
//        System.out.println(net.getWeights());

        GeneticAlgorithm algo = new GeneticAlgorithm(20, 10, 0.2, 0.3);
//        System.out.println(algo.getPopulation());

    }
}
