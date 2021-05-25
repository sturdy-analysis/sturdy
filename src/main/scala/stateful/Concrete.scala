package stateful

class Concrete(_localValue: Int, _current: Int) extends ValInt with ReadImpl[Int] with StateImpl[Int] with FailConc {
  override var localValue: Int = _localValue
  override var current: Int = _current
}

object Concrete {
  def run[A](f: Concrete => A, localValue: Int = 0, current: Int = 0): (A, Int, Int, Option[String]) = {
    val c = new Concrete(localValue, current)
    val (a, msg) = try (f(c), None) catch {
      case FailConc.Failure(msg) => (null.asInstanceOf[A], Some(msg))
    }
    (a, c.localValue, c.current, msg)
  }
}