package sturdy.effect.except

case class ExceptException[E](e: E) extends Throwable

trait Except[E]:
  @throws[ExceptException[E]]
  def throws(ex: E): Nothing

  def tries[A, B](f: => A)(success: A => B)(fail: E => B): B

  final def catches[A](f: => A)(fail: E => A): A =
    tries(f)(identity)(fail)

  final def finalizes[A](f: => A)(g: => Unit): A =
    tries(f)(a => {g; a})(e => {g; throws(e)})

  final def catchFinally[A](f: => A)(fail: E => A)(g: => Unit): A =
    tries(f)(a => {g; a})(e => try fail(e) finally g)
