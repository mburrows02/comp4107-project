package edu.carleton.comp4107.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.Population;
import org.jgap.impl.*;

public class SudokuSolver {
	private static int POP_SIZE = 500;
	private static int MAX_GEN = 10000;
	private static int STUCK_THRESHOLD = 200;
	private static int MAX_TRIES = 10;
	private static int TOURNAMENT_SIZE = 5;
	private static double TOURNAMENT_PROB = 0.4;
	private static double XOVER_RATE = 0.9;
	private static int XOVER_TYPE = PermutationCrossover.PMX;
	static final String DATA_ERR = "Invalid data format: ";
	static final String SIZE_ERR = "Grid size is not a square number";
	static final String ROW_LEN_ERR = "Mismatched length at row ";
	static final String NUM_ROWS_ERR =
			"Number does not match length of rows";

	/**
	 * @param args
	 * --puzzle <filename> the puzzle file to load
	 * --pop-size <value> the population size
	 * --stuck <value> the number of generations to keep trying without improvement in max fitness before giving up
	 * --tries <value> the number of times to try to solve the puzzle
	 * --tourn-size <value> the tournament size to use for selection
	 * --tourn-prob <value> the probability of selecting the tournament winner
	 * --crossover-rate <value> the crossover rate
	 * --order-one use order one crossover
	 * --pmx use partially mapped crossover (default)
	 * --cycle use cycle crossover
	 * --mutation <value> the mutation rate
	 * @throws IOException If the specified file can't be read
	 * @throws InvalidConfigurationException 
	 */
	public static void main(String[] args) 
			throws IOException, 
	               InvalidConfigurationException {
		String sep = System.getProperty("file.separator");
		String puzzleFilename = "data" + sep + "nine" + sep + "medium1.txt";
		
		for (int i = 0; i < args.length; ++i) {
			if (args[i].equals("--puzzle")) {
				puzzleFilename = args[++i];
			} else if (args[i].equals("--pop-size")) {
				POP_SIZE = Integer.parseInt(args[++i]);
			} else if (args[i].equals("--stuck")) {
				STUCK_THRESHOLD = Integer.parseInt(args[++i]);
			} else if (args[i].equals("--tries")) {
				MAX_TRIES = Integer.parseInt(args[++i]);
			} else if (args[i].equals("--tourn-size")) {
				TOURNAMENT_SIZE = Integer.parseInt(args[++i]);
			} else if (args[i].equals("--tourn-prob")) {
				TOURNAMENT_PROB = Double.parseDouble(args[++i]);
			} else if (args[i].equals("--crossover")) {
				XOVER_RATE = Double.parseDouble(args[++i]);
			} else if (args[i].equals("--mutation")) {
				SwapMutation.MUTATION_RATE = Double.parseDouble(args[++i]);
			} else if (args[i].equals("--order-one")) {
				XOVER_TYPE = PermutationCrossover.ORDER_ONE;
			} else if (args[i].equals("--pmx")) {
				XOVER_TYPE = PermutationCrossover.PMX;
			} else if (args[i].equals("--cycle")) {
				XOVER_TYPE = PermutationCrossover.CYCLE;
			} else {
				System.out.println("Error: Invalid argument supplied.");
				System.exit(-1);
			}
		}
		
		int[][] puzzle = SudokuUtils.loadPuzzle(puzzleFilename);
		
		Configuration conf = new DefaultConfiguration();
		conf.getGeneticOperators().clear();
		//Crossover
		conf.addGeneticOperator(new PermutationCrossover(conf, XOVER_RATE, XOVER_TYPE));
		//Mutation
		conf.addGeneticOperator(new SwapMutation());
		//Selection
		TournamentSelector tourney = new TournamentSelector(conf, TOURNAMENT_SIZE, TOURNAMENT_PROB);
		conf.addNaturalSelector(tourney, false);
		conf.setFitnessFunction(new SudokuFitnessFunction(puzzle));
		conf.setPopulationSize(POP_SIZE);
		Gene[] sampleGene = new Gene[puzzle.length];

		for (int i = 0; i < puzzle.length; ++i) {
			boolean[] seen = new boolean[puzzle.length];
			for (int j = 0; j < puzzle.length; ++j) {
				int num = puzzle[i][j];
				if (num != SudokuUtils.BLANK) seen[num-1] = true;
			}

			CompositeGene gene = new CompositeGene(conf);
			for (int j = 0; j < puzzle.length; ++j) {
				if (!seen[j]) {
					IntegerGene num = new IntegerGene(conf, 1, puzzle.length);
					gene.addGene(num);
				}
			}
			sampleGene[i] = gene;
		}

		Chromosome sampleChromosome = new Chromosome(conf, sampleGene);
		conf.setSampleChromosome(sampleChromosome);
		int maxFitness = puzzle.length*puzzle.length*2;
		List<Integer> bestFitPerTry = new ArrayList<Integer>();
		ArrayList<Long> times = new ArrayList<Long>();
		ArrayList<Integer> generations = new ArrayList<Integer>();
		for (int t = 0; t < MAX_TRIES; ++t) {
			Genotype population = new Genotype(conf, generatePopulation(conf, puzzle));
			int gen;
			IChromosome bestSolution = null;

			int stuckCount = 0;
			int oldBestFit = 0;
			long startTime = System.nanoTime();
			for (gen = 0; gen < MAX_GEN; ++gen) {
				bestSolution = population.getFittestChromosome();
				int bestFit = (int) bestSolution.getFitnessValue();
				if (bestFit <= oldBestFit) {
					if (++stuckCount >= STUCK_THRESHOLD) {
						bestFitPerTry.add(bestFit);
						break;
					}
				} else {
					System.out.println("Try " + t + "; Generation " + gen +
							"; Best solution " + bestFit + "/" + maxFitness +
							"; Stuck for " + stuckCount + " generations");
					System.out.println("new best: ");
					SudokuUtils.printPuzzle(SudokuUtils.formatPuzzle(bestSolution, puzzle));
					stuckCount = 0;
					oldBestFit = bestFit;
				}
				if (bestFit == maxFitness) {
					long time = System.nanoTime() - startTime;
					times.add(time / 1000000); // milli-seconds
					generations.add(gen);
					System.out.println("Done: " + bestFit + "/" + maxFitness);
					System.out.println("Time: "+time);
					break;
				}
				population.evolve();
			}
			if (bestSolution.getFitnessValue() == maxFitness) {
				System.out.println();
				int[][] solved = SudokuUtils.formatPuzzle(bestSolution, puzzle);
				SudokuUtils.printPuzzle(solved);
			} else {
				System.out.println("Attempt aborted; score stuck for too long");
			}
		}
		System.out.println("---------------------------");
		System.out.println("All attempts completed");
		int numSuccess = times.size();

		System.out.println("Successful attempts: " + numSuccess +"/" + MAX_TRIES);

		long totalTime = 0;
		long totalSquaredTime = 0;
		int totalGens = 0;
		int totalSquaredGens = 0;
		for (long time: times) totalTime += time;
		for (long time: times) totalSquaredTime += time * time;
		for (int gen: generations) totalGens += gen;
		for (int gen: generations) totalSquaredGens += gen * gen;
		if (numSuccess > 0) {
			long avgTime = totalTime / numSuccess;
			int avgGen = totalGens / numSuccess;
			long timeStdDev = (long)Math.sqrt(totalSquaredTime / numSuccess - avgTime * avgTime);
			int genStdDev = (int)Math.sqrt(totalSquaredGens / numSuccess - avgGen * avgGen);
	
			System.out.println("Successful attempt stats:");
			System.out.println("Average time: " + avgTime + "ms +/- " + timeStdDev);
			System.out.println("Average generation: " + avgGen + " +/- " + genStdDev);
			System.out.println("");
		}
		System.out.println("Failed attempt stats:");
		double avgBestFit = 0;
		int maxBestFit = 0;
		for (Integer i : bestFitPerTry) {
			avgBestFit += i;
			if (i > maxBestFit) {
				maxBestFit = i;
			}
		}
		avgBestFit /= bestFitPerTry.size();
		System.out.println("Average best fitness reached per attempt: " + avgBestFit);
		System.out.println("Best fitness reached over all attempts: " + maxBestFit);

	}
	
