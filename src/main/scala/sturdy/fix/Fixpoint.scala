package sturdy.fix

trait Fixpoint[Dom, Codom] {
  def fix(f: (Dom => Codom) => Dom => Codom): Dom => Codom
}