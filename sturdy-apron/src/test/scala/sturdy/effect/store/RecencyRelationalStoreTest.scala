package sturdy.effect.store

import apron.{Abstract1, Environment, Interval}
import sturdy.apron.{ApronExpr, RelationalExpr, given}
import sturdy.effect.EffectStack
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.{Finite, Widen}
import sturdy.values.integer.{BaseTypeIntegerOps, NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import sturdy.values.types.{BaseType, given}


type Ctx = String
given Finite[Ctx] with {}

type VAddr = VirtualAddress[Ctx]
type PowVAddr = PowVirtualAddress[Ctx]
type PAddr = PhysicalAddress[Ctx]
type PowPAddr = PowersetAddr[PAddr, PAddr]
type ApAddr = PhysicalAddress[Ctx]
given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)
class RecencyRelationalStoreTest extends RecencyAbstractionTest({
  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(failure)
  given Finite[FailureKind] with {}

  var relationalStore: RelationalStore[Ctx, BaseType[Int], PowPAddr, NumericInterval[Int]] = null

  given RelationalExpr[NumericInterval[Int], PhysicalAddress[Ctx], BaseType[Int]] with
    override def getRelationalExpr(v: NumericInterval[Int]): Option[ApronExpr[PhysicalAddress[Ctx], BaseType[Int]]] =
      Option(ApronExpr.intInterval(v.low, v.high, BaseType[Int]))
    override def makeRelationalExpr(expr: ApronExpr[PhysicalAddress[Ctx], BaseType[Int]]): NumericInterval[Int] =
      val iv = relationalStore.getBound(expr)
      val d = Array[Double](0)
      iv.inf().toDouble(d, 0)
      val lower = d(0).intValue()
      iv.sup().toDouble(d, 0)
      val upper = d(0).intValue()
      NumericInterval(lower, upper)

  relationalStore = new RelationalStore(man, initialState, Map())

  val addressTranslation = AddressTranslation.empty[Ctx]
  new RecencyStore[Ctx, VAddr, NumericInterval[Int]](relationalStore, addressTranslation)
})
