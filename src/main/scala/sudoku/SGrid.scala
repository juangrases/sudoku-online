package sudoku



//trait Grid{
//  def getValue: Option[Int]
//}
//case class ValueRow(v: Option[Int]) extends Grid {
//  override def getValue: Option[Int] = v
//}
//case class ValueGrid(v: SGrid) extends Grid {
//  override def getValue: Option[Int] = v.value
//}

case class Position(column: Int, row: Int)
case class SGrid(value: Option[Int], candidates: Set[Int], position: Position)
