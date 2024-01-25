package sturdy.apron

import org.scalatest.funsuite.AnyFunSuite
import apron.*
import gmp.*
import org.scalatest.matchers.should.Matchers.*
import sturdy.values.Join

class ApronJoinTest extends AnyFunSuite:

  val x = StringVar("x")
  val y = StringVar("y")
  val z = StringVar("z")
  val inames = Array[Var](x,y,z)
  val fnames: Array[Var] = Array()
  val env = new Environment(inames, fnames)
  val manager: Manager = new Polka(false)
  val state1 = new Abstract1(manager, env)

  test("{x ∈ [0,10]} ⊔ {x ∈ [10,20]} = {x ∈ [0,20]}") {
    val state2 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(0, 10)).toIntern(env), null)
    val state3 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(10, 20)).toIntern(env), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(0,20)
  }

  test("{x ∈ [0,20]} ⊔ {x ∈ [10,10]} = {x ∈ [0,20]}") {
    val state2 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20)).toIntern(env), null)
    val state3 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(10, 20)).toIntern(env), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe false
    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
  }

  test("{x ∈ [0,20], y = x + 1} ⊔ {x ∈ [0,20], y = x + 2} = {x ∈ [0,20], x + 1 <= y <= x + 2}") {
    val state2 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20)).toIntern(env), null)
                       .assignCopy(manager, y, ApronExpr.Binary[StringVar](BinOp.Add, ApronExpr.Var(x), ApronExpr.Constant(Interval(1,1))).toIntern(env), null)
    val state3 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20)).toIntern(env), null)
                       .assignCopy(manager, y, ApronExpr.Binary(BinOp.Add, ApronExpr.Var(x),  ApronExpr.Constant(Interval(2,2))).toIntern(env), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
    joined.get.getBound(manager, y) shouldBe Interval(1, 22)
    println(joined.get)
  }


  test("{x ∈ [0,20]} ⊔ {y ∈ [0,20]} = {x ∈ [0,20], y ∈ [0,20]}") {
    val state2 = state1.assignCopy(manager, x, ApronExpr.Constant(Interval(0, 20)).toIntern(env), null)
    val state3 = state1.assignCopy(manager, y, ApronExpr.Constant(Interval(0, 20)).toIntern(env), null)
    val joined = Join(state2, state3)

    joined.hasChanged shouldBe true
    joined.get.getBound(manager, x) shouldBe Interval(0, 20)
    joined.get.getBound(manager, y) shouldBe Interval(0, 20)
  }


  case class StringVar(name: String) extends Var:
    override def compareTo(other: Var): Int =
      other match
        case otherStringVar: StringVar => name.compareTo(otherStringVar.name)
        case _ => -1

    override def clone(): Var = this

    override def toString: String = name
