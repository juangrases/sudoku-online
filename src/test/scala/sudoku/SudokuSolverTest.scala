package sudoku

import org.scalatest.flatspec.AnyFlatSpec
import sudoku.Main.Sudoku

class SudokuSolverTest extends AnyFlatSpec{
  "A sudoku with an empty square" should "be solved" in {

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

    val candidates: Set[Int] = Main.computeCandidates(Position(0,0),sudoku)

    assertResult(1)(candidates.size)
    assert(candidates(4))
  }

}
