package sturdy.language.wasm.generic

import scodec.bits.ByteVector
import sturdy.IsSound.{NotSound, Sound}
import sturdy.language.wasm.ConcreteInterpreter.RefValue
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.abstractions.ControlFlow
import sturdy.{AbstractlySound, IsSound, Soundness, seqIsSound}
import swam.*
import swam.syntax.*
import sturdy.values.{Finite, Join, MaybeChanged, PartialOrder, Structural, concreteAbstractly, concretePO}

case class TableAddr(addr: Int) extends AnyVal:
  override def toString: String = addr.toString
case class MemoryAddr(addr: Int) extends AnyVal:
  override def toString: String = addr.toString
case class GlobalAddr(addr: Int) extends AnyVal:
  override def toString: String = addr.toString

given Finite[TableAddr] with {}
given Finite[MemoryAddr] with {}
given Finite[GlobalAddr] with {}
given Finite[FunctionInstance] with {}
given Structural[TableAddr] with {}
given Structural[MemoryAddr] with {}
given Structural[GlobalAddr] with {}
given Structural[FunctionInstance] with {}
given Ordering[MemoryAddr] = Ordering.by[MemoryAddr,Int](_.addr)

class BlockId(val b: FuncId | Block | Loop | (If, Boolean) | Global | Data | Elem):
  override def equals(obj: Any): Boolean = obj match
    case that: BlockId => this.b match
      case _: FuncId => this.b == that.b
      case (ifInst1: If, bool1) => that.b match
        case (ifInst2, bool2) => (ifInst1 eq ifInst2) && bool1 == bool2
        case _ => false
      case _ => this.b eq that.b
    case _ => false
  override val hashCode: Int = b.hashCode

class ModuleInstance(val id: Option[Any] = None):
  override def equals(obj: Any): Boolean = obj match
    case that: ModuleInstance => (this.id, that.id) match
      case (Some(id1), Some(id2)) => id1 == id2
      case _ => this eq that
    case _ => false
  override def hashCode(): Int = id match
    case None => super.hashCode()
    case Some(id) => id.hashCode()

  var functionTypes: Vector[FuncType] = Vector.empty

  private var _functions: Vector[FunctionInstance] = Vector.empty
  def functions: Vector[FunctionInstance] = _functions
  def addFunction(fun: FunctionInstance): Unit =
    _functions :+= fun
    fun match
      case FunctionInstance.Wasm(_, ix, func, _) =>
        val funcId = FuncId(this, ix)
        val loc = InstLoc.InFunction(funcId, 0)
        registerBlockSizes(BlockId(funcId), loc, func.body)
      case _: FunctionInstance.Host => // nothing

  var tableAddrs: Vector[TableAddr] = Vector.empty
  var memoryAddrs: Vector[MemoryAddr] = Vector.empty
  var globalAddrs: Vector[GlobalAddr] = Vector.empty
  var globalTypes: Vector[GlobalType] = Vector.empty
  var elements: Vector[ElemInstance] = Vector.empty
  var data: Vector[DataInstance] = Vector.empty
  var exports: Vector[(String, ExternalValue)] = Vector.empty

  def exportedFunctions: Map[String, ExternalValue.Function] =
    exports.collect {
      case (name, fun: ExternalValue.Function) => (name, fun)
    }.toMap

  /** For each block, where does each contained instruction start. */
  var blockInstLocs: Map[(BlockId, Int), InstLoc] = Map.empty

  lazy val cfgNodes: Set[CfgNode] = ControlFlow.allCfgNodes(this)

  def registerBlockSizes(block: BlockId, loc: InstLoc, insts: Iterable[Inst]): InstLoc =
    var current = loc
    for ((inst, ix) <- insts.zipWithIndex)
      blockInstLocs += (block, ix) -> current
      inst match
        case inst@If(_, thn, els) =>
          val thnLoc = current + 1
          val elsLoc = registerBlockSizes(BlockId(inst -> true), thnLoc, thn)
          current = registerBlockSizes(BlockId(inst -> false), elsLoc, els)
        case inst@Block(_, body) =>
          val id = BlockId(inst)
          val bodyLoc = current + 1
          current = registerBlockSizes(id, bodyLoc, body)
        case inst@Loop(_, body) =>
          val id = BlockId(inst)
          val bodyLoc = current + 1
          current = registerBlockSizes(id, bodyLoc, body)
        case _ =>
          current = current + 1
    current

  override def toString: Name = id match
    case Some(id) => id.toString
    case None => Integer.toHexString(super.hashCode())

