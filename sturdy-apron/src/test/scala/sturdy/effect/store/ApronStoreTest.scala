package sturdy.effect.store

import org.scalatest.funsuite.AnyFunSuite
import sturdy.values.references.Recency.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.data.{*, given}
import sturdy.effect.EffectStack
import sturdy.effect.store.{ApronStore, given}
import sturdy.apron.{ApronExpr, BinOp, given}
import sturdy.values.{*, given}
import sturdy.values.integer.{NumericInterval, NumericIntervalJoin, NumericIntervalWiden}
import sturdy.values.references.{*, given}
import apron.{Abstract1, Environment, Interval, MpqScalar, Polka}

class ApronRecencyStoreTest extends AnyFunSuite:

  type Context = String
  given Finite[Context] with {}

  type Addr = PhysicalAddress[Context]
  type PowAddr = PowersetAddr[Addr, Addr]
  type VAddr = VirtualAddress[Context]
  type PowVAddr = PowersetAddr[VAddr, VAddr]

  type ApAddr = ApronPhysicalAddress[Context]

  // The one from ApronStore seems to restrictive, as PAddr here isn't a subtype of apron.Var
  given JoinApronExpr(using abstract1: Abstract1): Join[ApronExpr[ApAddr]] with
    def apply(v1: ApronExpr[ApAddr], v2: ApronExpr[ApAddr]): MaybeChanged[ApronExpr[ApAddr]] =
      throw NotImplementedError()



  val man = new apron.Polka(true)
  given initialState: Abstract1 = new Abstract1(man, new Environment())

  test("basic case") {
    val AS = new ApronStore[Context, Addr, PowAddr, ApronExpr[ApAddr]](
      man,
      initialState,
      (v : ApronExpr[ApAddr]) => Option(v),
      (e: ApronExpr[ApAddr], s: Abstract1) => ApronExpr.Constant[ApAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )

    val x = PhysicalAddress("x", Recency.Recent)
    val xR = PowersetAddr(Set(x))
    val yR = PowersetAddr(Set(PhysicalAddress("y", Recency.Recent)))

    AS.write(xR, ApronExpr.Constant(Interval(0, 10)))
    println(s"$xR <- [0, 10] = ${AS.getState}")

    AS.write(yR, ApronExpr.Binary(BinOp.Add, ApronExpr.Var(x), ApronExpr.Constant(MpqScalar(1))))
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
      Addr,
      PowersetAddr[Addr, Addr],
      ApronExpr[ApAddr]
      ](
      man,
      initialState,
      (v : ApronExpr[ApAddr]) => Option(v),
      (e: ApronExpr[ApAddr], s: Abstract1) => ApronExpr.Constant[ApAddr](s.getBound(man, e.toIntern(s.getEnvironment)))
    )
    val AT = AddressTranslation.empty[Context]
    val RS = new RecencyStore[Context, PowVAddr, ApronExpr[ApAddr]](AS, AT)
  
  }
  
