package sturdy.language.wasm.generic

import swam.*
import swam.syntax.Func

trait ModuleInstance[V]:
  val functionTypes: Vector[FuncType]
  val functions: Vector[FunctionInstance[V]]
  val tables: Vector[TableInstance[V]]
  val memoryAddrs: Vector[Int]
  val globals: Vector[GlobalInstance[V]]
  val elementAddrs: Vector[Int]
  val dataAddrs: Vector[Int]
  val exports: Vector[(String, ExternalValue)]

enum FunctionInstance[V]:
  case Wasm(module: ModuleInstance[V], func: Func, ft: FuncType)
  //  case HostX(...)

  def funcType: FuncType = this match
    case Wasm(_, _ , ft) => ft

case class TableInstance[V](functions: Vector[FunctionInstance[V]])
case class GlobalInstance[V](var value: V)

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)