package sturdy.lang.whilee.vals

import sturdy.lang.whilee.CanFail
import sturdy.lang.whilee.vals.StdFail.FailException

object StdFail {
  case class FailException[E](e: E) extends Exception
}

trait StdFail[E] extends CanFail[E] {
  override def fail[A](e: E): A = throw FailException(e)
}
