package sturdy.language.tip_xdai.record

import sturdy.data.MayJoin
import sturdy.effect.Effect
import sturdy.effect.allocation.Allocator
import sturdy.effect.store.Store
import sturdy.language.tip_xdai.core.*
import sturdy.language.tip_xdai.references.UnboundAddr
import sturdy.values.records.RecordOps
import sturdy.values.references.ReferenceOps

case class RecordSite(r: Record) extends AllocationSite

trait GenericInterpreter[V, Addr, J[_] <: MayJoin[_]] extends CoreGenericInterpreter[V, J]:
  val recOps: RecordOps[Field, V, V]

  val refOps: ReferenceOps[Addr, V]
  val store: Store[Addr, V, J]
  val alloc: Allocator[Addr, AllocationSite]

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
    case r@Record(fields) =>
      // represents record as a reference to a record value
      val fieldVals = fields.map(fe => Field(fe._1) -> eval(fe._2))
      val rec = recOps.makeRecord(fieldVals)
      val addr = alloc(RecordSite(r))
      store.write(addr, rec)
      refOps.mkManagedRef(addr)
    case FieldAccess(rec, field) =>
      val addr = refOps.deref(eval(rec))
      val recVal = store.read(addr).getOrElse(failure(UnboundAddr, addr.toString))
      recOps.lookupRecordField(recVal, Field(field))
    case _ => super.eval_open(e)

  override def assign(lhs: Assignable, v: V)(using Fixed): Unit = lhs match
    case AField(recVar, field) =>
      val recRef = eval(Var(recVar))
      val recAddr = refOps.deref(recRef)
      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
      val updated = recOps.updateRecordField(recVal, Field(field), v)
      store.write(recAddr, updated)
    case ADerefField(rec, field) =>
      val recRef = eval(rec)
      val recAddr = refOps.deref(recRef)
      val recVal = store.read(recAddr).getOrElse(failure(UnboundAddr, recAddr.toString))
      val updated = recOps.updateRecordField(recVal, Field(field), v)
      store.write(recAddr, updated)
    case _ => super.assign(lhs, v)