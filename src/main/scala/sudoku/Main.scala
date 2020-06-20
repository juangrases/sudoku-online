package sudoku

object Main {


  type Sudoku = Array[Array[Option[Int]]]


  type SudokuWithCandidates = Array[Array[SGrid]]


  def toSudokuWithCandidates(s: Sudoku): SudokuWithCandidates = {
    s.view.zipWithIndex.map{ case (row, rowIndex) =>
      row.view.zipWithIndex.map{ case (value, columnIndex) =>
        val position = Position(columnIndex, rowIndex)
        SGrid(value, Set(), position)
      }.toArray
    }.toArray
  }

  def generateSudoku(): Sudoku = {
    ???
  }

  def solveSudoku(s: SudokuWithCandidates): SudokuWithCandidates = {
    val tableWithCandidates = computeAllCandidates(s)
    val solvedGrid: SGrid = solveGridWithLessCandidates(tableWithCandidates)
    solveSudoku(tableWithCandidates, solvedGrid)
  }

  //Note: No need to have an Array for this step
  private def computeAllCandidates(s: SudokuWithCandidates): Array[Array[SGrid]] = {
    s.map { row =>
      row.map { case grid@SGrid(value, _, position) =>
        if (value.isDefined) {
          grid
        } else {
          val newCandidates = computeCandidates(position, s)
          SGrid(value, newCandidates, position)
        }
      }
    }
  }

  @scala.annotation.tailrec
  private def solveSudoku(s: SudokuWithCandidates, lastSolved: SGrid): SudokuWithCandidates = {
    if(s.forall(_.forall(_.value.isDefined))) return s

    val updatedSudoku = updateCandidatesFromLastSolved(s, lastSolved)

    val solvedGrid: SGrid = solveGridWithLessCandidates(updatedSudoku)
    solveSudoku(updatedSudoku, solvedGrid)
  }

  private def updateCandidatesFromLastSolved(s: SudokuWithCandidates, lastSolved: SGrid): SudokuWithCandidates = {
    //NOTE: Array structure allow not to access a row directly
    val rowGrids: Array[SGrid] = s(lastSolved.position.row).filter(_.value.isEmpty)
    val columnGrids: Array[SGrid] = getGridsFromSameColumn(s, lastSolved).filter(_.value.isEmpty)
    val squareGrids = getGridsFromSameSquare(s, lastSolved).filter(_.value.isEmpty)
    updateCandidates(s, rowGrids ++ columnGrids ++ squareGrids)
  }


  private def solveGridWithLessCandidates(s: SudokuWithCandidates) = {
    val f = s.flatten
    val toBesSolvedGrid = s.flatten.filter(_.value.isEmpty).minBy(_.candidates.size)
    val solvedGrid = toBesSolvedGrid.copy(value = Some(toBesSolvedGrid.candidates.head))
    s(solvedGrid.position.row)(solvedGrid.position.column) = solvedGrid
    solvedGrid
  }

  private def getGridsFromSameColumn(s: SudokuWithCandidates, g: SGrid): Array[SGrid] = {
    s.foldLeft(Array[SGrid]()) {
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
      s(squareColumn)(squareRow)
    }).toArray
  }

  //NOTE: This step we get a benefit of use an Array as we update specific grids
  private def updateCandidates(sudoku: SudokuWithCandidates, newGrids: Array[SGrid]): SudokuWithCandidates = {
    newGrids.map{grid =>
      grid.copy(candidates = computeCandidates(grid.position,sudoku))
    }.foreach{ updatedGrid =>
      sudoku(updatedGrid.position.row)(updatedGrid.position.column) = updatedGrid
    }
    sudoku
  }

  def computeCandidates(p: Position, s: SudokuWithCandidates): Set[Int] = {
    //Grid starts with all candidates
    //TODO: Make this structure better
    val grid = SGrid(None, Set(1,2,3,4,5,6,7,8,9), p)
    val reducedByRow = reduceCandidatesFromRow(grid, s)
    val reducedByRowAndColumn = reduceCandidatesFromColumn(reducedByRow, s)
    val candidates = reduceCandidatesFromSquare(reducedByRowAndColumn, s).candidates
    if(candidates.isEmpty) throw new Exception("Sudoku cannot be solved")
    candidates
  }

  /*
    (x,y) = (0,0) => Top left of sudoku

    Sudoky is Array of Arrays. Every Array is a Row

   */
  private def reduceCandidatesFromRow(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    s(grid.position.row).foldLeft(grid) {
      case (finalResult, theRow) =>
        theRow.value.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
    }
  }

  private def reduceCandidatesFromColumn(grid: SGrid, s: SudokuWithCandidates): SGrid = {
    val column = grid.position.column
    val columnList = s.foldLeft(Seq[Option[Int]]()) {
      case (result, row) => result :+ row(column).value
    }
    columnList.foldLeft(grid) {
      case (finalResult, theRow) =>
        theRow.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
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
      s(squareRow)(squareColumn)
    })
      .foldLeft(grid) {
        case (finalResult, theRow) =>
          theRow.value.map(v => finalResult.copy(candidates = finalResult.candidates - v)).getOrElse(finalResult)
      }
  }
}
