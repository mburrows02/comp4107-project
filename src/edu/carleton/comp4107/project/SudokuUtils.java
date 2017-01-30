package edu.carleton.comp4107.project;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.jgap.Gene;
import org.jgap.IChromosome;
import org.jgap.impl.CompositeGene;

public class SudokuUtils {
	public static final int BLANK = 0;

	/**
	 * Fills a puzzle in with a potential solution
	 * @param solution The solution values
	 * @return A copy of the puzzle with solution values filled in
	 */
	public static  int[][] formatPuzzle(IChromosome solution, int[][] inputPuzzle) {
		int size = inputPuzzle.length;
		int[][] puzzle = new int[size][];

		for (int i = 0; i < size; ++i) {

			CompositeGene rowGene = (CompositeGene) solution.getGene(i);
			List<Gene> row = rowGene.getGenes();

			puzzle[i] = new int[size];
			int k = 0;
			/*
			for (int j = 0; j < size; ++j) System.out.print(inputPuzzle[i][j]+ ", ");
			System.out.println("--");
			for (Gene g: row) System.out.print(g.getAllele()+ ", ");
			System.out.println("!!");
			*/
			for (int j = 0; j < size; ++j) {
				int input = inputPuzzle[i][j];
				if (input == BLANK) {
					puzzle[i][j] = (Integer) row.get(k++).getAllele();
				} else {
					puzzle[i][j] = input;
				}

			}
		}
		return puzzle;
	}
	
	/**
	 * Prints the given puzzle to stdout
	 * @param puzzle
	 */
	public  static void printPuzzle(int[][] puzzle) {
		for (int i = 0; i < puzzle.length; ++i) {
			for (int j = 0; j < puzzle.length; ++j) {
				System.out.print(puzzle[i][j] + "\t");
			}
			System.out.println();
		}
	}

	/**
	 * Read & deserialize test data from file and populate list
	 * @throws IOException
	 */
	public static int[][] loadPuzzle(String filename) throws IOException {
		FileReader fr = new FileReader(new File(filename));
		BufferedReader br = new BufferedReader(fr);
		String line;
		int[][] puzzle = null;
		int size = 0;
		int lineNum = 0;
		for (lineNum = 0; (line = br.readLine()) != null; ++lineNum) {
			if (!line.isEmpty()) {
				String[] stringRow = line.split(" ");
				int[] row = new int[stringRow.length];
				for (int i = 0; i < stringRow.length; ++i) {
					row[i] = Integer.parseInt(stringRow[i]);
				}
				if (puzzle == null) {
					size = row.length;
					double n = Math.sqrt(size);
					if (n != Math.floor(n)) {
						br.close();
						throw new IOException(SudokuSolver.DATA_ERR + 
								SudokuSolver.SIZE_ERR);
					}
					puzzle = new int[row.length][];
				}
				if (row.length != size || lineNum >= size) {
					br.close();
					throw new IOException(SudokuSolver.DATA_ERR + 
							SudokuSolver.ROW_LEN_ERR);
				}
				puzzle[lineNum] = row;
			}
		}
		if (lineNum < size) {
			br.close();
			throw new IOException(SudokuSolver.DATA_ERR + 
					SudokuSolver.NUM_ROWS_ERR);
		}
		br.close();
		return puzzle;
	}

}