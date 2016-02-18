package io.eels

object RowUtils {

  def replaceByFn(index: Int, fn: Any => Any, row: Row): Row = {
    (row.slice(0, index) :+ fn(row(index))) ++ row.slice(index + 1, row.length)
  }

  def replace(from: String, target: Any, row: Row): Row = row.map {
    case `from` => target
    case other => other
  }

  def replace(index: Int, from: String, target: Any, row: Row): Row = {
    (row.slice(0, index) :+ (row(index) match {
      case `from` => target
      case other => other
    })) ++ row.slice(index + 1, row.length)
  }

  def toMap(schema: FrameSchema, row: Row): Map[String, Any] = {
    schema.columnNames.zip(row).map { case (field, value) => field -> value }.toMap
  }
}