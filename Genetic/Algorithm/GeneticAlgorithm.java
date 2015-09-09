package Genetic.Algorithm;

import Help.Helper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneticAlgorithm {
    private List<Genome> population;
    private int fittestGenome;
    private int populationSize;
    private int chromoLength;
    private int generation;
    private double mutationRate;
    private double crossRate;
    private double totalFitness;
    private double bestFitness;
    private double worstFitness;
    private double averageFitness;

    public GeneticAlgorithm(int populationSize, int chromoLength, double mutationRate, double crossRate) {
        this.populationSize = populationSize;
        this.chromoLength = chromoLength;
        this.mutationRate = mutationRate;
        this.crossRate = crossRate;
        population = new ArrayList<Genome>();

        for (int i = 0; i < populationSize; i++) {
            population.add(new Genome(new ArrayList<Double>(), 0));

            for (int j = 0; j < chromoLength; j++) {
                population.get(i).addWeight(Helper.randomClamped());
            }
        }
    }


    /**
     * mutates a genome by perturbing its weights by an amount not greater than MAX_PERTURBATION
     *
     * @param genome Genome to be mutated
     * @return mutated genome
     */
    public Genome mutate(Genome genome) {
        List<Double> weights = genome.getWeights();
        double newValue = 0;
        for (int i = 0; i < weights.size(); i++) {
            if (Helper.randomDouble() > this.mutationRate) {
                newValue = weights.get(i) + (Helper.randomClamped() * GeneSettings.MAX_PERTURBATION);
                genome.setWeight(i, newValue);
            }
        }
        return genome;
    }

    /**
     * returns a chromo based on roulette wheel sampling
     *
     * @return genome based on roulette wheel sampling
     */
    public Genome genomeRoulette() {
        //Generate a random number between 0 & total fitness count
        double slice = Helper.randomDouble() * this.totalFitness;

        //This will be the chosen Genome
        Genome chosenOne = null;

        //Go through the chromosomes adding up the fitness so far
        double fitnessSoFar = 0.0;
        boolean found = false;
        for (int i = 0; i < this.populationSize && !found; i++) {
            fitnessSoFar += this.population.get(i).getFitness();

            //if the fitness so far > random number return the chromo at this point
            if (fitnessSoFar >= slice) {
                chosenOne = this.population.get(i);
                found = true;
            }
        }

        //If the Roullette failed give at least some random genome
        if (!found) {
            chosenOne = this.population.get(Helper.randomInt(0, this.populationSize));
        }
        return chosenOne;
    }

    /**
     * Mix the weights of the parents to create their children
     *
     * @param mum Mother Genome
     * @param dad Father Genome
     * @return both childs
     */
    public List<Genome> crossover(Genome mum, Genome dad) {
        List<Genome> result = new ArrayList<Genome>();
        Genome baby1, baby2;

        //Just return parents as offspring dependent on the rate or if parents are the same
        if (Helper.randomDouble() > this.crossRate || mum.equals(dad)) {
            result.add(mum);
            result.add(dad);
            return result;
        }

        //determine a crossover point
        int crossPoint = Helper.randomInt(0, this.chromoLength);

        //create the offspring

        List<Double> mumWeights = mum.getWeights();
        List<Double> dadWeights = dad.getWeights();
        baby1 = new Genome(new ArrayList<Double>(), 0);
        baby2 = new Genome(new ArrayList<Double>(), 0);

        for (int i = 0; i < crossPoint; i++) {
            baby1.setWeight(i, mumWeights.get(i));
            baby2.setWeight(i, dadWeights.get(i));
        }
        for (int i = crossPoint; i < this.chromoLength; i++) {
            baby1.setWeight(i, dadWeights.get(i));
            baby2.setWeight(i, mumWeights.get(i));
        }

        result.add(baby1);
        result.add(baby2);
        return result;
    }

    public void reset() {
        this.totalFitness = 0;
        this.bestFitness = 0;
        this.worstFitness = Double.MAX_VALUE;
        this.averageFitness = 0;
    }

    /**
     * Updates the best, worst, average and total fitness
     */
    public void calculateStats() {
        this.totalFitness = 0;

        double currentMax = 0;
        double currentMin = Double.MAX_VALUE;
        double currentFitness;

        for (int i = 0; i < this.populationSize; i++) {
            currentFitness = this.population.get(i).getFitness();

            //update fittest if necessary
            if (currentFitness > currentMax) {
                currentMax = currentFitness;
                this.fittestGenome = i;
                this.bestFitness = currentFitness;
            }

            //update worst if necessary
            if (currentFitness < currentMin) {
                currentMin = currentFitness;
                this.worstFitness = currentMin;
            }

            this.totalFitness += currentFitness;
        }

        this.averageFitness = this.totalFitness / this.populationSize;
    }

    public List<Genome> grabNBest(int numBest, int numCopies, List<Genome> population) {
        while (numBest-- > 0) {
            for (int i = 0; i < numCopies; i++) {
                population.add(this.population.get(i));
            }
        }
        return population;
    }

    public List<Genome> epoch(List<Genome> oldPopulation) {
        //create a temporary population to store new genomes
        List<Genome> newPopulation = new ArrayList<Genome>();
        List<Genome> children;
        Genome mum, dad, baby1, baby2;

        //assign the given population to the classes population
        this.population = oldPopulation;
        //Reset current generation
        this.reset();

        //Sort the population (for scaling and elitism)
        Collections.sort(this.population);
        //calculate best, worst, average and total fitness
        this.calculateStats();

        //Now to add a little elitism we shall add in some copies of the fittest genomes. Make sure we add an EVEN number or the roulette wheel sampling will crash
        if ((GeneSettings.NUM_COPY_ELITES * GeneSettings.NUM_ELITES) % 2 == 0) {
            newPopulation = grabNBest(GeneSettings.NUM_ELITES, GeneSettings.NUM_COPY_ELITES, newPopulation);
        }

        //now we enter the GA loop
        //repeat until a new population is generated
        while (newPopulation.size() < this.populationSize) {
            //grab two genomes
            mum = genomeRoulette();
            dad = genomeRoulette();

            //create some offspring via crossover
            children = crossover(mum, dad);
            //now we mutate
            baby1 = mutate(children.get(0));
            baby2 = mutate(children.get(1));
            //now copy into new population
            newPopulation.add(baby1);
            newPopulation.add(baby2);
        }

        //finnished so assign new population back into class Population
        this.population = newPopulation;
        this.generation++;
        return this.population;
    }

    public Genome getBestGenome() {
        return this.population.get(this.fittestGenome);
    }

    public List<Genome> getPopulation() {
        return population;
    }

    public double getAverageFitness() {
        return averageFitness;
    }

    public double getWorstFitness() {
        return worstFitness;
    }

    public double getBestFitness() {
        return bestFitness;
    }

    public int getGeneration() {
        return generation;
    }
}
