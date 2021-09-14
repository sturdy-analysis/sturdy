package sturdy.effect.except

import scala.util.Success

trait CExcept[E] extends Except[E]:
  override def throws(ex: E): Nothing = throw ExceptException(ex)
  override def tries[A, B](f: => A)(success: A => B)(fail: E => B): B =
    try {
      val a = f
      success(a)
    } catch {
      case ExceptException(ex) => fail(ex.asInstanceOf[E])
      case ex => throw ex
    }
