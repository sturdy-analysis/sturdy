package sturdy.fix

object Fixpoint:
  def apply[Dom, Codom](f: (Dom => Codom) => (Dom => Codom)): Dom => Codom =
    f(dom => Fixpoint(f)(dom))
  
  val DEBUG: Boolean = System.getProperty("STURDY_DEBUG_FIXPOINT", "true").toBoolean
  val DEBUG_CACHE_CHANGES = System.getProperty("STURDY_DEBUG_CACHE_CHANGES", "false").toBoolean
