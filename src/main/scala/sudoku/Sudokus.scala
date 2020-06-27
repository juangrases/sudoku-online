package sudoku

import web.Protocol.GridMessage


object Sudokus {
  val easyGames = Array(
    Array(
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
  )

  val hardGames = Array(
    Array(
      Array(None, Some(4), None, None, Some(5), None, Some(8), None, None),
      Array(None, None, None, Some(3), None, None, None, None, Some(6)),
      Array(None, None, Some(6), None, None, None, None, None, None),
      Array(None, None, None, None, Some(8), None, Some(2), Some(5), None),
      Array(None, Some(3), Some(2), None, None, None, Some(6), None, None),
      Array(None, None, None, Some(4), None, Some(7), None, None, Some(3)),
      Array(Some(3), None, None, None, Some(9), Some(8), Some(1), None, None),
      Array(Some(8), None, None, None, None, None, None, Some(7), None),
      Array(Some(1), None, None, None, Some(6), None, None, Some(3), Some(8))
    )
  )

  def toProtocolGame(s: Array[Array[Option[Int]]]): Array[Array[GridMessage]] = {
    s.map(_.map{
      case Some(n) => GridMessage(n.toString, editable = false)
      case None =>   GridMessage("", editable = true)
    })
  }
}
