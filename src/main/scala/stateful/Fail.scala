package stateful

object Fail {
  trait Failure extends Throwable
}
trait Fail {
  @throws[Fail.Failure]
  def fail(msg: String): Nothing
}

object FailConc {
  case class Failure(msg: String) extends Fail.Failure
}
trait FailConc extends Fail {
  override def fail(msg: String): Nothing =
    throw FailConc.Failure(msg)
}

object FailAbs {
  case class Failure(msgs: Set[String]) extends Fail.Failure
}
trait FailAbs extends Fail with JoinComputation {
  var failures: Set[String] = Set()

  private def logFailures[A](fun: => A): A = try fun catch {
    case fail@FailAbs.Failure(msgs) =>
      failures ++= msgs
      throw fail
  }

  override def fail(msg: String): Nothing =
    throw FailAbs.Failure(Set(msg))

  override def join[A](f: => A, g: => A)(implicit j: JoinVal[A]): A =
    super.join(logFailures(f), logFailures(g))
}