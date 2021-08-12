package sturdy.language.wasm.generic

import swam.*
import swam.syntax.Func

trait ModuleInstance:
  val functionTypes: Vector[FuncType]
  val functions: Vector[FunctionInstance]
  val tables: Vector[TableInstance]
  val memoryAddrs: Vector[Int]
  val globalAddrs: Vector[Int]
  val elementAddrs: Vector[Int]
  val dataAddrs: Vector[Int]
  val exports: Vector[(String, ExternalValue)]

enum FunctionInstance:
  case Wasm(module: ModuleInstance, func: Func, ft: FuncType)
  //  case HostX(...)

  def funcType: FuncType = this match
    case Wasm(_, _ , ft) => ft

case class TableInstance(functions: Vector[FunctionInstance])

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)