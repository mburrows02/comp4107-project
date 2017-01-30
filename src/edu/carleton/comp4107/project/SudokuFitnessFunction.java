package edu.carleton.comp4107.project;

import java.util.HashSet;
import java.util.Set;

import org.jgap.FitnessFunction;
import org.jgap.IChromosome;


public class SudokuFitnessFunction extends FitnessFunction {
	private final int[][] puzzle;
	
	public SudokuFitnessFunction(int[][] puzzle) {
		this.puzzle = puzzle;
	}
	
	@Override
	protected double evaluate(IChromosome arg0) {
		int count = 0;
		int[][] solution = SudokuUtils.formatPuzzle(arg0, this.puzzle);
		for (int i = 0; i < puzzle.length; ++i) {
			count += isColumnValid(solution, i);
			count += isBoxValid(solution, i);
		}
		return count;
	}
	
	/**
	 * Determines how close a row is to valid (i.e. the number of distinct
	 * 	values in it)
	 * @param solution the puzzle
	 * @param i the row number
	 * @return the number of distinct values in the row
	 */
	private int isRowValid(int[][] solution, int i) {
		Set<Integer> values = new HashSet<Integer>();
		for (int j = 0; j < solution.length; ++j) {
			values.add(solution[i][j]);
		}
		return values.size();
	}

	/**
	 * Determines how close a column is to valid (i.e. the number of distinct
	 * 	values in it)
	 * @param solution the puzzle
	 * @param i the column number
	 * @return the number of distinct values in the column
	 */
	private int isColumnValid(int[][] solution, int i) {
		Set<Integer> values = new HashSet<Integer>();
		for (int j = 0; j < solution.length; ++j) {
			values.add(solution[j][i]);
		}
		return values.size();
	}

	/**
	 * Determines how close a box is to valid (i.e. the number of distinct
	 * 	values in it)
	 * @param solution the puzzle
	 * @param i the box number (numbered left to right then top to bottom)
	 * @return the number of distinct values in the box
	 */
	private int isBoxValid(int[][] solution, int i) {
		Set<Integer> values = new HashSet<Integer>();
		int boxSize = (int)Math.round(Math.sqrt(solution.length));
		int boxRow = (int)Math.floor((double)i/(double)boxSize)*boxSize;
		int boxCol = (i % boxSize)*boxSize;
		for (int j = 0; j < solution.length; ++j) {
			int row = (int)Math.floor((double)j/(double)boxSize) + boxRow;
			int col = (j % boxSize) + boxCol;
			values.add(solution[row][col]);
		}
		return values.size();
	}
	
	private int doesSolutionMatchPuzzle(int[][] solution) {
		int count = 0;
		for (int i = 0; i < solution.length; ++i) {
			for (int j = 0; j < solution.length; ++j) {
				if (puzzle[i][j] != SudokuUtils.BLANK && puzzle[i][j] == solution[i][j]) {
					++count;
				}
			}
		}
		return count;
	}
}
