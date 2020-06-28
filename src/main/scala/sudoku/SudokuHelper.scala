package sudoku

import scala.collection.immutable.Iterable
import scala.util.{Failure, Success, Try}

object SudokuHelper {


  type Sudoku = Array[Array[Option[Int]]]

  case class SudokuWithCandidates(v: Array[Array[SGrid]])

  case class IllegalSudokuState(message: String) extends Exception(message)

  private val ALL_CANDIDATES = Set(1, 2, 3, 4, 5, 6, 7, 8, 9)


  /*
  Solve the sudoku in a not very functional way as the SudokuWithCandidates is formed from an Array of Arrays which are mutated during the process
   */
  def solveSudoku(s: SudokuWithCandidates): Option[SudokuWithCandidates] =
    Try(solveSudokuInternal(computeAllCandidates(s), None)).toOption.flatten


  def toSudokuWithCandidates(s: Sudoku): SudokuWithCandidates = {
    SudokuWithCandidates(s.view.zipWithIndex.map { case (row, rowIndex) =>
      row.view.zipWithIndex.map { case (value, columnIndex) =>
        val position = Position(columnIndex, rowIndex)
        SGrid(value, ALL_CANDIDATES, position)
      }.toArray
    }.toArray)
  }

  def printSudoku(s: SudokuWithCandidates) = {
    s.v.foreach { row =>
      row.foreach(v => print(v.value.getOrElse("_") + " "))
      println("")
    }
  }

  def generateSudoku(): Sudoku = {
    ???
  }


  //Note: No need to have an Array for this step
  private def computeAllCandidates(s: SudokuWithCandidates): SudokuWithCandidates = {
    s.copy(v = s.v.map { row =>
      row.map { case grid@SGrid(value, _, p@Position(column, row)) =>
        if (value.isDefined) {
          if (computeCandidates(s, grid).contains(grid.value.get)) {
            grid
          } else {
            throw IllegalSudokuState(s"Value ${value.get} defined on row $row and column $column is invalid")
          }
        } else {
          val newCandidates = computeCandidates(s, grid)
          SGrid(value, newCandidates, p)
        }
      }
    })
  }

  private def solveSudokuInternal(s: SudokuWithCandidates, lastSolved: Option[SGrid]): Option[SudokuWithCandidates] = {
    if (s.v.flatten.forall(_.value.isDefined)) return Some(s)

    val updatedSudokuOpt = lastSolved.map(ls => updateCandidatesFromLastSolved(s, ls)).getOrElse(Success(s))
    updatedSudokuOpt match {
      case Success(updatedSudoku) =>
        val toBesSolvedGrid = s.v.flatten.filter(_.value.isEmpty).minBy(_.candidates.size)
        toBesSolvedGrid.candidates.foldLeft(Option.empty[SudokuWithCandidates]) {
          case (a@Some(_), _) =>
            a
          case (None, c) =>
            val solvedGrid = toBesSolvedGrid.copy(value = Some(c))
            s.v(solvedGrid.position.row)(solvedGrid.position.column) = solvedGrid
            solveSudokuInternal(updatedSudoku, Some(solvedGrid)) match {
              case None =>
                s.v(solvedGrid.position.row)(solvedGrid.position.column) = solvedGrid.copy(value = None, candidates = ALL_CANDIDATES)
                resetLastMove(s, solvedGrid, c)
                None
              case a => a
            }
        }

      case Failure(_: IllegalSudokuState) =>
        None
    }

  }


  /*
   Update candidates of all grids that are in the same row, column and square of the last solved grid
   */
  private def updateCandidatesFromLastSolved(s: SudokuWithCandidates, lastSolved: SGrid): Try[SudokuWithCandidates] = {
    val rowGrids: Array[SGrid] = s.v(lastSolved.position.row).filter(_.value.isEmpty)
    val columnGrids: Array[SGrid] = getGridsFromSameColumn(s, lastSolved).filter(_.value.isEmpty)
    val squareGrids = getGridsFromSameSquare(s, lastSolved).filter(_.value.isEmpty)
    updateCandidates(s, (rowGrids ++ columnGrids ++ squareGrids))
  }

