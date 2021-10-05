package sturdy.language.wasm.generic

import swam.{FuncType, ValType}

sealed trait HostFunction(val funcType: FuncType)

object HostFunction:
  case class Exit() extends HostFunction(FuncType(Vector(ValType.I32), Vector()))
  //def unapply(hf: HostFunction): Option(FuncType) = hf.funcType

  val nameMap: Map[String, HostFunction] = Map("proc_exit" -> Exit())

  case class ExitException[V](exitCode: V) extends Exception

  def nameToHostFunction(name: String): HostFunction =
    nameMap.getOrElse(name, throw IllegalArgumentException(s"No host function with name $name."))