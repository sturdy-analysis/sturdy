package sturdy.language.whilelang.analysis

import sturdy.effect.allocation.AllocationContextAbstractly
import sturdy.values.Abstractly
import sturdy.language.whilelang.ConcreteInterpreter
import sturdy.values.PartialOrder
import sturdy.{*, given}
import sturdy.util.{Label, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.doubles.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{*, given}

import SignAnalysis.*

object SignAnalysisSoundness:
  given Abstractly[ConcreteInterpreter.Value, Value] with
    override def abstractly(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.BooleanValue(b) => Value.BooleanValue(Abstractly.abstractly(b))
      case ConcreteInterpreter.Value.DoubleValue(d) => Value.DoubleValue(Abstractly.abstractly(d))

  given PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (Value.BooleanValue(b1), Value.BooleanValue(b2)) => PartialOrder[Topped[Boolean]].lteq(b1, b2)
      case (Value.DoubleValue(d1), Value.DoubleValue(d2)) => PartialOrder[DoubleSign].lteq(d1, d2)
      case _ => false

  given Soundness[ConcreteInterpreter, SignAnalysis] with
    def isSound(c: ConcreteInterpreter, a: SignAnalysis): IsSound = {

      given Abstractly[ConcreteInterpreter.Addr, PowAddr] =
        new AllocationContextAbstractly(c.effectOps, a => Powerset(AllocationSiteAddr.Alloc(a.label)(true)))

      a.effectOps.environmentIsSound(c.effectOps) &&
      a.effectOps.storeIsSound(c.effectOps)
    }

