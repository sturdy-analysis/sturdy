package sturdy.effect.store

import apron.{Abstract1, Environment, Interval}
import sturdy.apron.{ApronExpr, ApronExprConverter, ApronRecencyState, ApronState, StatefullRelationalExprT, StatelessRelationalExpr, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.util.{Lazy, lazily}
import sturdy.values.floating.FloatSpecials
import sturdy.values.{Finite, Widen}
import sturdy.values.integer.{BaseTypeIntegerOps, NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}


class RecencyRelationalStoreTest extends RecencyAbstractionTest({
  type Ctx = String
  given Finite[Ctx] with {}
  type Type = BaseType[Int]
  type VAddr = VirtualAddress[Ctx]
  type PowVAddr = PowVirtualAddress[Ctx]
  type PAddr = PhysicalAddress[Ctx]
  type PowPAddr = PowersetAddr[PAddr, PAddr]
  type ApAddr = PhysicalAddress[Ctx]
  type Value = NumericInterval[Int]
  given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)

  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(failure)
  given Finite[FailureKind] with {}

  var exprConverter: ApronExprConverter[Ctx, Type, Value] = null
  given lazyExprConverter: Lazy[ApronExprConverter[Ctx, Type, Value]] = lazily(exprConverter)

  given StatelessRelationalExpr[Value, VAddr, Type] with
    override def getRelationalExpr(v: Value): Option[ApronExpr[VAddr, Type]] =
      Option(ApronExpr.interval(v.low, v.high, BaseType[Int]))
    override def makeRelationalExpr(expr: ApronExpr[VAddr, Type]): Value =
      val iv = exprConverter.relationalStore.getBound(exprConverter.virtToPhys(expr))
      val d = Array[Double](0)
      iv.inf().toDouble(d, 0)
      val lower = d(0).intValue()
      iv.sup().toDouble(d, 0)
      val upper = d(0).intValue()
      NumericInterval(lower, upper)

    override def getMetaData(v: Value): Option[(FloatSpecials, Type)] =
      Some((FloatSpecials.Bottom, BaseType[Int]))

  given relationalValue: StatefullRelationalExprT[Value, PAddr, Type, RelationalStoreState[Ctx, Map[PAddr, Value]]] = RelationalValueApronExprPhysicalAddress[Value, Ctx, Type].asInstanceOf
  val relationalStore: RelationalStore[Ctx, Type, PowPAddr, Value] = new RelationalStore(Map(), man, initialState, Map())
  import relationalStore.given
  val recencyStore: RecencyStore[Ctx, PowVAddr, Value] = new RecencyStore[Ctx, PowVAddr, Value](relationalStore)
  exprConverter = ApronExprConverter(recencyStore, relationalStore)
  recencyStore
})
