package sturdy.language.tip_xdai.references.concrete

import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.language.tip_xdai.core.*
import sturdy.language.tip_xdai.references.GenericInterpreter
import sturdy.language.tip_xdai.references.UnboundAddr
import sturdy.values.Finite
import sturdy.values.integer.IntegerOps
import sturdy.values.ordering.OrderingOps
import sturdy.values.records.{RecordOps, UnboundRecordField}
import sturdy.values.references.{NullDereference, Reference, ReferenceOps}

case class RecordSite(r: Record) extends AllocationSite

case class Field(name: String)
given Finite[Field] with {}

trait ConcreteInterpreter extends GenericInterpreter[Value, ConcreteAddr, NoJoin]:
  private def unlift(v: Value): RefV = v match
    case r: RefV => r
    case _ => failure(TypeError, s"Expected Reference but got $this")
  
  override val refOps: ReferenceOps[ConcreteAddr, Value] = new ReferenceOps[ConcreteAddr, Value]:
    def mkNullRef: Value = RefV(Reference.Null)
    def mkRef(trg: ConcreteAddr): Value = RefV(Reference.Addr(trg, managed = false))
    def mkManagedRef(trg: ConcreteAddr): Value = RefV(Reference.Addr(trg, managed = true))
    def deref(v: Value): ConcreteAddr = unlift(v).ref.getOrElse(failure.fail(NullDereference, ""))