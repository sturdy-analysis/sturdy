package stateful.whilelang

trait Fix[Dom, Codom] {
  def fix(f: (Dom => Codom) => Dom => Codom): Dom => Codom
}

trait FixImpl[Dom, Codom] extends Fix[Dom, Codom] {
  override def fix(f: (Dom => Codom) => Dom => Codom): Dom => Codom = {
    f(dom => fix(f)(dom))
  }
}
