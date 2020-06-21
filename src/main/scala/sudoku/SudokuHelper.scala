package sudoku

import scala.collection.mutable
import scala.util.{Failure, Success, Try}

object SudokuHelper {


  type Sudoku = Array[Array[Option[Int]]]

  case class SudokuWithCandidates(v: Array[Array[SGrid]])


  def toSudokuWithCandidates(s: Sudoku): SudokuWithCandidates = {
    SudokuWithCandidates(s.view.zipWithIndex.map { case (row, rowIndex) =>
      row.view.zipWithIndex.map { case (value, columnIndex) =>
        val position = Position(columnIndex, rowIndex)
        SGrid(value, Set(1, 2, 3, 4, 5, 6, 7, 8, 9), position)
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

  def solveSudoku(s: SudokuWithCandidates): SudokuWithCandidates = {
    val tableWithCandidates = computeAllCandidates(s)
    val value = solveSudokuI(tableWithCandidates, None)
    value.get
  }

  //Note: No need to have an Array for this step
  private def computeAllCandidates(s: SudokuWithCandidates): SudokuWithCandidates = {
    s.copy(v = s.v.map { row =>
      row.map { case grid@SGrid(value, _, position) =>
        if (value.isDefined) {
          grid
        } else {
          val newCandidates = computeCandidates(grid, s)
          SGrid(value, newCandidates, position)
        }
      }
    })
  }

  private def solveSudokuI(s: SudokuWithCandidates, lastSolved: Option[SGrid]): Option[SudokuWithCandidates] = {
    printSudoku(s)
    if (s.v.flatten.forall(_.value.isDefined)) return Some(s)

    val updatedSudokuOpt = lastSolved.map(ls => updateCandidatesFromLastSolved(s, ls)).getOrElse(Success(s))
    updatedSudokuOpt match {
      case Success(updatedSudoku) =>
        val toBesSolvedGrid = s.v.flatten.filter(_.value.isEmpty).minBy(_.candidates.size)
        toBesSolvedGrid.candidates.foldLeft(Option.empty[SudokuWithCandidates]) { case (resultOpt, c) =>
          if (resultOpt.isDefined) {
            resultOpt
          } else {
            val solvedGrid = toBesSolvedGrid.copy(value = Some(c))
            s.v(solvedGrid.position.row)(solvedGrid.position.column) = solvedGrid
            solveSudokuI(updatedSudoku, Some(solvedGrid)) match {
              case None =>
                s.v(solvedGrid.position.row)(solvedGrid.position.column) = solvedGrid.copy(value = None, candidates = Set(1, 2, 3, 4, 5, 6, 7, 8, 9))
                resetCandidates(s, solvedGrid)
                None
              case a =>
                a
            }
          }
        }

      case Failure(exception) =>
        println(exception.getMessage)
        None
    }

  }


  private def updateCandidatesFromLastSolved(s: SudokuWithCandidates, lastSolved: SGrid): Try[SudokuWithCandidates] = {
    //NOTE: Array structure allow not to access a row directly
    val rowGrids: Array[SGrid] = s.v(lastSolved.position.row).filter(_.value.isEmpty)
    val columnGrids: Array[SGrid] = getGridsFromSameColumn(s, lastSolved).filter(_.value.isEmpty)
    val squareGrids = getGridsFromSameSquare(s, lastSolved).filter(_.value.isEmpty)
    updateCandidates(s, rowGrids ++ columnGrids ++ squareGrids)
  }

  private def resetCandidates(s: SudokuWithCandidates, lastSolved: SGrid): Option[SudokuWithCandidates] = {
    //NOTE: Array structure allow not to access a row directly
    val rowGrids: Array[SGrid] = s.v(lastSolved.position.row).filter(_.value.isEmpty)
    val columnGrids: Array[SGrid] = getGridsFromSameColumn(s, lastSolved).filter(_.value.isEmpty)
    val squareGrids = getGridsFromSameSquare(s, lastSolved).filter(_.value.isEmpty)
    Try {
      (rowGrids ++ columnGrids ++ squareGrids).foreach { grid =>
        //BUG: don't add a candidate that might be wrong

        s.v(grid.position.row)(grid.position.column) = grid.copy(candidates = computeCandidates(grid, s))
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

  //NOTE: This step we get a benefit of use an Array as we update specific grids
  private def updateCandidates(sudoku: SudokuWithCandidates, newGrids: Array[SGrid]): Try[SudokuWithCandidates] = {
    Try {
      newGrids.map { grid =>
        val newCandidates = computeCandidates(grid, sudoku)
        grid.copy(candidates = newCandidates)
      }.foreach { updatedGrid =>
        sudoku.v(updatedGrid.position.row)(updatedGrid.position.column) = updatedGrid
      }
      sudoku
    }
  }

  def computeCandidates(p: SGrid, s: SudokuWithCandidates): Set[Int] = {
    //Grid starts with all candidates
    //TODO: Make this structure better
    val grid = SGrid(None, Set(1, 2, 3, 4, 5, 6, 7, 8, 9), p.position)
    val reducedByRow = reduceCandidatesFromRow(grid, s)
    val reducedByRowAndColumn = reduceCandidatesFromColumn(reducedByRow, s)
    val candidates = reduceCandidatesFromSquare(reducedByRowAndColumn, s).candidates
    if (candidates.isEmpty)
      throw new Exception(s"Sudoku cannot be solved because grid column ${grid.position.column} row ${grid.position.row} doesn't have candidates")
    candidates
  }

  /*
    (x,y) = (0,0) => Top left of sudoku

    Sudoky is Array of Arrays. Every Array is a Row

   */
  private def reduceCandidatesFromRow(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    s.v(grid.position.row).filter(_.value.isDefined).foldLeft(grid) {
      case (finalResult, theRow) =>
        finalResult.copy(candidates = finalResult.candidates - theRow.value.get)
    }
  }

  private def reduceCandidatesFromColumn(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    val column = grid.position.column
    val columnList = s.v.foldLeft(Seq[Option[Int]]()) {
      case (result, row) => result :+ row(column).value
    }
    columnList.filter(_.isDefined).foldLeft(grid) {
      case (finalResult, theRow) =>
        finalResult.copy(candidates = finalResult.candidates - theRow.get)
    }
  }


  /*
  Squares starts on X = 0, 3, 6 and Y = 0, 3, 6
   */
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
          finalResult.copy(candidates = finalResult.candidates - theGrid.value.get)
      }
  }
}
