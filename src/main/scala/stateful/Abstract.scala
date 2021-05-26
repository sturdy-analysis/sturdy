package stateful

import stateful.SignEnum.{Sign, Zero}

class Abstract(_localValue: Sign, _current: Sign) extends ValSign with ReadAbs[Sign] with StateAbs[Sign] with FailAbs {
  override var localValue: Sign = _localValue
  override var current: Sign = _current
  override val currentValJoin: Join[Sign] = implicitly
}
object Abstract {
  def run[A](f: Abstract => A, localValue: Sign = Zero, current: Sign = Zero): (A, Sign, Sign, Set[String]) = {
    val c = new Abstract(localValue, current)
    val (a, msgs) = try (f(c), Set[String]()) catch {
      case FailAbs.Failure(msgs) => (null.asInstanceOf[A], msgs)
    }
    (a, c.localValue, c.current, c.failures ++ msgs)
  }
}