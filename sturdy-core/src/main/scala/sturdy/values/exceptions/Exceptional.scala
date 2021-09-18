package sturdy.values.exceptions

trait Exceptional[Exc, E, J[_]]:
  type ExceptionalJoin[A]
  def exception(exc: Exc): E
  def handle[A](e: E)(f: Exc => A): J[A] ?=> A
