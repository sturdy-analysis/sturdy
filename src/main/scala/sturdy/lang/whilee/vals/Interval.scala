package sturdy.lang.whilee.vals

import sturdy.common.Label
import sturdy.common.order.CompleteFun
import sturdy.common.order.CompleteVal.CompleteUnit
import sturdy.lang.whilee.Syntax.Expr
import sturdy.lang.whilee._
import sturdy.lang.whilee.vals.Interval._

object Interval {
  sealed trait Val
  case object TopVal extends Val
  case class BoolVal(b: Bool) extends Val
  case class NumVal(n: Num) extends Val

  sealed trait Bool
  case object BotBool extends Bool
  case class B(b: Boolean) extends Bool

  sealed trait Num
  case object BotNum extends Num
  case class IV(l: Double, r: Double) extends Num {
    def +(y: IV): IV = IV(l + y.l, r + y.r)
    def -(y: IV): IV = IV(l + y.l, r + y.r)
  }

  type Store = Map[String, Val]
  type LiftedStore = Map[String, Set[Val]]

  def liftStore(st: Store): LiftedStore =
    st.mapValues(Set(_)).toMap
}

trait IntervalEval extends
  Eval[Val] with
  HasStore[Store] with
  CanFail[String] with
  HasRandomGen[Double] {

  override def lookup(x: String, l: Label): Val = getStore(x)
  override def boolLit(b: Boolean, l: Label): Val = BoolVal(B(b))
  override def and(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (BoolVal(BotBool), _) => BoolVal(BotBool)
    case (_,BoolVal(BotBool)) => BoolVal(BotBool)
    case (BoolVal(B(b1)),BoolVal(B(b2))) => BoolVal(B(b1 && b2))
    case _ => fail("Expected two booleans as arguments for 'and'")
  }
  override def not(e: Val, l: Label): Val = e match {
    case (BoolVal(BotBool)) => BoolVal(BotBool)
    case (BoolVal(B(b))) => BoolVal(B(!b))
    case _ => fail("Expected a boolean as argument for 'not'")
  }
  override def numLit(d: Double, l: Label): Val = NumVal(IV(d,d))
  override def randomNum(l: Label): Val = {
    val r = nextRandom
    NumVal(IV(r,r))
  }
  override def add(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(BotNum),_) => NumVal(BotNum)
    case (_,NumVal(BotNum)) => NumVal(BotNum)
    case (NumVal(IV(l1,r1)),NumVal(IV(l2,r2))) => NumVal(IV(l1+l2,r1+r2))
    case _ => fail("Expected two numbers as arguments for 'add'")
  }
  override def sub(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(BotNum),_) => NumVal(BotNum)
    case (_,NumVal(BotNum)) => NumVal(BotNum)
    case (NumVal(IV(l1,r1)),NumVal(IV(l2,r2))) => NumVal(IV(l1+l2,r1+r2))
    case _ => fail("Expected two numbers as arguments for 'sub'")
  }
  override def mul(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
//    case (NumVal(d1),NumVal(d2)) => NumVal(d1*d2)
    case _ => fail("Expected two numbers as arguments for 'mul'")
  }
  override def div(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
//    case (NumVal(d1),NumVal(d2)) => NumVal(d1/d2)
    case _ => fail("Expected two numbers as arguments for 'div'")
  }
  override def eq(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (BoolVal(BotBool),BoolVal(_)) => BoolVal(BotBool)
    case (BoolVal(_),BoolVal(BotBool)) => BoolVal(BotBool)
    case (BoolVal(B(b1)),BoolVal(B(b2))) => BoolVal(B(b1 == b2))

    case (NumVal(BotNum),NumVal(_)) => BoolVal(BotBool)
    case (NumVal(_),NumVal(BotNum)) => BoolVal(BotBool)
    case (NumVal(IV(l1,r1)), NumVal(IV(l2,r2))) if l1==r1 && l2==r2 => BoolVal(B(l1==l2))
    case (NumVal(_),NumVal(_)) => TopVal

    case _ => fail("Expected two values of the same type as arguments for 'eq'")
  }
  override def fixEval(f: (Expr => Val) => Expr => Val): Expr => Val = f(fixEval(f))
}

trait IntervalRun extends
  Run[Val] with
  HasStore[Store] with
  CanFail[String] with
  CompleteFun {

  override def assign(x: String, e: Val, l: Label): Unit =
    modifyStore(_.updated(x,e))
  override def if_(cond: Val, thn: =>Unit, els: =>Unit, l: Label): Unit = cond match {
    case BoolVal(B(b)) => if (b) thn else els
    case BoolVal(BotBool) => { }
    case TopVal => join[Unit,Unit](CompleteUnit, _=>thn, _=>els)()
    case _ => fail("Expected boolean as argument for 'if'")
  }
}

//class IntervalInterpreter extends Interpreter[Val] {
//  val a =
//    new IntervalEval with
//        IntervalRun with
//        StdRandomGen with
//        StdFail[String] with
//        StdStore[Store] with
//        StdRunFix[Statement,Unit] {
//    override val emptyStore: Store = Map()
//  }
//
//  def eval: Expr => Val = super.eval(a)
//  def run: Statement => Unit = super.run(a)
//}
