package sudoku

object Main {


  /*
  1. Sudoku Generator
  2. Sudoku Validator
  3.

   */



//  trait Row
//  case class ValueRow(v: Option[Int]) extends Row
//  case class ValueGrid(v: SGrid) extends
  type Sudoku = Array[Array[Option[Int]]]
  type SudokuWithCandidates = Array[Array[SGrid]]

  def generateSudoku(): Sudoku = {


    ???
  }

  def solveSudoku(s: Sudoku) = {

    /*
    How to keep the structure of Array[Array
    for every
     */

    val tableWithCandidates = s.view.zipWithIndex.map{ case (row, xIndex) =>
      row.view.zipWithIndex.map{ case (value, columnIndex) =>
        val position = Position(xIndex, columnIndex)
        val candidates = computeCandidates(position, s)
        SGrid(value, candidates, position)
      }.toArray
    }.toArray
    val solvedGrid = tableWithCandidates.map(_.minBy(_.candidates.size)).minBy(_.candidates.size)
    tableWithCandidates(solvedGrid.position.column)(solvedGrid.position.row) = solvedGrid
  }

  def solveSudoku(s: SudokuWithCandidates, lastSolved: SGrid) = {
    /*
       1. Update candidates of row column and square
       2. Repeat previous logic
     */

    val rowGrids: Array[SGrid] = s(lastSolved.position.row)
    val columnGrids: Array[SGrid] = s.foldLeft(Array[SGrid]()) {
      case (result, row) => result :+ row(lastSolved.position.column)
    }


    val lastSolvedRow = lastSolved.position.row
    val lastSolvedColumn = lastSolved.position.column
    val (squareRowStart, squareColumnStart) = (lastSolvedRow - lastSolvedRow % 3, lastSolvedColumn - lastSolvedColumn % 3)

    val squareGrids = (for {
      squareRow <- squareRowStart to squareRowStart + 2
      squareColumn <- squareColumnStart to squareColumnStart + 2
    } yield {
      s(squareRow)(squareColumn)
    }).toArray
    rowGrids.map(g => computeCandidates(g.position,s))

  }

  def computeCandidates(p: Position, s: Sudoku): Set[Int] = {
    //Grid starts with all candidates

    //TODO: Make this structure better
    val grid = SGrid(None, Set(1,2,3,4,5,6,7,8,9), p)
    val reducedByRow = reduceCandidatesFromRow(grid, s)
    val reducedByRowAndColumn = reduceCandidatesFromColumn(reducedByRow, s)
    reduceCandidatesFromSquare(reducedByRowAndColumn, s).candidates
  }

  /*
    (x,y) = (0,0) => Top left of sudoku

    Sudoky is Array of Arrays. Every Array is a Row

   */
  private def reduceCandidatesFromRow(grid: SGrid, s: Sudoku): SGrid = {
    s(grid.position.row).foldLeft(grid) {
      case (finalResult, theRow) =>
        theRow.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
    }
  }

  private def reduceCandidatesFromColumn(grid: SGrid, s: Sudoku): SGrid = {
    val column = grid.position.column
    val columnList = s.foldLeft(Seq[Option[Int]]()) {
      case (result, row) => result :+ row(column)
    }
    columnList.foldLeft(grid) {
      case (finalResult, theRow) =>
        theRow.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
    }
  }


  /*
  Squares starts on X = 0, 3, 6 and Y = 0, 3, 6
   */
  private def reduceCandidatesFromSquare(grid: SGrid, s: Sudoku): SGrid = {
    val row = grid.position.row
    val column = grid.position.column

    val (rowStart, columnStart) = (row - row % 3, column - column % 3)

    (for {
      squareRow <- rowStart to rowStart + 2
      squareColumn <- columnStart to columnStart + 2
    } yield {
      s(squareRow)(squareColumn)
    })
      .foldLeft(grid) {
        case (finalResult, theRow) =>
          theRow.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
      }
  }
}
