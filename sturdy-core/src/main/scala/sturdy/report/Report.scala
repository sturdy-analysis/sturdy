package sturdy.report

import scala.collection.mutable.ListBuffer

type Program = String
type Column = String

class Report:
  val infos: ListBuffer[((Program,Column), Any)] = ListBuffer()
  val results: ListBuffer[((Program,Column), Any)] = ListBuffer()

  def addInfo(program: Program, column: Column, info: Any): Unit =
    infos += ((program, column) -> info)
  def addResult(program: Program, column: Column, result: Any): Unit =
    results += ((program, column) -> result)

