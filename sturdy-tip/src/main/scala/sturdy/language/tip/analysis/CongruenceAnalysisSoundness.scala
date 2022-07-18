package sturdy.language.tip.analysis

import sturdy.effect.allocation.{AllocationContextAbstractly, CAllocationIntIncrement}
import sturdy.values.Abstractly
import sturdy.language.tip.{ConcreteInterpreter, Function, given}
import sturdy.language.tip.{AllocationSite, Field}
import sturdy.values.PartialOrder
import sturdy.{*, given}
import sturdy.util.{*, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{*, given}

import CongruenceAnalysis.*

object CongruenceAnalysisSoundness:
  given addrAbstractly(using calloc: CAllocationIntIncrement[AllocationSite]): Abstractly[ConcreteInterpreter.Addr, Addr] =
    new AllocationContextAbstractly(calloc, fromAllocationSite)

  given valuesAbstractly(using Abstractly[ConcreteInterpreter.Addr, Addr]): Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.IntValue(d) => Value.IntValue(Abstractly.apply(d))
      case ConcreteInterpreter.Value.RefValue(caddr) => caddr match
        case None => Value.RefValue(Powerset(AllocationSiteRef.Null))
        case Some(ca) => Value.RefValue(Abstractly.apply(ca).map(AllocationSiteRef.Addr.apply))
      case ConcreteInterpreter.Value.FunValue(fun) => Value.FunValue(Powerset(fun))
      case ConcreteInterpreter.Value.RecValue(rec) => Value.RecValue(ARecord.Map(rec.view.mapValues(v => apply(v)).toMap))

  given po: PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (_, Value.TopValue) => true
      case (Value.IntValue(i1), Value.IntValue(i2)) => PartialOrder[VInt].lteq(i1, i2)
      case (Value.RefValue(r1), Value.RefValue(r2)) => PartialOrder[VRef].lteq(r1, r2)
      case (Value.FunValue(f1), Value.FunValue(f2)) => PartialOrder[Powerset[Function]].lteq(f1, f2)
      case (Value.RecValue(r1), Value.RecValue(r2)) => PartialOrder[ARecord[Field, Value]].lteq(r1, r2)
      case _ => false
  given Lazy[PartialOrder[Value]] = lazily(po)

  given Soundness[ConcreteInterpreter.Instance, CongruenceAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: CongruenceAnalysis.Instance): IsSound = {
      given CAllocationIntIncrement[AllocationSite] = c.alloc

      // concrete environment is sound by construction
      a.store.storeIsSound(c.store) &&
      a.print.isSound(c.print)
    }