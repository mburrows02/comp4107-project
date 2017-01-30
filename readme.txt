COMP 4107 Project - Solving Sudoku with Genetic Algorithms
Alexis Beingessner - 100816810
Michelle Burrows - 100819652

Structure
---------
bin:	contains compiled class files after compilation
data:	contains sample puzzles
lib:	contains required libraries (JGAP)
src:	contains all source code

Compilation
-----------
Run compile.sh (or see compile.sh for compilation command)

Execution
---------
Run run.sh [options] (or see run.sh for compilation command)
Options:
--puzzle <filename>
	the puzzle file to load
--pop-size <value>
	the population size
--stuck <value>
	the number of generations to keep trying without improvement in max fitness before giving up
--tries <value>
	the number of times to try to solve the puzzle
--tourn-size <value>
	the tournament size to use for selection
--tourn-prob <value>
	the probability of selecting the tournament winner
--crossover-rate <value> 
	the crossover rate
--mutation <value>
	the mutation rate
--order-one 
	use order one crossover
--pmx
	use partially mapped crossover (default)
--cycle 
	use cycle crossover
