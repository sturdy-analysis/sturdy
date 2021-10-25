package sturdy.language.wasm.generic

import scodec.bits.ByteVector
import sturdy.{IsSound, Soundness, AbstractlySound, seqIsSound}
import swam.*
import swam.syntax.Func
import sturdy.values.Finite
import sturdy.values.Join
import sturdy.values.MaybeChanged
import sturdy.values.Structural
import sturdy.values.concretePO
import sturdy.values.concreteAbstractly

case class TableAddr(addr: Int) extends AnyVal
case class MemoryAddr(addr: Int) extends AnyVal
case class GlobalAddr(addr: Int) extends AnyVal

given Finite[TableAddr] with {}
given Finite[MemoryAddr] with {}
given Finite[GlobalAddr] with {}
given Structural[TableAddr] with {}
given Structural[MemoryAddr] with {}
given Structural[GlobalAddr] with {}

class ModuleInstance:
  var functionTypes: Vector[FuncType] = Vector.empty
  var functions: Vector[FunctionInstance] = Vector.empty
  var tableAddrs: Vector[TableAddr] = Vector.empty
  var memoryAddrs: Vector[MemoryAddr] = Vector.empty
  var globalAddrs: Vector[GlobalAddr] = Vector.empty
  var elems: Vector[ElemInstance] = Vector.empty
  var data: Vector[DataInstance] = Vector.empty
  var exports: Vector[(String, ExternalValue)] = Vector.empty

  override def toString: Name = Integer.toHexString(this.hashCode)

given moduleInstanceIsSound: Soundness[ModuleInstance, ModuleInstance] with
  // TODO: not optimal, because we don't check soundness of the function instances' modules
  override def isSound(c: ModuleInstance, a: ModuleInstance): IsSound =
    given vecIsSound[V](using Structural[V]): Structural[Vector[V]] with {}
    given Structural[(String, ExternalValue)] with {}
    val ftSound = summon[Soundness[Vector[FuncType], Vector[FuncType]]].isSound(c.functionTypes, a.functionTypes)
    val fSound = seqIsSound(using functionInstanceIsSoundFlat).isSound(c.functions, a.functions)
    val tabSound = summon[Soundness[Vector[TableAddr], Vector[TableAddr]]].isSound(c.tableAddrs, a.tableAddrs)
    val memSound = summon[Soundness[Vector[MemoryAddr], Vector[MemoryAddr]]].isSound(c.memoryAddrs, a.memoryAddrs)
    val globSound = summon[Soundness[Vector[GlobalAddr], Vector[GlobalAddr]]].isSound(c.globalAddrs, a.globalAddrs)
    val elemSound = seqIsSound(using elemInstanceIsSound(using functionInstanceIsSoundFlat)).isSound(c.elems, a.elems)
    val datSound = summon[Soundness[Vector[DataInstance], Vector[DataInstance]]].isSound(c.data, a.data)
    val expSound = summon[Soundness[Vector[(String,ExternalValue)], Vector[(String,ExternalValue)]]].isSound(c.exports, a.exports)

    ftSound && fSound && tabSound && memSound && globSound && elemSound && datSound && expSound

given Structural[Func] with {}
given Structural[FuncType] with {}
given Structural[DataInstance] with {}

enum FunctionInstance:
  case Wasm(module: ModuleInstance, funcIx: Int,  func: Func, ft: FuncType)
  case Host(hf: HostFunction)

  def funcType: FuncType = this match
    case Wasm(_, _, _, ft) => ft
    case Host(hf) => hf.funcType

given functionInstanceIsSound: Soundness[FunctionInstance, FunctionInstance] with
  override def isSound(c: FunctionInstance, a: FunctionInstance): IsSound = (c,a) match
    case (FunctionInstance.Wasm(cM,_,_,_), FunctionInstance.Wasm(aM,_,_,_)) =>
      functionInstanceIsSoundFlat.isSound(c,a) &&
        summon[Soundness[ModuleInstance, ModuleInstance]].isSound(cM,aM)
    case _ => functionInstanceIsSoundFlat.isSound(c,a)

def functionInstanceIsSoundFlat: Soundness[FunctionInstance, FunctionInstance] = new Soundness[FunctionInstance, FunctionInstance] {
  override def isSound(c: FunctionInstance, a: FunctionInstance): IsSound = (c,a) match
    case (FunctionInstance.Wasm(_,_,cFunc,cFt), FunctionInstance.Wasm(_,_,aFunc,aFt)) =>
      val fIsSound = summon[Soundness[Func,Func]].isSound(cFunc, aFunc)
      val tIsSound = summon[Soundness[FuncType,FuncType]].isSound(cFt, aFt)
      fIsSound && tIsSound
    case (FunctionInstance.Host(chf), FunctionInstance.Host(ahf)) => summon[Soundness[HostFunction, HostFunction]].isSound(chf, ahf)
    case _ => IsSound.NotSound(s"Concrete function instance $c not approximated by $a.")
}

//case class TableInstance[V](tableType: TableType, functions: Vector[FunctionInstance[V]])
//case class GlobalInstance[V](tpe: ValType, val value: V)
case class DataInstance(data: ByteVector)
case class ElemInstance(functions: Vector[FunctionInstance])

given elemInstanceIsSound(using fSoundness: Soundness[FunctionInstance, FunctionInstance]): Soundness[ElemInstance, ElemInstance] with
  override def isSound(c: ElemInstance, a: ElemInstance): IsSound =
    seqIsSound.isSound(c.functions, a.functions)

//def mapFunctionInstance[A,B](f: A => B)(x: FunctionInstance[A]): FunctionInstance[B] = x match
//  case Wasm(mod, fun, ft) => Wasm(mod.mapModuleInstance(f), fun, ft)

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)