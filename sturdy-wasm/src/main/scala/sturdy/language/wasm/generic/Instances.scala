package sturdy.language.wasm.generic

import scodec.bits.ByteVector
import swam.*
import swam.syntax.Func
import sturdy.values.Finite

case class TableAddr(addr: Int) extends AnyVal
case class MemoryAddr(addr: Int) extends AnyVal

given Finite[TableAddr] with {}
given Finite[MemoryAddr] with {}

trait ModuleInstance[V]:
  var functionTypes: Vector[FuncType] = Vector.empty
  var functions: Vector[FunctionInstance[V]] = Vector.empty
  var tableAddrs: Vector[TableAddr] = Vector.empty
  var memoryAddrs: Vector[MemoryAddr] = Vector.empty
  var globals: Vector[GlobalInstance[V]] = Vector.empty
  var elems: Vector[ElemInstance[V]] = Vector.empty
  var data: Vector[DataInstance] = Vector.empty
  var exports: Vector[(String, ExternalValue)] = Vector.empty

enum FunctionInstance[V]:
  case Wasm(module: ModuleInstance[V], func: Func, ft: FuncType)
  //  case HostX(...)

  def funcType: FuncType = this match
    case Wasm(_, _ , ft) => ft

case class TableInstance[V](tableType: TableType, functions: Vector[FunctionInstance[V]])
case class GlobalInstance[V](tpe: ValType, var value: V)
case class DataInstance(data: ByteVector)
case class ElemInstance[V](functions: Vector[FunctionInstance[V]])

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)