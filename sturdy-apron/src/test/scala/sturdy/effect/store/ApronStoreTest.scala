package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import sturdy.values.references.Recency.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.{ApronStore, given}
import sturdy.apron.{ApronCons, ApronExpr, ApronVar, BinOp, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden, TypeIntegerOps}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.types.{BaseType, given}

class ApronStoreTest extends AnyFunSuite:

  type Context = String
  type Addr = PhysicalAddress[Context]
  type PowAddr = PowersetAddr[Addr, Addr]
  type VAddr = VirtualAddress[Context]
  type PowVAddr = PowVirtualAddress[Context]
  type ApAddr = PhysicalAddress[Context]

  given Finite[Context] with {}
  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(List(failure))
  given Finite[FailureKind] with {}


  // The one from ApronStore seems to restrictive, as PAddr here isn't a subtype of apron.Var
  given JoinApronExpr[Type](using abstract1: Abstract1): Join[ApronExpr[ApAddr, Type]] with
    def apply(v1: ApronExpr[ApAddr, Type], v2: ApronExpr[ApAddr, Type]): MaybeChanged[ApronExpr[ApAddr, Type]] =
      throw NotImplementedError()



  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  test("Retire a recent address") {
    val apronStore = new ApronStore[Context, BaseType[Int], PowAddr, ApronExpr[ApAddr, BaseType[Int]]](
      man,
      initialState,
      Map(),
      (v : ApronExpr[ApAddr, BaseType[Int]]) => Option(v),
      (e: ApronExpr[ApAddr, BaseType[Int]], s: Abstract1) =>
        ApronExpr.Constant[ApAddr, BaseType[Int]](s.getBound(man, e.toIntern(s.getEnvironment)), BaseType[Int])
    )

    val xRecent = PhysicalAddress("x", Recency.Recent)
    val xOld = PhysicalAddress("x", Recency.Old)

    apronStore.write(PowersetAddr(xRecent), ApronExpr.intInterval(0, 10))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.Some(ApronExpr.intInterval(0,10))

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.intInterval(0,10))

    apronStore.write(PowersetAddr(xRecent), ApronExpr.intInterval(15, 20))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.Some(ApronExpr.intInterval(15,20))

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.intInterval(0, 20))

    apronStore.addConstraint(ApronCons.intLt[PhysicalAddress[Context],BaseType[Int]](ApronExpr.intLit(10), ApronExpr.addr(xOld, BaseType[Int])))
    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.intInterval(11, 20))
  }

  type AExpr = ApronExpr[ApAddr, BaseType[Int]]
  test("with recencystore") {
    val apronStore = new ApronStore[
      Context,
      BaseType[Int],
      PowersetAddr[Addr, Addr],
      ApronExpr[ApAddr, BaseType[Int]]
      ](
      man,
      initialState,
      Map(),
      (v : ApronExpr[ApAddr, BaseType[Int]]) => Option(v),
      (e: ApronExpr[ApAddr, BaseType[Int]], s: Abstract1) => ApronExpr.constant(s.getBound(man, e.toIntern(s.getEnvironment)), BaseType[Int])
    )
    val addressTranslation = AddressTranslation.empty[Context]
    val recencyStore = new RecencyStore[Context, PowVAddr, AExpr](apronStore, addressTranslation)
  
    val x = recencyStore.alloc("x")
    val xPow = PowVirtualAddress(x)
    val yPow = PowVirtualAddress(recencyStore.alloc("y"))

    recencyStore.write(xPow, ApronExpr.intInterval(0, 10))
    recencyStore.read(xPow) shouldBe JOptionA.Some(ApronExpr.intInterval(0,10))

    val xVs = addressTranslation(x)
    xVs.reduce(x =>
      // ApronExpr already works on virtual addresses here...
      recencyStore.write(yPow, ApronExpr.intAdd(ApronExpr.addr(x, BaseType[Int]), ApronExpr.intLit(1)))
      recencyStore.read(yPow) shouldBe JOptionA.Some(ApronExpr.intInterval(1,11))
      ()
    )
  }
  
