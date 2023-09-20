package sturdy.language.tip.analysis

import sturdy.effect.allocation.{AllocationContextAbstractly, CAllocatorIntIncrement}
import sturdy.effect.store.{PowVirtualAddress, VirtualAddress, given}
import sturdy.language.tip.abstractions.References
import sturdy.language.tip.analysis.IntervalRecencyAnalysis.*
import sturdy.language.tip.{AllocationSite, ConcreteInterpreter, Field, Function}
import sturdy.util.{*, given}
import sturdy.values.Topped.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.relational.{*, given}
import sturdy.values.{*, given}
import sturdy.{*, given}

object IntervalRecencyAnalysisSoundness:
  given addrAbstractly: Abstractly[ConcreteInterpreter.Addr, AllocationSiteAddr] =
    c => References.allocationSiteAddr(c._1)

  given valuesAbstractly(using addr: AllocationSiteAddr => PowVirtualAddress[AllocationSiteAddr]): Abstractly[ConcreteInterpreter.Value, Value] with
    override def apply(c: ConcreteInterpreter.Value): Value = c match
      case ConcreteInterpreter.Value.TopValue => Value.TopValue
      case ConcreteInterpreter.Value.IntValue(d) => Value.IntValue(Abstractly.apply(d))
      case ConcreteInterpreter.Value.RefValue(caddr) => caddr match
        case Reference.Null => Value.RefValue(AbstractReference.Null)
        case Reference.Addr(caddr, m) =>
          val ctx = addrAbstractly(caddr)
          Value.RefValue(AbstractReference.Addr(addr(ctx), m))
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

  given interpSoundness(using AllocationSiteAddr => PowVirtualAddress[AllocationSiteAddr]): Soundness[ConcreteInterpreter.Instance, IntervalRecencyAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: IntervalRecencyAnalysis.Instance): IsSound = {
      // concrete environment is sound by construction
      a.store.isSound(c.store) &&
      a.print.isSound(c.print)
    }

