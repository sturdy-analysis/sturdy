package sturdy.fix

class CFixpoint[Dom, Codom] extends Fixpoint[Dom, Codom]:
  override def fix(f: (Dom => Codom) => Dom => Codom): Dom => Codom =
    f(dom => fix(f)(dom))
