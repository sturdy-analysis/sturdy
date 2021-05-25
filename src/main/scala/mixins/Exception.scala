package mixins

trait Exception[Exc] {
  val exc: Exception[Exc] = this

  def throwing(e: Exc): Nothing
  def trying[A](f: => A)(handle: Exc => A): A
}

trait ConcreteException[Exc] extends Exception[Exc] {
  case class ExceptionImpl(e: Exc) extends Throwable

  override def throwing(e: Exc): Nothing =
    throw ExceptionImpl(e)

  override def trying[A](f: => A)(handle: Exc => A): A =
    try f catch {
      case ExceptionImpl(e) => handle(e)
    }
}