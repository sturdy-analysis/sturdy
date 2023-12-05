package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.ApronStore
import sturdy.apron.{ApronExpr, BinOp}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Polka, Abstract1, Interval, MpqScalar}

class ApronRecencyStoreTest extends AnyFunSuite:

  type Context = String
  type PAddr = ApronPAWrap[Context]

  test("basic case") {
    val AS = new ApronStore[Context, ApronExpr[PAddr]](
        new apron.Polka(true),
        (v : ApronExpr[PAddr]) => Option(v), 
        (e: ApronExpr[PAddr], s: Abstract1) => /* TODO: get itv overapproximation of e in s */ throw new NotImplementedError("getVal")
    )
    val xR : PAddr = ApronPAWrap(PhysicalAddress("x", Recency.Recent))
    val yR : PAddr = ApronPAWrap(PhysicalAddress("y", Recency.Recent))

    AS.write(xR, ApronExpr.Constant(Interval(0, 10)))
    println(xR.toString + " <- [0, 10] = " + AS.getState.toString)

//
    AS.write(yR, ApronExpr.Binary(BinOp.Add, ApronExpr.Var(xR), ApronExpr.Constant(MpqScalar(1))))
    println(yR.toString + " <- " + xR.toString + " + 1 = " + AS.getState.toString)
 
    
    // AS.read(yR) ~> should just provide the corresponding interval, non-relational read shouldn't happen much
  }