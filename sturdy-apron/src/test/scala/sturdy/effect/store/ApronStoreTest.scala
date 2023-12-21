package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.ApronStore
import sturdy.apron.{ApronExpr, BinOp, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}

class ApronRecencyStoreTest extends AnyFunSuite:

  type Context = String
  type PAddr = ApronPhysicalAddress[Context]

  test("basic case") {
    val man = new apron.Polka(true)
    given initialState: Abstract1 = new Abstract1(man, new Environment())
    val AS = new ApronStore[Context, PAddr, PAddr, ApronExpr[PAddr]](
      man,
      initialState,
      (v : ApronExpr[PAddr]) => Option(v),
      (e: ApronExpr[PAddr], s: Abstract1) => ApronExpr.Constant(s.getBound(man, e.toIntern(s.getEnvironment)))
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
  /*
  test("with recencystore") {
    val man = new apron.Polka(true)
    val AS = new ApronStore[Context, ApronExpr[PAddr]](
          man,
          (v : ApronExpr[PAddr]) => Option(v), 
          (e: ApronExpr[PAddr], s: Abstract1) => ApronExpr.Constant(s.getBound(man, e.toIntern(s.getEnvironment)))
        )
    
  
  }
  */
