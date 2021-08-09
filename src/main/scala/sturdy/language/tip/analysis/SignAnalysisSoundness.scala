package sturdy.language.tip.analysis

import sturdy.effect.allocation.{AllocationContextAbstractly, CAllocationIntIncrement}
import sturdy.values.Abstractly
import sturdy.language.tip.{ConcreteInterpreter, Function, given_Structural_Function}
import sturdy.language.tip.GenericInterpreter.AllocationSite
import sturdy.values.PartialOrder
import sturdy.{*, given}
import sturdy.util.{Label, given}
import sturdy.values.{*, given}
import sturdy.values.ints.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{*, given}

import SignAnalysis.*

object SignAnalysisSoundness:
  given addrAbstractly(using calloc: CAllocationIntIncrement[AllocationSite]): Abstractly[ConcreteInterpreter.Addr, PowAddr] =
    new AllocationContextAbstractly(calloc, fromAllocationSite)

  given valuesAbstractly(using Abstractly[ConcreteInterpreter.Addr, PowAddr]): Abstractly[ConcreteInterpreter.Value, Value] with
    override def abstractly(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.IntValue(d) => Value.IntValue(Abstractly.abstractly(d))
      case ConcreteInterpreter.Value.RefValue(caddr) => caddr match
        case None => Value.RefValue(Powerset(AllocationSiteRef.Null))
        case Some(ca) => Value.RefValue(Abstractly.abstractly(ca).pureMap(AllocationSiteRef.Addr.apply))
      case ConcreteInterpreter.Value.FunValue(fun) => Value.FunValue(Powerset(fun))

  given PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (_, Value.TopValue) => true
      case (Value.IntValue(i1), Value.IntValue(i2)) => PartialOrder[IntSign].lteq(i1, i2)
      case (Value.RefValue(r1), Value.RefValue(r2)) => PartialOrder[Refs].lteq(r1, r2)
      case (Value.FunValue(f1), Value.FunValue(f2)) => PartialOrder[Powerset[Function]].lteq(f1, f2)
      case _ => false

  given Soundness[ConcreteInterpreter, SignAnalysis] with
    def isSound(c: ConcreteInterpreter, a: SignAnalysis): IsSound = {
      given CAllocationIntIncrement[AllocationSite] = c.effectOps

      // concrete environment is sound by construction
      a.effectOps.storeIsSound(c.effectOps) &&
      a.effectOps.printIsSound(c.effectOps)
    }

