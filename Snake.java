import Game.Board;
import Genetic.Algorithm.GeneticAlgorithm;
import Genetic.Algorithm.Genome;
import Neural.Net.NeuralNet;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

public class Snake extends JFrame {
    // The number of hidden layers in the NN
    private static final int NUM_HIDDENLAYERS = 2;
    // Neurons per hidden layer
    private static final int NEURONS_PER_H_LAYER = 8;
    // Probability of a mutation
    private static final double MUTATION_RATE = 0.5;
    // Defines how the child genomes are put together 0.7 = 70% Genome1 / 30% Genome2
    private static final double CROSS_RATE = 0.7;
    // How many snakes per generation
    private static final int POPULATION_SIZE = 20;

    // If set to true, the game board will be shown while training (way slower but you can see what your NN learned already)
    private static final boolean VISIBLE = false;
    // If set to true, the game board will be shown while training (way slower but you can see what your NN learned already)
    private static final int MAX_ACTIONS = 500;

    public static void main(String[] args) {
        GeneticAlgorithm genetics = null;
        try {

            // Read from disk using FileInputStream
            FileInputStream f_in = new
                    FileInputStream(POPULATION_SIZE + " Pop " + NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data");

            // Read object using ObjectInputStream
            ObjectInputStream obj_in =
                    new ObjectInputStream(f_in);

            // Read an object
            Object obj = obj_in.readObject();

            if (obj instanceof GeneticAlgorithm) {
                // Cast object to a Vector
                genetics = (GeneticAlgorithm) obj;
            }
            f_in.close();
            obj_in.close();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }


        List<NeuralNet> players = new ArrayList<NeuralNet>();
        for (int i = 0; i < POPULATION_SIZE; i++) {
            NeuralNet player = new NeuralNet(5, 4, NUM_HIDDENLAYERS, NEURONS_PER_H_LAYER);
            players.add(player);
        }

        if (genetics == null) {
            genetics = new GeneticAlgorithm(POPULATION_SIZE, players.get(0).getNumberOfWeights(), MUTATION_RATE, CROSS_RATE);
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

            population = genetics.epoch(population, false);
            averageFitness = genetics.getAverageFitness();
            System.out.println("Generation " + i + ": Fitness: " + averageFitness + " Best: " + genetics.getBestFitness() + "\t Overall Best: " + bestFitness);
            if (averageFitness > bestFitness) {
                bestFitness = averageFitness;
                System.out.println("New Best Population!");
            }
            try {

                FileOutputStream f_out = new
                        FileOutputStream(POPULATION_SIZE + " Pop " + NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data.new");
                ObjectOutputStream obj_out = new
                        ObjectOutputStream(f_out);
                obj_out.writeObject(genetics);
                f_out.close();
                obj_out.close();
                File newfile = new File(POPULATION_SIZE + " Pop " + NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data.new");
                Path newfilePath = newfile.toPath();
                File oldfile = new File(POPULATION_SIZE + " Pop " + NUM_HIDDENLAYERS + "x" + NEURONS_PER_H_LAYER + ".data");
                Path oldfilePath = oldfile.toPath();
                Files.move(newfilePath, oldfilePath, StandardCopyOption.REPLACE_EXISTING);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }


    public Snake(Board game, boolean visible) {
        int actionCounter = 0;
        add(game);

        setResizable(false);
        pack();

        setTitle("Snake");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setVisible(visible);
        setAlwaysOnTop(true);
        while (actionCounter < MAX_ACTIONS && !game.timerStopped()) {
            game.actionPerformed(new ActionEvent(this, 0, "continue"));
            if (visible) {
                try {
                    Thread.sleep(100);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            actionCounter++;
        }
        setVisible(false);
        dispose();
    }
}
