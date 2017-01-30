package edu.carleton.comp4107.project;


import org.jgap.*;
import org.jgap.impl.CompositeGene;

import java.util.Collections;
import java.util.List;

public class SwapMutation implements GeneticOperator {
	public static double MUTATION_RATE = 0.1;
	
    @Override
    public void operate(Population population, List candidates) {
        int size = population.size();
        for(int i = 0; i < size; ++i) {
            boolean mutated = false;
            IChromosome chromosome = population.getChromosome(i);
            for (Gene g: chromosome.getGenes()) {
                CompositeGene gene = (CompositeGene) g;
                int gSize = gene.size();
                if (Math.random() < MUTATION_RATE) {
                    mutated = true;
                    Collections.swap(gene.getGenes(), (int) (Math.random() * gSize), (int) (Math.random() * gSize));
                }
            }
            if (mutated) {
               // candidates.add(chromosome);
            }
        }
    }
}
