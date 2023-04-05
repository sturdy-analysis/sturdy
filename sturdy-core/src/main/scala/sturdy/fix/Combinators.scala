package sturdy.fix

import sturdy.effect.ObservableJoin
import sturdy.effect.TrySturdy
import sturdy.effect.except.ObservableExcept
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.fix.cfg.ControlLogger

import scala.reflect.ClassTag

trait Combinator[Dom, Codom] extends Function[Dom => Codom, Dom => Codom]:
  def fixpoint: CombinatorFixpoint[Dom, Codom] = new CombinatorFixpoint {
    override lazy val phi: Combinator[Dom, Codom] = Combinator.this
  }

  def compose(other: Combinator[Dom, Codom]): Combinator[Dom, Codom] =
    (f: Dom => Codom) => this(other(f))
  
trait ContextualCombinator[Ctx, Dom, Codom] extends Function[Contextual[Ctx, Dom] ?=> Dom => Codom, Dom => Codom]:
  type Context = Contextual[Ctx, Dom]

def identity[Dom, Codom]: Identity[Dom, Codom] = new Identity
final class Identity[Dom, Codom] extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = f
}

def const[Dom, Codom](c: Dom => Codom): Const[Dom, Codom] = new Const(c)
final class Const[Dom, Codom](c: Dom => Codom) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = c
}

def filter[Dom, Codom](pred: Dom => Boolean, phi: Combinator[Dom, Codom]): Filter[Dom, Codom] = new Filter(pred, phi)
final class Filter[Dom, Codom](pred: Dom => Boolean, val phi: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    if (pred(dom))
      phi(f)(dom)
    else
      f(dom)
}

val UnwindingProperty = "loop unwinding"

def unwind[Dom, Codom](steps: Int, phi: Combinator[Dom, Codom]): Unwind[Dom, Codom] = new Unwind(steps, phi)
final class Unwind[Dom, Codom](steps: Int, val phi: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  private var stepsLeft: Int = steps
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    stepsLeft -= 1
    if (stepsLeft > 0)
      f(dom)
    else
      phi(f)(dom)
}

def dispatch[Dom, Codom]
  (choose: Dom => Int, phis: Iterable[Combinator[Dom, Codom]])
  : Dispatch[Dom, Codom] = new Dispatch(choose, phis.toArray)
final class Dispatch[Dom, Codom](choose: Dom => Int, val phis: Array[Combinator[Dom, Codom]]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    val ix = choose(dom)
    if (ix >= 0)
      phis(ix)(f)(dom)
    else
      f(dom)
}

def conditional[Dom, Codom]
  (cond: Dom => Boolean, ifTrue: Combinator[Dom, Codom], ifFalse: Combinator[Dom, Codom])
  : Conditional[Dom, Codom] = new Conditional(cond, ifTrue, ifFalse)
final class Conditional[Dom, Codom](cond: Dom => Boolean, phiIfTrue: Combinator[Dom, Codom], phiIfFalse: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    val bool = cond(dom)
    if (bool)
      phiIfTrue(f)(dom)
    else
      phiIfFalse(f)(dom)
}

def control[Ctx, Dom, Codom, Exc, Node <: ControlFlowGraph.Node]
  (contextSensitive: Boolean, startNode: Node)
  (getDomNode: Dom => Option[Node])
  (getCodomNode: (Dom, Codom) => Option[Node])
  (using obsJoin: ObservableJoin, obsExcept: ObservableExcept[Exc])
  : ControlLogger[Ctx, Dom, Codom, Exc, Node] =
  new ControlLogger(contextSensitive, startNode, getDomNode, getCodomNode, obsJoin, obsExcept)
