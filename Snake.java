import Game.Board;
import Genetic.Algorithm.GeneticAlgorithm;
import Genetic.Algorithm.Genome;
import Neural.Net.NeuralNet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Snake extends JFrame {
    // The number of hidden layers in the NN
    private static final int NUM_HIDDENLAYERS = 1;
    // Neurons per hidden layer
    private static final int NEURONS_PER_H_LAYER = 6;
    // Probability of a mutation
    private static final double MUTATION_RATE = 0.5;
    // Defines how the child genomes are put together 0.7 = 70% Genome1 / 30% Genome2
    private static final double CROSS_RATE = 0.7;

    // If set to true, the game board will be shown while training (way slower but you can see what your NN learned already)
    private static final boolean VISIBLE = false;

    public static void main(String[] args) {
        GeneticAlgorithm genetics = null;
        try {

            // Read from disk using FileInputStream
            FileInputStream f_in = new
                    FileInputStream(NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data");

            // Read object using ObjectInputStream
            ObjectInputStream obj_in =
                    new ObjectInputStream(f_in);

            // Read an object
            Object obj = obj_in.readObject();

            if (obj instanceof GeneticAlgorithm) {
                // Cast object to a Vector
                genetics = (GeneticAlgorithm) obj;
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        List<NeuralNet> players = new ArrayList<NeuralNet>();
        for (int i = 0; i < 20; i++) {
            NeuralNet player = new NeuralNet(6, 4, NUM_HIDDENLAYERS, NEURONS_PER_H_LAYER);
            players.add(player);
        }

        if (genetics == null) {
            genetics = new GeneticAlgorithm(20, players.get(0).getNumberOfWeights(), MUTATION_RATE, CROSS_RATE);
        }


        List<Genome> population = genetics.getPopulation();

        double averageFitness = 0;
        double minAverage = Integer.MAX_VALUE;
        double bestFitness = genetics.getAverageFitness();

        for (int i = genetics.getGeneration(); averageFitness < minAverage; i++) {
            for (int j = 0; j < players.size(); j++) {
                players.get(j).putWeights(population.get(j).getWeights());
                Board game = new Board(players.get(j));
                new Snake(game, VISIBLE);
                population.get(j).setFitness(game.getFitness());
            }

            population = genetics.epoch(population, true);
            averageFitness = genetics.getAverageFitness();
            System.out.println("Generation " + i + ": Fitness: " + averageFitness + " Best: " + genetics.getBestFitness() + "\t Overall Best: " + bestFitness);
            if (averageFitness > bestFitness) {
                bestFitness = averageFitness;
                System.out.println("New Best Population!");
            }
            try {
                FileOutputStream f_out = new
                        FileOutputStream(NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data");
                ObjectOutputStream obj_out = new
                        ObjectOutputStream(f_out);
                obj_out.writeObject(genetics);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public Snake(Board game, boolean visible) {
        add(game);

        setResizable(false);
        pack();

        setTitle("Snake");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(visible);
        setAlwaysOnTop(true);
        while (!game.timerStopped()) {
            game.actionPerformed(new ActionEvent(this, 0, "continue"));
            if (visible) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        }
        setVisible(false);
        dispose();
    }
}
