package sturdy.language.wasm.generic

import sturdy.values.Structural
import swam.{FuncType, ValType}

import ValType.*

enum HostFunction(val name: String, val funcType: FuncType) extends Enumeration:
  case proc_exit extends HostFunction("proc_exit", FuncType(Vector(I32), Vector()))
  case fd_close extends HostFunction("fd_close", FuncType(Vector(I32), Vector(I32)))
  case fd_read extends HostFunction("fd_read", FuncType(Vector(I32, I32, I32, I32), Vector(I32)))
  case fd_seek extends HostFunction("fd_seek", FuncType(Vector(I32, I64, I32, I32), Vector(I32)))
  case fd_write extends HostFunction("fd_write", FuncType(Vector(I32, I32, I32, I32), Vector(I32)))
  case fd_fdstat_get extends HostFunction("fd_fdstat_get", FuncType(Vector(I32, I32), Vector(I32)))

  override def toString(): String = name


given Structural[HostFunction] with {}

object HostFunction:
  val nameMap: Map[String, HostFunction] = HostFunction.values.map(f => f.name -> f).toMap
  
  def nameToHostFunction(name: String): HostFunction =
    nameMap.getOrElse(name, throw IllegalArgumentException(s"No host function with name $name."))