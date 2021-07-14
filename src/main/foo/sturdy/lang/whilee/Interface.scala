package sturdy.lang.whilee

import sturdy.common.Label
import sturdy.common.order.{CompleteFun, CompleteVal}
import sturdy.lang.whilee.Syntax._

trait HasRandomGen[R] {
  def nextRandom: R
}

trait HasStore[S] extends CompleteFun {
  def getStore: S
  def putStore(st: S): Unit

  def modifyStore(f: S => S): Unit = {
    val st = getStore
    putStore(f(st))
  }

  val completeStore: CompleteVal[S]

  override abstract def join[A, B](c: CompleteVal[B], f: A => B, g: A => B): A => B = {
    val original = getStore
    var fStore: S = original
    val joined = super.join(c, {
      val b = f
      fStore = getStore
      b
    },{
      putStore(original)
      g
    })
    putStore(completeStore.join(fStore, getStore))
    joined
  }
}

trait HasProp[P] {
  def getProp: P
  def putProp(pr: P): Unit

  def modifyProp(f: P => P): Unit = {
    val pr = getProp
    putProp(f(pr))
  }
}

trait Eval[V] {
  def lookup(x: String, l: Label): V
  def boolLit(b: Boolean, l: Label): V
  def and(e1: V, e2: V, l: Label): V
  def not(e: V, l: Label): V
  def numLit(n: Double, l: Label): V
  def randomNum(l: Label): V
  def add(e1: V, e2: V, l: Label): V
  def sub(e1: V, e2: V, l: Label): V
  def mul(e1: V, e2: V, l: Label): V
  def div(e1: V, e2: V, l: Label): V
  def eq(e1: V, e2: V, l: Label): V
  def fixEval(f: (Expr => V) => (Expr => V)): Expr => V
}

trait Run[V] {
  def assign(s: String, e: V, l: Label): Unit
  def if_(cond: V, thn: =>Unit, els: =>Unit, l: Label): Unit
}

trait RunFix[In, Out] {
  def fix(f: (In => Out) => (In => Out)): In => Out
}

trait CanFail[E] {
  def fail[A](e: E): A
}