given Ordering[ModuleInstance] = Ordering.by[ModuleInstance, Int](_.hashCode())

given Structural[Func] with {}
given Structural[FuncType] with {}
given Structural[DataInstance] with {}

enum FunctionInstance:
  case Wasm(mod: ModuleInstance, funcIx: Int,  func: Func, ft: FuncType)
  case Host(mod: ModuleInstance, funcIx: Int, hf: HostFunction)
  case Null()

  def funcIdx: FuncIdx = this match
    case Wasm(_, funcIx, _, _) => funcIx
    case Host(_, funcIx, _) => funcIx

  def funcType: FuncType = this match
    case Wasm(_, _, _, ft) => ft
    case Host(_, _, hf) => hf.funcType
    case Null() => FuncType(Vector.empty, Vector.empty)

  def module: ModuleInstance = this match
    case Wasm(mod, _, _, _) => mod
    case Host(mod, _, _) => mod
    case Null() => ModuleInstance()

  override def toString: String =
    this match
      case Wasm(_, funcIx,_, tpe) => s"f$funcIx: ${toString(tpe)}"
      case Host(_, _, hostFun) => s"${hostFun.name}: ${toString(hostFun.funcType)}"

  private def toString(tpe: FuncType): String =
    s"${tpe.params.mkString("×")} -> ${tpe.t.mkString("×")}"

enum ExternalValue:
  case Function(addr: Int)
  case Table(addr: Int)
  case Memory(addr: Int)
  case Global(addr: Int)



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
    val elemSound = seqIsSound(using elemInstanceIsSound(using functionInstanceIsSoundFlat)).isSound(c.elements, a.elements)
    val datSound = summon[Soundness[Vector[DataInstance], Vector[DataInstance]]].isSound(c.data, a.data)
    val expSound = summon[Soundness[Vector[(String,ExternalValue)], Vector[(String,ExternalValue)]]].isSound(c.exports, a.exports)

    ftSound && fSound && tabSound && memSound && globSound && elemSound && datSound && expSound
    //ftSound && fSound && tabSound && memSound && globSound && datSound && expSound

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
    case (FunctionInstance.Host(_, _, chf), FunctionInstance.Host(_, _, ahf)) => summon[Soundness[HostFunction, HostFunction]].isSound(chf, ahf)
    case _ => IsSound.NotSound(s"Concrete function instance $c not approximated by $a.")
}

given functionInstancePO: PartialOrder[FunctionInstance] with
  override def lteq(c: FunctionInstance, a: FunctionInstance): Boolean = (c, a) match
    case (FunctionInstance.Wasm(_, _, cFunc, cFt), FunctionInstance.Wasm(_, _, aFunc, aFt)) =>
      cFunc == aFunc && cFt == aFt
    case (FunctionInstance.Host(_, _, chf), FunctionInstance.Host(_, _, ahf)) =>
      chf == ahf
    case (FunctionInstance.Null(), FunctionInstance.Null()) =>
      true
    case _ =>
      false

//case class TableInstance[V](tableType: TableType, functions: Vector[FunctionInstance[V]])
//case class GlobalInstance[V](tpe: ValType, val value: V)
case class DataInstance(data: ByteVector)
case class ElemInstance(functions: Seq[FunctionInstance], referenceType: ReferenceType, elemMode: ElemMode)

given elemModeIsSound: Soundness[ElemMode, ElemMode] with
  override def isSound(c: ElemMode, a: ElemMode): IsSound = if (c.equals(a)) Sound else NotSound("ElemMode mismatch")

given elemRefTypeIsSound: Soundness[ReferenceType, ReferenceType] with
  override def isSound(c: ReferenceType, a: ReferenceType): IsSound = if (c.equals(a)) Sound else NotSound(s"ElemInstance reference type mismatch: $c != $a")

given elemInstanceIsSound(using fSoundness: Soundness[FunctionInstance, FunctionInstance]): Soundness[ElemInstance, ElemInstance] with
  override def isSound(c: ElemInstance, a: ElemInstance): IsSound =
    seqIsSound.isSound(c.functions, a.functions) && summon[Soundness[ElemMode, ElemMode]].isSound(c.elemMode, a.elemMode) && summon[Soundness[ReferenceType, ReferenceType]].isSound(c.referenceType, a.referenceType)

//def mapFunctionInstance[A,B](f: A => B)(x: FunctionInstance[A]): FunctionInstance[B] = x match
//  case Wasm(mod, fun, ft) => Wasm(mod.mapModuleInstance(f), fun, ft)
