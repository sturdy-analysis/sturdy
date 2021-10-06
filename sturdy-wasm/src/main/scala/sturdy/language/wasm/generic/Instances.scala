package sturdy.language.wasm.generic

import scodec.bits.ByteVector
import swam.*
import swam.syntax.Func
import sturdy.values.Finite
import sturdy.values.Join

case class TableAddr(addr: Int) extends AnyVal
case class MemoryAddr(addr: Int) extends AnyVal
case class GlobalAddr(addr: Int) extends AnyVal

given Finite[TableAddr] with {}
given Finite[MemoryAddr] with {}

trait ModuleInstance[V]:
  var functionTypes: Vector[FuncType] = Vector.empty
  var functions: Vector[FunctionInstance[V]] = Vector.empty
  var tableAddrs: Vector[TableAddr] = Vector.empty
  var memoryAddrs: Vector[MemoryAddr] = Vector.empty
  var globalAddrs: Vector[GlobalAddr] = Vector.empty
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

def mapGlobalInstance[A,B](f: A => B)(x: GlobalInstance[A]): GlobalInstance[B] = GlobalInstance(x.tpe, f(x.value))
//def mapFunctionInstance[A,B](f: A => B)(x: FunctionInstance[A]): FunctionInstance[B] = x match
//  case Wasm(mod, fun, ft) => Wasm(mod.mapModuleInstance(f), fun, ft)

given JoinGlobalInstance[V](using j: Join[V]): Join[GlobalInstance[V]] with
  override def apply(g1: GlobalInstance[V], g2: GlobalInstance[V]): GlobalInstance[V] = (g1, g2) match
    case (GlobalInstance(t1,v1), GlobalInstance(t2,v2)) =>
      if (t1 == t2)
        GlobalInstance(t1, j(v1,v2))
      else throw new Error("Joining of global instances with different types is not allowed.")

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)