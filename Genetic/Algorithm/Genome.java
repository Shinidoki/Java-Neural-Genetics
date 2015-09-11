package Genetic.Algorithm;

import java.io.Serializable;
import java.util.List;

public class Genome implements Comparable<Genome>, Serializable {

    private List<Double> weights;
    private double fitness;

    public Genome(List<Double> weights, double fitness) {
        this.weights = weights;
        this.fitness = fitness;
    }

    /**
     * Adds another weight to the genome.
     *
     * @param weight Weight that is added to the weights of the genome
     */
    public void addWeight(double weight) {
        this.weights.add(weight);
    }

    /**
     * Compare function for sorting
     *
     * @param other Genome to compare to
     * @return 0 if the fitness is the same, -1 if the other is fitter and 1 if this genome is fitter
     */
    public int compareTo(Genome other) {
        if (this.fitness == other.fitness) {
            return 0;
        }
        return (this.fitness < other.fitness) ? -1 : 1;
    }

    public List<Double> getWeights() {
        return weights;
    }

    public boolean equals(Object other) {
        return (other instanceof Genome) && this.weights.equals(((Genome) other).weights);
    }

    public void setWeight(int i, double value) {
        try {
            weights.get(i);
        } catch (IndexOutOfBoundsException e) {
            weights.add(i, value);
        }
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}
