package stateful

trait Fail {
  def fail(msg: String): Unit
}

object FailConc {
  case class Failure(msg: String) extends Exception(msg)
}
trait FailConc extends Fail {
  override def fail(msg: String): Unit =
    throw FailConc.Failure(msg)
}

object FailAbs {
  case class Failure(msgs: Set[String]) extends Exception(msgs.toString)
}
trait FailAbs extends Fail with JoinComputation {
  var failures: Set[String] = Set()

  override def fail(msg: String): Unit =
    throw FailAbs.Failure(Set(msg))

  override def join[A](f: => A, g: => A)(implicit j: JoinVal[A]): A = {
    def track(fun: => A): A = try fun catch {
      case FailAbs.Failure(msgs) =>
        failures ++= msgs
        j.bottom
    }

    val a = super.join(track(f), track(g))
    if (j.isBottom(a)) {
      val fail = FailAbs.Failure(failures)
      failures = Set()
      throw fail
    } else {
      a
    }
  }
}