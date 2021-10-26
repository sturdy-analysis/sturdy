package sturdy.effect.except

import sturdy.data.Either
import sturdy.effect.SturdyException
import sturdy.values.exceptions.Exceptional

trait ExceptException extends SturdyException:
  override def isBottom: Boolean = false

trait Except[Exc, E, MayJoin[_]] extends ObservableExcept[Exc]:
  val exceptional: Exceptional[Exc, E, MayJoin]

  @throws[ExceptException]
  def throws(ex: Exc): Nothing

  protected def tries[A](f: => A): Either[MayJoin, A, E]

  final def tryCatch[A](f: => A)(handle: Exc => A): MayJoin[A] ?=> A =
    tries(f).either(identity)(e => exceptional.handle(e)(handle))

  final def tryFinally[A](f: => A)(g: => Unit): MayJoin[A] ?=> A =
    tries(f).either(a => {g; a})(e => {g; exceptional.handle(e)(throws)})

  final def tryCatchFinally[A](f: => A)(handle: Exc => A)(g: => Unit): MayJoin[A] ?=> A =
    val tried = tries(f)
    tried.either(a => {g; a})(e => try exceptional.handle(e)(handle) finally g)
