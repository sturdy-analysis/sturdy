package sturdy.effect.store

import apron.{Abstract1, Environment, Interval}
import sturdy.apron.ApronExpr
import sturdy.values.{Finite, Widen}
import sturdy.values.integer.{NumericInterval, NumericIntervalWiden, given}
import sturdy.values.references.{AddressTranslation, PhysicalAddress, PowVirtualAddress, PowersetAddr, VirtualAddress}


type Ctx = String
given Finite[Ctx] with {}

type VAddr = VirtualAddress[Ctx]
type PowVAddr = PowVirtualAddress[Ctx]
type PAddr = PhysicalAddress[Ctx]
type PowPAddr = PowersetAddr[PAddr, PAddr]
type ApAddr = ApronPhysicalAddress[Ctx]
given Widen[NumericInterval[Int]] = NumericIntervalWiden[Int](Set(10, 20, 30, 40, 50, 60, 70, 80, 90), 0, 100)
class ApronRecencyStoreTest extends RecencyAbstractionTest({
  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  val apronStore = new ApronStore[Ctx, PAddr, PowPAddr, NumericInterval[Int]](
    man,
    initialState,
    (v: NumericInterval[Int]) => Option(ApronExpr.Constant(Interval(v.low, v.high))),
    (e: ApronExpr[ApAddr], s: Abstract1) =>
      val iv = s.getBound(man, e.toIntern(s.getEnvironment))
      val d = Array[Double](0)
      iv.inf().toDouble(d, 0)
      val lower = d(0).intValue()
      iv.sup().toDouble(d, 0)
      val upper = d(0).intValue()
      NumericInterval(lower, upper)
  )

  val addressTranslation = AddressTranslation.empty[Ctx]
  new RecencyStore[Ctx, VAddr, NumericInterval[Int]](apronStore, addressTranslation)
})
