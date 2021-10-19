package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, ValType}

sealed trait HostFunction(val funcType: FuncType)

given Structural[HostFunction] with {}

object HostFunction:
  case class Exit() extends HostFunction(FuncType(Vector(ValType.I32), Vector()))
  
  val nameMap: Map[String, HostFunction] = Map("proc_exit" -> Exit())
  
  def nameToHostFunction(name: String): HostFunction =
    nameMap.getOrElse(name, throw IllegalArgumentException(s"No host function with name $name."))