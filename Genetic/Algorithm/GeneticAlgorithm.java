package Genetic.Algorithm;

import Help.Helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GeneticAlgorithm implements Serializable {
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
        population = new ArrayList<>();

        for (int i = 0; i < populationSize; i++) {
            population.add(new Genome(new ArrayList<>(), 0));

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
        double newValue;
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

    public List<Genome> tournament() {
        List<Genome> winners = new ArrayList<>();
        List<Integer> freeIndexes = new ArrayList<>();
        int index1, index2;

        for (int i = 0; i < populationSize; i++) {
            freeIndexes.add(i);
        }

        while (winners.size() < populationSize / 2) {
            index1 = freeIndexes.get(Helper.randomInt(0, freeIndexes.size() - 1));
            freeIndexes.remove(new Integer(index1));

            if (freeIndexes.size() == 1) {
                index2 = freeIndexes.get(0);
            } else {
                index2 = freeIndexes.get(Helper.randomInt(0, freeIndexes.size() - 1));
            }
            freeIndexes.remove(new Integer(index2));

            if (population.get(index1).getFitness() > population.get(index2).getFitness()) {
                winners.add(population.get(index1));
            } else {
                winners.add(population.get(index2));
            }
        }

        return winners;
    }

    /**
     * Mix the weights of the parents to create their children
     *
     * @param mum Mother Genome
     * @param dad Father Genome
     * @return both childs
     */
    public List<Genome> crossover(Genome mum, Genome dad) {
        List<Genome> result = new ArrayList<>();
        Genome baby1, baby2;

        //Just return parents as offspring dependent on the rate or if parents are the same
//        if (Helper.randomDouble() > this.crossRate || mum.equals(dad)) {
//            result.add(mum);
//            result.add(dad);
//            return result;
//        }

        //determine a crossover point
        int crossPoint = Helper.randomInt(0, this.chromoLength);

        //create the offspring

        List<Double> mumWeights = new ArrayList<>(mum.getWeights());
        List<Double> dadWeights = new ArrayList<>(dad.getWeights());
        baby1 = new Genome(new ArrayList<>(), 0);
        baby2 = new Genome(new ArrayList<>(), 0);

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
        while (numCopies-- > 0) {
            for (int i = populationSize - 1; i > (populationSize - 1 - numBest); i--) {
                population.add(this.population.get(i));
            }
        }
        return population;
    }

    public List<Genome> epoch(List<Genome> oldPopulation, boolean tournament) {
        //create a temporary population to store new genomes
        List<Genome> newPopulation = new ArrayList<>();
        List<Genome> children, candidates = null;
        Genome mum, dad, baby1, baby2;
        int candidateCounter = 0;

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

        if (tournament) {
            candidates = tournament();
            candidateCounter = candidates.size() - 1;
        }

        //now we enter the GA loop
        //repeat until a new population is generated
        while (newPopulation.size() < this.populationSize) {
            if (tournament) {
                mum = candidates.get(candidateCounter);
                dad = candidates.get(candidateCounter - 1);
                if (candidateCounter <= 1) {
                    candidateCounter = candidates.size() - 1;
                } else {
                    candidateCounter--;
                }
            } else {
                //grab two genomes
                mum = genomeRoulette();
                dad = genomeRoulette();
            }
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
