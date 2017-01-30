package edu.carleton.comp4107.project;

import java.util.*;

import org.jgap.*;
import org.jgap.impl.CompositeGene;
import org.jgap.impl.CrossoverOperator;

public class PermutationCrossover extends CrossoverOperator 
		implements GeneticOperator {
	public static final int ORDER_ONE = 0;
	public static final int PMX = 1;
	public static final int CYCLE = 2;
	private final double crossoverRate;
	private final int crossoverType;
	
	public PermutationCrossover(final Configuration config, double crossoverRate, int crossoverType) throws InvalidConfigurationException {
		super(config);
		this.crossoverRate = crossoverRate;
		this.crossoverType = crossoverType;
	}

	@Override
	public void operate(Population pop, List candidates) {
		int numCrossovers = (int)(pop.size()*crossoverRate);
		Random rand = new Random();
		for (int i = 0; i < numCrossovers; ++i) {
			int index1, index2;
			IChromosome chrom1, chrom2;
			/*Keep picking random chromosomes until we get two different ones
			 * that aren't brand new */
			do {
				index1 = rand.nextInt(pop.size());
				index2 = rand.nextInt(pop.size());
				chrom1 = pop.getChromosome(index1);
				chrom2 = pop.getChromosome(index2);
			} while (chrom1 == chrom2/* || chrom1.getAge() < 1 || 
					chrom2.getAge() < 1*/);
			
			IChromosome parent1 = (IChromosome) chrom1.clone();
			IChromosome parent2 = (IChromosome) chrom2.clone();

			try {
				doCrossover(parent1, parent2, candidates, rand);
				//doSimpleCrossover(parent1, parent2, candidates, rand);
			} catch(InvalidConfigurationException e) {} // NOPE
		}
	}
	
	/**
	 * Perform a crossover between two chromosomes by performing a crossover between each
	 * corresponding pair of genes.
	 * @param chrom1
	 * @param chrom2
	 * @param candidates
	 * @param rand
	 */
	private void doCrossover(IChromosome chrom1, IChromosome chrom2, List candidates, Random rand)
			throws InvalidConfigurationException {
		//for each composite gene, crossover the sub-genes
		CompositeGene[] genes1 = new CompositeGene[chrom1.size()];
		CompositeGene[] genes2 = new CompositeGene[chrom2.size()];
		for (int i = 0; i < chrom1.size(); ++i) {
			CompositeGene compGene1 = (CompositeGene)chrom1.getGene(i);
			CompositeGene compGene2 = (CompositeGene)chrom2.getGene(i);
			
			List<Gene> par1 = compGene1.getGenes();
			List<Gene> par2 = compGene2.getGenes();
			List<Gene> child1 = new ArrayList<Gene>(par1.size());
			List<Gene> child2 = new ArrayList<Gene>(par2.size());
			for (int j = 0; j < par1.size(); ++j) {
				child1.add(null);
				child2.add(null);
			}
			//Gene[] child1 = new Gene[par1.size()];
			//Gene[] child2 = new Gene[par2.size()];
			
			switch(crossoverType) {
			case ORDER_ONE:
				doOrderOneCrossover(par1, par2, child1, child2, rand);
				break;
			case PMX:
				doPartiallyMappedCrossover(par1, par2, child1, child2, rand);
				break;
			case CYCLE:
				doCycleCrossover(par1, par2, child1, child2, rand);
				break;
			}

			genes1[i] = new CompositeGene(chrom1.getConfiguration());
			genes2[i] = new CompositeGene(chrom1.getConfiguration());
			for(Gene gene: child1) genes1[i].addGene(gene);
			for(Gene gene: child2) genes2[i].addGene(gene);

		}
		chrom1.setGenes(genes1);
		chrom2.setGenes(genes2);
		candidates.add(chrom1);
		candidates.add(chrom2);
	}
	
    /**
     * Perform a basic crossover by randomly selecting which parent to take each gene (row) from
     * @param chrom1
     * @param chrom2
     * @param candidates
     * @param rand
     * @throws InvalidConfigurationException
     */
    private void doSimpleCrossover(IChromosome chrom1, IChromosome chrom2, List candidates, Random rand)
                       throws InvalidConfigurationException {
               CompositeGene[] genes1 = new CompositeGene[chrom1.size()];
               CompositeGene[] genes2 = new CompositeGene[chrom2.size()];
               for (int i = 0; i < chrom1.size(); ++i) {
                       if (rand.nextBoolean()) {
                               genes1[i] = (CompositeGene) chrom1.getGene(i);
                               genes2[i] = (CompositeGene) chrom2.getGene(i);
                       } else {
                               genes1[i] = (CompositeGene) chrom2.getGene(i);
                               genes2[i] = (CompositeGene) chrom1.getGene(i);
                       }
               }
               chrom1.setGenes(genes1);
               chrom2.setGenes(genes2);
               candidates.add(chrom1);
               candidates.add(chrom2);
       }


	/**
	 * Perform an Order One Crossover between two chromosomes
	 * @param par1 the genes of the first parent
	 * @param par2 the genes of the second parent
	 * @param child1 the genes for the first child
	 * @param child2 the genes for the second child
	 * @param rand a random number generator
	 */
	private void doOrderOneCrossover(List<Gene> par1, List<Gene> par2, List<Gene> child1, List<Gene> child2, Random rand) {
		int numGenes = par1.size();
		
		List<Gene> used1 = new ArrayList<Gene>();
		List<Gene> used2 = new ArrayList<Gene>();
		int start = rand.nextInt(numGenes-1);
		int end = start + rand.nextInt(numGenes - start) + 1;
		for (int i = start; i < end; ++i) {
			child1.set(i, par2.get(i));
			child2.set(i, par1.get(i));
			used1.add(par2.get(i));
			used2.add(par1.get(i));
		}
		int j = end, k = end;
		for (int i = 0; i < numGenes; ++i) {
			int index = (i+end)%numGenes;
			if (!used1.contains(par1.get(index))) {
				child1.set(j%numGenes, par1.get(index));
				++j;
			}
			if (!used2.contains(par2.get(index))) {
				child2.set(k%numGenes, par2.get(index));
				++k;
			}
		}
	}
	
	/**
	 * Perform a Partially Mapped Crossover between two chromosomes
	 * @param par1 the genes of the first parent
	 * @param par2 the genes of the second parent
	 * @param child1 the genes for the first child
	 * @param child2 the genes for the second child
	 * @param rand a random number generator
	 */
	private void doPartiallyMappedCrossover(List<Gene> par1, List<Gene> par2, List<Gene> child1, List<Gene> child2, Random rand) {
		//TODO update for CompositeGene
		int numGenes = par1.size();
		
		List<Gene> used1 = new ArrayList<Gene>();
		List<Gene> used2 = new ArrayList<Gene>();
		List<Gene> need1 = new ArrayList<Gene>();
		List<Gene> need2 = new ArrayList<Gene>();
		
		int start = rand.nextInt(numGenes-1);
		int end = start + rand.nextInt(numGenes - start) + 1;

		for (int i = start; i < end; ++i) {
			child1.set(i, par1.get(i));
			child2.set(i, par2.get(i));
			used1.add(par1.get(i));
			used2.add(par2.get(i));
		}
		for (int i = start; i < end; ++i) {
			if (!used1.contains(par2.get(i))) {
				need1.add(par2.get(i));
			}
			if (!used2.contains(par1.get(i))) {
				need2.add(par1.get(i));
			}
		}
		for (int j = 0; j < numGenes - (end - start); ++j) {
			int i = (j+end)%numGenes;
			if (used1.contains(par2.get(i))) {
				child1.set(i, need1.get(0));
				need1.remove(0);
			} else {
				child1.set(i, par2.get(i));
			}

			if (used2.contains(par1.get(i))) {
				child2.set(i, need2.get(0));
				need2.remove(0);
			} else {
				child2.set(i, par1.get(i));
			}
			
		}
	}
	
	private void doCycleCrossover(List<Gene> par1, List<Gene> par2, List<Gene> child1, List<Gene> child2, Random rand) {
		int start = 0;
		int count = 0;
		boolean flip = false;
		while (true) {
			int i = start;
			while ((!child1.contains(par1.get(i)) && !flip) || (!child2.contains(par1.get(i)) && flip)) {
				if (flip) {
					child2.set(i, par1.get(i));
					child1.set(i, par2.get(i));
				} else {
					child1.set(i, par1.get(i));
					child2.set(i, par2.get(i));
				}
				++count;
				i = par1.indexOf(par2.get(i));
			}
			if (count >= par1.size()) {
				break;
			}
			while (child1.contains(par1.get(start))) {
				++start;
			}
			flip = !flip;
		}
	}

}
