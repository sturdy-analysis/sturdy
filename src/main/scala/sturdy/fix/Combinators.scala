package sturdy.fix

import scala.reflect.ClassTag

trait Combinator[Dom, Codom] extends Function[Dom => Codom, Dom => Codom]

def identity[Dom, Codom]: Identity[Dom, Codom] = new Identity
final class Identity[Dom, Codom] extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = f
}

def const[Dom, Codom](c: Dom => Codom): Const[Dom, Codom] = new Const(c)
final class Const[Dom, Codom](c: Dom => Codom) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = c
}

def filter[Dom, Codom, Phi <: Combinator[Dom, Codom]](pred: Dom => Boolean, phi: Phi): Filter[Dom, Codom, Phi] = new Filter(pred, phi)
final class Filter[Dom, Codom, Phi <: Combinator[Dom, Codom]](pred: Dom => Boolean, val phi: Phi) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    if (pred(dom))
      phi(f)(dom)
    else
      f(dom)
}

def unwind[Dom, Codom, Phi <: Combinator[Dom, Codom]](steps: Int, phi: Phi): Unwind[Dom, Codom, Phi] = new Unwind(steps, phi)
final class Unwind[Dom, Codom, Phi <: Combinator[Dom, Codom]](steps: Int, val phi: Phi) extends Combinator[Dom, Codom] {
  private var stepsLeft: Int = steps
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    stepsLeft -= 1
    if (stepsLeft > 0)
      f(dom)
    else
      phi(f)(dom)
}

def dispatch[Dom, Codom, Phi <: Combinator[Dom, Codom]]
  (choose: Dom => Int, phis: Iterable[Phi])
  (using ClassTag[Phi])
  : Dispatch[Dom, Codom, Phi] = new Dispatch(choose, phis.toArray)
final class Dispatch[Dom, Codom, Phi <: Combinator[Dom, Codom]](choose: Dom => Int, val phis: Array[Phi]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    val ix = choose(dom)
    if (ix >= 0)
      phis(ix)(f)(dom)
    else
      f(dom)
}

trait Logger[Dom]:
  def enter(d: Dom): Unit
  def exit(d: Dom): Unit

def log[Dom, Codom, Phi <: Combinator[Dom, Codom]](logger: Logger[Dom], phi: Phi): Log[Dom, Codom, Phi] = new Log(logger, phi)
final class Log[Dom, Codom, Phi <: Combinator[Dom, Codom]](logger: Logger[Dom], val phi: Phi) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    logger.enter(dom)
    try phi(f)(dom) finally
      logger.exit(dom)
}