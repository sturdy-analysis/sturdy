package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.matchers.should.Matchers.*
import sturdy.values.references.Recency.*
import sturdy.data.{*, given}
import sturdy.effect.{EffectStack, Stateless}
import sturdy.effect.store.{RelationalStore, given}
import sturdy.apron.{ApronCons, ApronExpr, ApronVar, BinOp, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden, BaseTypeIntegerOps}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}
import sturdy.effect.allocation.Allocator
import sturdy.effect.failure.{CollectedFailures, Failure, FailureKind}
import sturdy.values.types.{BaseType, given}

class RelationalStoreTest extends AnyFunSuite:

  type Context = String
  type Addr = PhysicalAddress[Context]
  type PowAddr = PowersetAddr[Addr, Addr]
  type VAddr = VirtualAddress[Context]
  type PowVAddr = PowVirtualAddress[Context]
  type ApAddr = PhysicalAddress[Context]
  type Type = BaseType[Int]
  type Val = ApronExpr[PhysicalAddress[Context], Type]
  val intType: Type = BaseType[Int]

  given Finite[Context] with {}
  given failure: Failure = new CollectedFailures[FailureKind]
  given effectState: EffectStack = EffectStack(failure)
  given Finite[FailureKind] with {}

  given Allocator[Context, Type] with Stateless with
    override def alloc(tpe: Type): Context =
      s"tmp_$tpe"
  given CombineVal[W <: Widening]: Combine[Val, W] = ???

  given apron.Manager = new apron.Polka(true)

  test("Retire a recent address") {
    val (recencyStore, apronStore) = RecencyRelationalStore[Context, Type].unapply

    val xRecent = PhysicalAddress("x", Recency.Recent)
    val xOld = PhysicalAddress("x", Recency.Old)

    apronStore.write(PowersetAddr(xRecent), ApronExpr.interval(0, 10, intType))
    apronStore.getBound(ApronExpr.addr(xRecent, intType)) shouldBe Interval(0,10)

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.getBound(ApronExpr.addr(xOld, intType)) shouldBe Interval(0,10)

    apronStore.write(PowersetAddr(xRecent), ApronExpr.interval(15, 20, intType))
    apronStore.getBound(ApronExpr.addr(xRecent, intType)) shouldBe Interval(15,20)

    apronStore.move(PowersetAddr(xRecent), PowersetAddr(xOld))
    apronStore.read(PowersetAddr(xRecent)) shouldBe JOptionA.None()
    apronStore.getBound(ApronExpr.addr(xOld, intType)) shouldBe Interval(0,20)

//    apronStore.addConstraint(ApronCons.intLt[PhysicalAddress[Context],BaseType[Int]](ApronExpr.intLit(10), ApronExpr.addr(xOld, BaseType[Int])))
//    apronStore.read(PowersetAddr(xOld)) shouldBe JOptionA.Some(ApronExpr.intInterval(11, 20))
  }

  type AExpr = ApronExpr[ApAddr, BaseType[Int]]
  test("with recencystore") {
    val (recencyStore, apronStore) = RecencyRelationalStore[Context, Type].unapply

    val x = recencyStore.alloc("x")
    val y = recencyStore.alloc("y")
    val xPow = PowVirtualAddress(x)
    val yPow = PowVirtualAddress(y)

    recencyStore.write(xPow, ApronExpr.interval(0, 10, intType))

    x.physical.reduce(xPhys =>
      // ApronExpr already works on virtual addresses here...
      apronStore.getBound(ApronExpr.addr(xPhys, intType)) shouldBe Interval(0, 10)
      recencyStore.write(yPow, ApronExpr.intAdd[Int,VAddr,Type](ApronExpr.addr(x, intType), ApronExpr.lit(1, intType)))
      y.physical.reduce(y =>
        apronStore.getBound(ApronExpr.addr(y,intType)) shouldBe Interval(1, 11)
        ()
      )
    )
  }
  
