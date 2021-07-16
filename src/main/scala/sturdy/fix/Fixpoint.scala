package sturdy.fix

trait Fixpoint[Dom, Codom] {
  type Fixed = Dom => Codom
  def fix(f: Fixed => Fixed): Fixed
}