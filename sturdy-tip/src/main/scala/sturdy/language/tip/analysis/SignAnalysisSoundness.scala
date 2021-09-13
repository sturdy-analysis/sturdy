package sturdy.language.tip.analysis

import sturdy.effect.allocation.{AllocationContextAbstractly, CAllocationIntIncrement}
import sturdy.values.Abstractly
import sturdy.language.tip.{ConcreteInterpreter, Function, given}
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.values.PartialOrder
import sturdy.{*, given}
import sturdy.util.{*, given}
import sturdy.values.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{*, given}

import SignAnalysis.*

object SignAnalysisSoundness:
  given addrAbstractly(using calloc: CAllocationIntIncrement[AllocationSite]): Abstractly[ConcreteInterpreter.Addr, Addr] =
    new AllocationContextAbstractly(calloc, fromAllocationSite)

  given valuesAbstractly(using Abstractly[ConcreteInterpreter.Addr, Addr]): Abstractly[ConcreteInterpreter.Value, Value] with
    override def abstractly(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.IntValue(d) => Value.IntValue(Abstractly.abstractly(d))
      case ConcreteInterpreter.Value.RefValue(caddr) => caddr match
        case None => Value.RefValue(Powerset(AllocationSiteRef.Null))
        case Some(ca) => Value.RefValue(Abstractly.abstractly(ca).pureMap(AllocationSiteRef.Addr.apply))
      case ConcreteInterpreter.Value.FunValue(fun) => Value.FunValue(Powerset(fun))
      case ConcreteInterpreter.Value.RecValue(rec) => Value.RecValue(ARecord.Map(rec.view.mapValues(v => abstractly(v)).toMap))

  given po: PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (_, Value.TopValue) => true
      case (Value.IntValue(i1), Value.IntValue(i2)) => PartialOrder[IntSign].lteq(i1, i2)
      case (Value.RefValue(r1), Value.RefValue(r2)) => PartialOrder[VRef].lteq(r1, r2)
      case (Value.FunValue(f1), Value.FunValue(f2)) => PartialOrder[Powerset[Function]].lteq(f1, f2)
      case (Value.RecValue(r1), Value.RecValue(r2)) => PartialOrder[ARecord[String, Value]].lteq(r1, r2)
      case _ => false
  given Lazy[PartialOrder[Value]] = lazily(po)

  given Soundness[ConcreteInterpreter.Instance, SignAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: SignAnalysis.Instance): IsSound = {
      given CAllocationIntIncrement[AllocationSite] = c.effects

      // concrete environment is sound by construction
      a.effects.storeIsSound(c.effects) &&
      a.effects.printIsSound(c.effects)
    }

