package sturdy.values.exceptions

trait Exceptional[Exc, ExcV, MayJoin[_]]:
  type ExceptionalJoin[A]
  def exception(exc: Exc): ExcV
  def handle[A](e: ExcV)(f: Exc => A): MayJoin[A] ?=> A
