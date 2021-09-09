package sturdy.language.wasm.generic

import scodec.bits.ByteVector
import swam.*
import swam.syntax.Func

trait ModuleInstance[V]:
  var functionTypes: Vector[FuncType] = Vector.empty
  var functions: Vector[FunctionInstance[V]] = Vector.empty
  var tableAddrs: Vector[Int] = Vector.empty
  var memoryAddrs: Vector[Int] = Vector.empty
  var globals: Vector[GlobalInstance[V]] = Vector.empty
  var elems: Vector[ElemInstance[V]] = Vector.empty
  var data: Vector[DataInstance] = Vector.empty
  var exports: Vector[(String, ExternalValue)] = Vector.empty


  def invokeExported[Addr,Bytes,Size](funcName: String, args: List[V])(using interp: Interpreter[V,Addr,Bytes,Size]): List[V] =
    exports.find((name,_) => name == funcName) match
      case Some((_,ExternalValue.Function(funcIx))) =>
        val func = functions.lift(funcIx).getOrElse(throw new Error(s"Unbound function index."))
        val paramTys = func.funcType.params
        if (paramTys.length != args.length)
          throw new Error(s"Wrong number of arguments in external invocation.")
        paramTys.zip(args).map(???) // TODO: check for right type -> we need some kind of generic language feature here
        val rtLength = func.funcType.t.length
        interp.invokeWithArguments(args, rtLength, func)
      case _ => throw new Error(s"Function with name $funcName was not found in module's exports.")

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