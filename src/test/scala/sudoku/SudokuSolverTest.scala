package sudoku

import org.scalatest.flatspec.AnyFlatSpec
import sudoku.SudokuHelper.Sudoku

class SudokuSolverTest extends AnyFlatSpec{

  "A sudoku with an empty square" should " have only one candidate on empty square" in {

    val sudoku: Sudoku = Array(
      Array(None,Some(3),Some(5),Some(2),Some(6),Some(9),Some(7),Some(8),Some(1)),
      Array(Some(6),Some(8),Some(2),Some(5),Some(7),Some(1),Some(4),Some(9),Some(3)),
      Array(Some(1),Some(9),Some(7),Some(8),Some(3),Some(4),Some(5),Some(6),Some(2)),
      Array(Some(8),Some(2),Some(6),Some(1),Some(9),Some(5),Some(3),Some(4),Some(7)),
      Array(Some(3),Some(7),Some(4),Some(6),Some(8),Some(2),Some(9),Some(1),Some(5)),
      Array(Some(9),Some(5),Some(1),Some(7),Some(4),Some(3),Some(6),Some(2),Some(8)),
      Array(Some(5),Some(1),Some(9),Some(3),Some(2),Some(6),Some(8),Some(7),Some(4)),
      Array(Some(2),Some(4),Some(8),Some(9),Some(5),Some(7),Some(1),Some(3),Some(6)),
      Array(Some(7),Some(6),Some(3),Some(4),Some(1),Some(8),Some(2),Some(5),Some(9)),
    )

    val candidates: Set[Int] = SudokuHelper.computeCandidates(SGrid(None, Set(1,2,3,4,5,6,7,8,9), Position(0,0)), SudokuHelper.toSudokuWithCandidates(sudoku))

    assertResult(1)(candidates.size)
    assert(candidates(4))
  }

  "A sudoku with two empty square" should " be solvable" in {

    val sudoku: Sudoku = Array(
      Array(None,Some(3),Some(5),Some(2),Some(6),Some(9),Some(7),Some(8),Some(1)),
      Array(Some(6),Some(8),Some(2),Some(5),Some(7),Some(1),Some(4),Some(9),Some(3)),
      Array(Some(1),Some(9),Some(7),Some(8),Some(3),Some(4),Some(5),Some(6),Some(2)),
      Array(Some(8),Some(2),Some(6),Some(1),Some(9),Some(5),Some(3),Some(4),Some(7)),
      Array(Some(3),Some(7),Some(4),Some(6),Some(8),Some(2),Some(9),Some(1),Some(5)),
      Array(Some(9),Some(5),Some(1),Some(7),Some(4),None,Some(6),Some(2),Some(8)),
      Array(Some(5),Some(1),Some(9),Some(3),Some(2),Some(6),Some(8),Some(7),Some(4)),
      Array(Some(2),Some(4),Some(8),Some(9),Some(5),Some(7),Some(1),Some(3),Some(6)),
      Array(Some(7),Some(6),Some(3),Some(4),Some(1),Some(8),Some(2),Some(5),Some(9))
    )

    val solvedSudoku = SudokuHelper.solveSudoku(SudokuHelper.toSudokuWithCandidates(sudoku))

    assertResult(4)(solvedSudoku.v(0)(0).value.get)
    assertResult(3)(solvedSudoku.v(5)(5).value.get)
  }

  "An easy sudoku" should " be solvable" in {

    val sudoku: Sudoku = Array(
      Array(None, None, None, None, None, None, None, None, Some(8)),
      Array(Some(7), None, None, None, None, Some(4), None, Some(3), None),
      Array(None, Some(4), None, None, None, Some(3), Some(2), None, None),
      Array(Some(2), None, None, Some(3), Some(9), None, Some(8), None, Some(4)),
      Array(None, None, Some(7), Some(8), Some(2), None, None, Some(6), Some(3)),
      Array(None, Some(5), None, None, Some(7), Some(6), None, Some(9), Some(2)),
      Array(None, Some(7), Some(4), Some(2), Some(6), None, None, None, None),
      Array(None, Some(3), None, None, None, None, Some(6), Some(8), None),
      Array(Some(5), None, Some(6), None, None, Some(9), None, None, Some(7))
    )

    val solvedSudoku = SudokuHelper.solveSudoku(SudokuHelper.toSudokuWithCandidates(sudoku))
    SudokuHelper.printSudoku(solvedSudoku)
    assertResult(6)(solvedSudoku.v(0)(0).value.get)
//    assertResult(3)(solvedSudoku(5)(5).value.get)
  }


  "An extreme sudoku" should " be solvable" in {

    val sudoku: Sudoku = Array(
      Array(Some(2), None, None, None, None, Some(8), None, Some(5), None),
      Array(None, None, Some(3), None, None, None, None, Some(6), None),
      Array(None, Some(7), None, None, None, Some(2), None, None, Some(4)),
      Array(None, None, Some(6), None, None, Some(7), Some(3), None, None),
      Array(None, None, None, Some(2), Some(4), None, None, None, Some(7)),
      Array(None, Some(8), None, Some(6), None, None, None, None, None),
      Array(None, None, None, None, None, None, Some(4), None, None),
      Array(None, None, None, Some(1), Some(9), None, None, None, None),
      Array(Some(9), Some(1), None, None, None, None, None, None, Some(3))
    )

    val solvedSudoku = SudokuHelper.solveSudoku(SudokuHelper.toSudokuWithCandidates(sudoku))
    assertResult(6)(solvedSudoku.v(0)(1).value.get)
    assertResult(4)(solvedSudoku.v(0)(2).value.get)
    //    assertResult(3)(solvedSudoku(5)(5).value.get)
  }

}
