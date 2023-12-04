package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.ApronStore
import sturdy.apron.{ApronExpr}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Polka, Abstract1, Interval}

class ApronRecencyStoreTest extends AnyFunSuite:

  type Context = String
  type PAddr = PhysicalAddress[Context]

  test("basic case") {
    val AS = new ApronStore[Context, NumericInterval[Int]](
        new apron.Polka(true),
        (v : NumericInterval[Int]) => Option(ApronExpr.Constant(Interval(v.low, v.high))), 
        (e: ApronExpr[PhysicalAddress[Context]], s: Abstract1) => /* TODO: get itv overapproximation of e in s */ throw new NotImplementedError("getVal")
    )
    val xR : PAddr = PhysicalAddress("x", Recency.Recent)
    val yR : PAddr = PhysicalAddress("y", Recency.Recent)

    AS.write(xR, NumericInterval(0, 10))
    println(xR.toString + " <- [0, 10] = " + AS.getState.toString)


    // AS.write(yR, ApronExpr(xR+1)) <- mmf, ApronExpr is not a value...
    
    // ApronCons handling? When do they happen?
    
    // AS.read(yR) ~> should just provide the corresponding interval, non-relational read shouldn't happen much
  }