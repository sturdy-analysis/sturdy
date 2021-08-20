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

case class TableInstance[V](functions: Vector[FunctionInstance[V]])
case class GlobalInstance[V](var value: V)

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)