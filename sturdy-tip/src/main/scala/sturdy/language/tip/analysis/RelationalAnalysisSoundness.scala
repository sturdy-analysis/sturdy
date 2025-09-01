package sturdy.language.tip.analysis

import sturdy.effect.allocation.{AllocationContextAbstractly, CAllocatorIntIncrement}
import sturdy.language.tip.analysis.RelationalAnalysis.*
import sturdy.language.tip.{AllocationSite, ConcreteInterpreter, Field, Function}
import sturdy.util.{*, given}
import sturdy.values.Topped.{*, given}
import sturdy.values.integer.{*, given}
import sturdy.values.ordering.{*, given}
import sturdy.values.records.{*, given}
import sturdy.values.references.{*, given}
import sturdy.language.tip.abstractions.References
import sturdy.values.{*, given}
import sturdy.{*, given}
import _root_.apron.*
import _root_.gmp.Mpz
import sturdy.apron.{ApronCons, ApronExpr, ApronRecencyState}

class RelationalAnalysisSoundness(analysis: RelationalAnalysis.Instance):
  import analysis.given
  val addrTrans = analysis.apronState.recencyStore.addressTranslation
  val apronState = analysis.apronState

  given Abstractly[ConcreteInterpreter.Addr, AddrCtx] = {
    case (AllocationSite.Alloc(e),_) => AddrCtx.Alloc(e)
    case (AllocationSite.Record(r),_) => AddrCtx.Alloc(r)
  }

  given Abstractly[ConcreteInterpreter.Addr, Addr] =
    (cAddr: ConcreteInterpreter.Addr) =>
      val ctx = Abstractly[ConcreteInterpreter.Addr, AddrCtx](cAddr)
      addrTrans.mapping.get(ctx) match
        case None => throw new IllegalStateException(s"Address Translation $addrTrans does not contain context $ctx")
        case Some(region) =>
          PowVirtualAddress(VirtualAddress(ctx, region.recent.max, addrTrans))

  given Soundness[ConcreteInterpreter.Value, RelationalAnalysis.Value] with
    def isSound(c: ConcreteInterpreter.Value, a: RelationalAnalysis.Value): IsSound =
      (c,a) match
        case (_, Value.TopValue) =>
          IsSound.Sound
        case (ConcreteInterpreter.Value.IntValue(i1), Value.IntValue(i2)) =>
          val s1 = MpqScalar(Mpz(i1))
          val iv1 = Interval(s1,s1)
          val iv2 = apronState.getInterval(i2)
          IsSound(iv1.isLeq(iv2), s"Abstract value $a with range $iv2 does not overapproximate concrete value $c")
        case (ConcreteInterpreter.Value.RefValue(r1), Value.RefValue(r2)) =>
          Soundness.isSound(r1, r2)
        case (ConcreteInterpreter.Value.FunValue(f1), Value.FunValue(f2)) =>
          IsSound(f2.set.contains(f1), s"function $f1 is not contained in abstract function value $f2")
        case (ConcreteInterpreter.Value.RecValue(r1), Value.RecValue(r2)) =>
          Soundness.isSound(r1, r2)
        case _ =>
          IsSound.NotSound(s"Abstract value $a does not overapproximate concrete value $c due to a type mismatch")

  given Soundness[ConcreteInterpreter.Instance, RelationalAnalysis.Instance] with
    def isSound(c: ConcreteInterpreter.Instance, a: RelationalAnalysis.Instance): IsSound =
      a.callFrame.isSound(c.callFrame) && a.store.isSound(c.store) && a.print.isSound(c.print)
      // concrete environment is sound by construction
//      a.store.isSound(c.store) &&
//      a.print.isSound(c.print) /* &&
//      a.assert.isSound(c.assert) */

