package sturdy.fix

class AFixpointFuel[Dom, Codom](steps: Int, fallback: Dom => Codom) extends Fixpoint[Dom, Codom]:
  private var stepsLeft = steps
  override def fix(f: (Dom => Codom) => Dom => Codom): Dom => Codom =
    stepsLeft -= 1
    if (stepsLeft <= 0)
      fallback
    else
      f(dom => fix(f)(dom))