  private def resetLastMove(s: SudokuWithCandidates, lastSolved: SGrid, oldValue: Int): Option[SudokuWithCandidates] = {
    val rowGrids: Array[SGrid] = s.v(lastSolved.position.row).filter(_.value.isEmpty)
    val columnGrids: Array[SGrid] = getGridsFromSameColumn(s, lastSolved).filter(_.value.isEmpty)
    val squareGrids: Array[SGrid] = getGridsFromSameSquare(s, lastSolved).filter(_.value.isEmpty)
    Try {
      (rowGrids ++ columnGrids ++ squareGrids).foreach { grid =>
        s.v(grid.position.row)(grid.position.column) = grid.copy(candidates = grid.candidates + oldValue)
      }
      s
    }.toOption
  }

  private def getGridsFromSameColumn(s: SudokuWithCandidates, g: SGrid): Array[SGrid] = {
    s.v.foldLeft(Array[SGrid]()) {
      case (result, row) => result :+ row(g.position.column)
    }
  }

  private def getGridsFromSameSquare(s: SudokuWithCandidates, g: SGrid): Array[SGrid] = {
    val lastSolvedRow = g.position.row
    val lastSolvedColumn = g.position.column
    val (squareRowStart, squareColumnStart) = (lastSolvedRow - lastSolvedRow % 3, lastSolvedColumn - lastSolvedColumn % 3)

    (for {
      squareRow <- squareRowStart to squareRowStart + 2
      squareColumn <- squareColumnStart to squareColumnStart + 2
    } yield {
      s.v(squareRow)(squareColumn)
    }).toArray
  }

  private def updateCandidates(sudoku: SudokuWithCandidates, newGrids: Iterable[SGrid]): Try[SudokuWithCandidates] = {
    Try {
      newGrids.map { grid =>
        val newCandidates = computeCandidates(sudoku, grid)
        grid.copy(candidates = newCandidates)
      }.foreach { updatedGrid =>
        sudoku.v(updatedGrid.position.row)(updatedGrid.position.column) = updatedGrid
      }
      sudoku
    }
  }

  def computeCandidates(s: SudokuWithCandidates, p: SGrid): Set[Int] = {
    val grid = SGrid(None, Set(1, 2, 3, 4, 5, 6, 7, 8, 9), p.position)
    val reducedByRow = reduceCandidatesFromRow(grid, s)
    val reducedByRowAndColumn = reduceCandidatesFromColumn(reducedByRow, s)
    val candidates = reduceCandidatesFromSquare(reducedByRowAndColumn, s).candidates
    if (candidates.isEmpty)
      throw IllegalSudokuState(s"Sudoku cannot be solved because grid column ${grid.position.column} row ${grid.position.row} doesn't have candidates")
    candidates
  }

  private def reduceCandidatesFromRow(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    s.v(grid.position.row).filter(_.value.isDefined).foldLeft(grid) {
      case (finalResult, theRow) =>
        if (theRow.position == grid.position) {
          finalResult
        } else {
          finalResult.copy(candidates = finalResult.candidates - theRow.value.get)
        }
    }
  }

  private def reduceCandidatesFromColumn(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    val columnList: Seq[SGrid] = getGridsFromSameColumn(s, grid)
    columnList.filter(_.value.isDefined).foldLeft(grid) {
      case (finalResult, theRow) =>
        if (grid.position == theRow.position) {
          finalResult
        } else {
          finalResult.copy(candidates = finalResult.candidates - theRow.value.get)
        }
    }
  }


  private def reduceCandidatesFromSquare(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    val row = grid.position.row
    val column = grid.position.column

    val (rowStart, columnStart) = (row - row % 3, column - column % 3)

    (for {
      squareRow <- rowStart to rowStart + 2
      squareColumn <- columnStart to columnStart + 2
    } yield {
      s.v(squareRow)(squareColumn)
    })
      .filter(_.value.isDefined)
      .foldLeft(grid) {
        case (finalResult, theGrid) =>
          if (theGrid.position == grid.position) {
            finalResult
          } else {
            finalResult.copy(candidates = finalResult.candidates - theGrid.value.get)
          }
      }
  }
}
