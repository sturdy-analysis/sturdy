package sturdy.values.exceptions

import sturdy.data.MayJoin

trait Exceptional[Exc, ExcV, J[_] <: MayJoin[_]]:
  type ExceptionalJoin[A]
  def exception(exc: Exc): ExcV
  def handle[A](e: ExcV)(f: Exc => A): J[A] ?=> A
