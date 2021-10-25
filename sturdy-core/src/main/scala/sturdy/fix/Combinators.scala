package sturdy.fix

import sturdy.effect.TrySturdy

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

def filter[Dom, Codom](pred: Dom => Boolean, phi: Combinator[Dom, Codom]): Filter[Dom, Codom] = new Filter(pred, phi)
final class Filter[Dom, Codom](pred: Dom => Boolean, val phi: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    if (pred(dom))
      phi(f)(dom)
    else
      f(dom)
}

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

trait Logger[-Dom, -Codom]:
  def enter(d: Dom): Unit
  def exit(dom: Dom, codom: TrySturdy[Codom]): Unit
  final def &&[DDom <: Dom, CCodom <: Codom](other: Logger[DDom, CCodom]): Logger[DDom, CCodom] = new ProductLogger(this, other)

class ProductLogger[-Dom, -Codom](l1: Logger[Dom, Codom], l2: Logger[Dom, Codom]) extends Logger[Dom, Codom]:
  inline override def enter(d: Dom): Unit =
    l1.enter(d)
    l2.enter(d)

  inline override def exit(dom: Dom, codom: TrySturdy[Codom]): Unit =
    l1.exit(dom, codom)
    l2.exit(dom, codom)


def log[Dom, Codom](logger: Logger[Dom, Codom], phi: Combinator[Dom, Codom]): Log[Dom, Codom] = new Log(logger, phi)
final class Log[Dom, Codom](logger: Logger[Dom, Codom], val phi: Combinator[Dom, Codom]) extends Combinator[Dom, Codom] {
  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    logger.enter(dom)
    val codom = TrySturdy(phi(f)(dom))
    logger.exit(dom, codom)
    codom.get
}