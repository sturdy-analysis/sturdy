package sturdy.language.tip_xdai.references

import sturdy.data.MayJoin
import sturdy.language.tip_xdai.core.*
import sturdy.language.tip_xdai.core.UnboundVariable
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.OrderingOps
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps
import sturdy.data.noJoin
import sturdy.effect.Effect
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.Store
import sturdy.values.Structural


trait AllocationSite
case class AllocSite(e: Alloc) extends AllocationSite
given Structural[AllocationSite] with {}

case object UnboundAddr extends TipFailure

trait GenericInterpreter[V, Addr, J[_] <: MayJoin[_]] extends CoreGenericInterpreter[V, J]:
  val store: Store[Addr, V, J]
  val alloc: Allocator[Addr, AllocationSite]

  val refOps: ReferenceOps[Addr, V]

  override def allEffects: Set[Effect] = super.allEffects ++ Set(alloc, store)

  override def inEffect(fixIn: Any): Set[Effect] = fixIn match
    case _: FixIn.Run | _: FixIn.EnterFunction => super.inEffect(fixIn) + store
    case _: FixIn.Eval => super.inEffect(fixIn) ++ Set(store, alloc)
    case _ => super.inEffect(fixIn)

  override def outEffect(fixIn: Any): Set[Effect] = fixIn match
    case _: FixIn.Run | _: FixIn.EnterFunction => super.outEffect(fixIn) + store
    case _: FixIn.Eval => super.outEffect(fixIn) + alloc
    case _ => super.outEffect(fixIn)

  override def eval_open(e: Exp)(using Fixed): V = e match
    case a@Alloc(e) =>
      val addr = alloc(AllocSite(a))
      store.write(addr, eval(e))
      refOps.mkManagedRef(addr)
    case Deref(e) =>
      val addr = refOps.deref(eval(e))
      val result = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
      result
    case NullRef() =>
      refOps.mkNullRef
    case VarRef(x) =>
      failure(VariableReferencesNotSupported, s"&$x")
    case _ => super.eval_open(e)

  override def assign(lhs: Assignable, v: V)(using Fixed): Unit = lhs match
    case AVar(x) =>
      callFrame.setLocalByName(x, v).getOrElse(failure(UnboundVariable, x))
    case ADeref(e) =>
      val addr = refOps.deref(eval(e))
      store.write(addr, v)
    case _ => super.assign(lhs, v)