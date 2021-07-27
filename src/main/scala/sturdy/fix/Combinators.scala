package sturdy.fix

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
  override def apply(f: Dom => Codom): Dom => Codom = (dom: Dom) =>
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