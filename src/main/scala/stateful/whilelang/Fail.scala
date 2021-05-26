package stateful.whilelang

object Fail {
  trait Failure extends Throwable
}
trait Fail {
  @throws[Fail.Failure]
  def fail(msg: String): Nothing
}

object FailImpl {
  case class Failure(msg: String) extends Fail.Failure
}
trait FailImpl extends Fail {
  override def fail(msg: String): Nothing =
    throw FailImpl.Failure(msg)
}
