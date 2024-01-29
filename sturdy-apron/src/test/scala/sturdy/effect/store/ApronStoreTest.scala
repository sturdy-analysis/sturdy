package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*

import sturdy.values.references.Recency.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.{ApronStore, given}
import sturdy.apron.{ApronExpr, BinOp, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}

class ApronStoreTest extends AnyFunSuite:

  type Context = String
  given Finite[Context] with {}

  type Addr = PhysicalAddress[Context]
  type PowAddr = PowersetAddr[Addr, Addr]
  type VAddr = VirtualAddress[Context]
  type PowVAddr = PowVirtualAddress[Context]
  type ApAddr = ApronPhysicalAddress[Context]

  // The one from ApronStore seems to restrictive, as PAddr here isn't a subtype of apron.Var
  given JoinApronExpr(using abstract1: Abstract1): Join[ApronExpr[ApAddr]] with
    def apply(v1: ApronExpr[ApAddr], v2: ApronExpr[ApAddr]): MaybeChanged[ApronExpr[ApAddr]] =
      throw NotImplementedError()



  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  test("Retire a recent address") {
    val apronStore = new ApronStore[Context, Addr, PowAddr, ApronExpr[ApAddr]](
      man,
      initialState,
      (v : ApronExpr[ApAddr]) => Option(v),
      (e: ApronExpr[ApAddr], s: Abstract1) => ApronExpr.Constant[ApAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )

    val xRecent = PhysicalAddress("x", Recency.Recent)
    val xOld = PhysicalAddress("x", Recency.Old)

    apronStore.write(PowersetAddr(xRecent), ApronExpr.Constant(Interval(0, 10)))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(0,10)))

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(0,10)))

    apronStore.write(PowersetAddr(xRecent), ApronExpr.Constant(Interval(15, 20)))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(15,20)))

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(0, 20)))
  }

  // TODO: x = [0, 10], if (x >= 15)
  // I think we need more than the ApronStore to do that?

  // TODO RecencyStore+ApronStore
  test("with recencystore") {
    val apronStore = new ApronStore[
      Context,
      Addr,
      PowersetAddr[Addr, Addr],
      ApronExpr[ApAddr]
      ](
      man,
      initialState,
      (v : ApronExpr[ApAddr]) => Option(v),
      (e: ApronExpr[ApAddr], s: Abstract1) => ApronExpr.Constant[ApAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )
    val addressTranslation = AddressTranslation.empty[Context]
    val recencyStore = new RecencyStore[Context, PowVAddr, ApronExpr[ApAddr]](apronStore, addressTranslation)
  
    val x = recencyStore.alloc("x")
    val xPow = PowVirtualAddress(x)
    val yPow = PowVirtualAddress(recencyStore.alloc("y"))

    recencyStore.write(xPow, ApronExpr.Constant(Interval(0, 10)))
    recencyStore.read(xPow) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(0,10)))

    val xVs = addressTranslation(x)
    xVs.reduce(x =>
      // ApronExpr already works on virtual addresses here...
      recencyStore.write(yPow, ApronExpr.Binary(BinOp.Add, ApronExpr.Var(x), ApronExpr.Constant(MpqScalar(1))))
      recencyStore.read(yPow) shouldBe JOptionA.Some(ApronExpr.Constant(Interval(1,11)))
      ()
    )
  }
  
