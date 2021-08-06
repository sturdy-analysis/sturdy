package sturdy.fix

object Fixpoint:
  def apply[Dom, Codom](f: (Dom => Codom) => (Dom => Codom)): Dom => Codom =
    f(dom => Fixpoint(f)(dom))
  
  var DEBUG: Boolean = System.getProperty("STURDY_DEBUG_FIXPOINT", "true").toBoolean