	/**
	 * Count the number of empty spaces in a puzzle; i.e., the number of
	 * 	spaces not pre-filled
	 * @param puzzle
	 * @return
	 */
	private static int countPrefilledSpaces(int[][] puzzle) {
		int count = 0;
		for (int i = 0; i < puzzle.length; ++i) {
			for (int j = 0; j < puzzle.length; ++j) {
				if (puzzle[i][j] != SudokuUtils.BLANK) {
					++count;
				}
			}
		}
		return count;
	}
	
	private static Population generatePopulation(Configuration conf, int[][] puzzle)
			throws InvalidConfigurationException {

		Population pop = new Population(conf);

		ArrayList<IntegerGene>[] genes = new ArrayList[puzzle.length];
		for (int i = 0; i < puzzle.length; ++i) {
			boolean[] seen = new boolean[puzzle.length];
			for (int j = 0; j < puzzle.length; ++j) {
				int num = puzzle[i][j];
				if (num != SudokuUtils.BLANK) {
					seen[num - 1] = true;
				}
			}

			genes[i] = new ArrayList<IntegerGene>();

			for (int j = 0; j < puzzle.length; ++j) {
				if (!seen[j]) {
					IntegerGene num = new IntegerGene(conf);
					num.setAllele(new Integer(j + 1));
					genes[i].add(num);
				}
			}
		}

		Random rand = new Random();
		for (int i = 0; i < POP_SIZE; ++i) {
			CompositeGene[] randGenes = new CompositeGene[puzzle.length];
			for (int j = 0; j < puzzle.length; ++j) {
				randGenes[j] = new CompositeGene(conf);
				Collections.shuffle(genes[j], rand);
				for (IntegerGene gene: genes[j]) {
					randGenes[j].addGene(gene);
				}
			}
			pop.addChromosome(new Chromosome(conf, randGenes));
		}
		return pop;
	}
}
