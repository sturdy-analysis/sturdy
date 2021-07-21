package sturdy.language.whilelang.analysis

import sturdy.values.Abstractly
import sturdy.language.whilelang.ConcreteInterpreter
import sturdy.values.PartialOrder
import sturdy.{*, given}
import sturdy.util.{Label, given}
import sturdy.values.{*, given}
import sturdy.values.booleans.{*, given}
import sturdy.values.doubles.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{Topped, given}
import sturdy.values.Topped.{*, given}

import IntervalAnalysis.*

object IntervalAnalysisSoundness:
  given Abstractly[ConcreteInterpreter.Value, Value] with
    override def abstractly(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.BooleanValue(b) => Value.BooleanValue(Abstractly.abstractly(b))
      case ConcreteInterpreter.Value.DoubleValue(d) => Value.DoubleValue(Abstractly.abstractly(d))

  given PartialOrder[Value] with
    override def lteq(x: Value, y: Value): Boolean = (x, y) match
      case (Value.BooleanValue(b1), Value.BooleanValue(b2)) => PartialOrder[Topped[Boolean]].lteq(b1, b2)
      case (Value.DoubleValue(d1), Value.DoubleValue(d2)) => PartialOrder[Topped[DoubleInterval]].lteq(d1, d2)
      case _ => false

  given Soundness[ConcreteInterpreter, IntervalAnalysis] with
    def isSound(c: ConcreteInterpreter, a: IntervalAnalysis): IsSound = {
      given Soundness[ConcreteInterpreter.Addr, Addr] with
        override def isSound(caddr: ConcreteInterpreter.Addr, aaddr: Addr): IsSound =
          c.effectOps.read(caddr, Some.apply, None) match
            case None => IsSound.Sound
            case Some(cv) =>
              val avs = a.effectOps.read(aaddr, Powerset(_), Powerset.empty)
              Soundness.isSound(cv, avs)

      given Abstractly[ConcreteInterpreter.Addr, Addr] with
        override def abstractly(caddr: ConcreteInterpreter.Addr): Addr =
          val xs = c.effectOps.getEnv.filter(kv => kv._2 == caddr).keySet
          val aaddrs = xs.flatMap(x => a.effectOps.getEnv.get(x).map(_._2.set).getOrElse(Set()))
          Powerset(aaddrs)

      a.effectOps.environmentIsSound(c.effectOps) &&
      a.effectOps.storeIsSound(c.effectOps)
    }

