package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.ApronStore
import sturdy.apron.{ApronExpr, BinOp, JoinApronExpr, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}

class ApronRecencyStoreTest extends AnyFunSuite:

  type Context = String
  given Finite[Context] with {}

  type PAddr = ApronPhysicalAddress[Context]
  type PowAddr[Context] = PowersetAddr[PAddr, PAddr]

  // The one from ApronStore seems to restrictive, as PAddr here isn't a subtype of apron.Var
  given JoinApronExpr[PAddr](using abstract1: Abstract1): Join[ApronExpr[PAddr]] with
    def apply(v1: ApronExpr[PAddr], v2: ApronExpr[PAddr]): MaybeChanged[ApronExpr[PAddr]] =
      throw NotImplementedError()



  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  test("basic case") {
    val AS = new ApronStore[Context, PAddr, PAddr, ApronExpr[PAddr]](
      man,
      initialState,
      (v : ApronExpr[PAddr]) => Option(v),
      (e: ApronExpr[PAddr], s: Abstract1) => ApronExpr.Constant[PAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )

    val xR : PAddr = PhysicalAddress("x", Recency.Recent)
    val yR : PAddr = PhysicalAddress("y", Recency.Recent)

    AS.write(xR, ApronExpr.Constant(Interval(0, 10)))
    println(s"$xR <- [0, 10] = ${AS.getState}")

    AS.write(yR, ApronExpr.Binary(BinOp.Add, ApronExpr.Var(xR), ApronExpr.Constant(MpqScalar(1))))
    println(s"$yR <- $xR + 1 = ${AS.getState}")

    val ry = AS.read(yR)
    println(s"Read $yR: $ry") // non-relational read is expected here, it shouldn't happen much.
  }

  // TODO: x = [0, 10], if (x >= 15)
  // I think we need more than the ApronStore to do that?

  // TODO RecencyStore+ApronStore
  test("with recencystore") {
    val AS = new ApronStore[
      Context, 
      PAddr, 
      PowersetAddr[PAddr, PAddr], 
      ApronExpr[PAddr]
      ](
      man,
      initialState,
      (v : ApronExpr[PAddr]) => Option(v),
      (e: ApronExpr[PAddr], s: Abstract1) => ApronExpr.Constant[PAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )
    val RS = new RecencyStore[Context, PAddr, ApronExpr[PAddr]](AS)
  
  }
  
