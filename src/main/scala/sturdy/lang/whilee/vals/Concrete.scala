package sturdy.lang.whilee.vals

import sturdy.common.Label
import sturdy.lang.whilee.Syntax.Expr
import sturdy.lang.whilee._
import sturdy.lang.whilee.vals.Concrete._

object Concrete {
  sealed trait Val
  case class BoolVal(b: Boolean) extends Val
  case class NumVal(d: Double) extends Val

  type Store = Map[String, Val]
  type LiftedStore = Map[String, Set[Val]]

  def liftStore(st: Store): LiftedStore =
    st.mapValues(Set(_)).toMap
}

trait ConcreteEval extends
  Eval[Val] with
  HasStore[Store] with
  CanFail[String] with
  HasRandomGen[Double] {

  override def lookup(x: String, l: Label): Val = getStore(x)
  override def boolLit(b: Boolean, l: Label): Val = BoolVal(b)
  override def and(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (BoolVal(b1), BoolVal(b2)) => BoolVal(b1 && b2)
    case _ => fail("Expected two booleans as arguments for 'and'")
  }
  override def not(e: Val, l: Label): Val = e match {
    case BoolVal(b) => BoolVal(!b)
    case _ => fail("Expected a boolean as argument for 'not'")
  }
  override def numLit(d: Double, l: Label): Val = NumVal(d)
  override def randomNum(l: Label): Val = NumVal(nextRandom)
  override def add(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(d1),NumVal(d2)) => NumVal(d1+d2)
    case _ => fail("Expected two numbers as arguments for 'add'")
  }
  override def sub(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(d1),NumVal(d2)) => NumVal(d1-d2)
    case _ => fail("Expected two numbers as arguments for 'sub'")
  }
  override def mul(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(d1),NumVal(d2)) => NumVal(d1*d2)
    case _ => fail("Expected two numbers as arguments for 'mul'")
  }
  override def div(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (NumVal(d1),NumVal(d2)) => NumVal(d1/d2)
    case _ => fail("Expected two numbers as arguments for 'div'")
  }
  override def eq(e1: Val, e2: Val, l: Label): Val = (e1,e2) match {
    case (BoolVal(b1),BoolVal(b2)) => BoolVal(b1 == b2)
    case (NumVal(d1), NumVal(d2)) => BoolVal(d1 == d2)
    case _ => fail("Expected two values of the same type as arguments for 'eq'")
  }
  override def fixEval(f: (Expr => Val) => Expr => Val): Expr => Val = f(fixEval(f))
}

trait ConcreteRun extends
  Run[Val] with
  HasStore[Store] with
  CanFail[String] {

  override def assign(x: String, e: Val, l: Label): Unit =
    modifyStore(_.updated(x,e))
  override def if_(cond: Val, thn: =>Unit, els: =>Unit, l: Label): Unit = cond match {
    case BoolVal(b) => if (b) thn else els
    case _ => fail("Expected boolean as argument for 'if'")
  }
}

//class ConcreteInterpreter extends Interpreter[Val] {
//  val a =
//    new ConcreteEval with
//        ConcreteRun with
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
