package sturdy.fix

object Fixpoint:
  def apply[Dom, Codom](f: (Dom => Codom) => (Dom => Codom)): Dom => Codom =
    f(dom => apply(f)(dom))
