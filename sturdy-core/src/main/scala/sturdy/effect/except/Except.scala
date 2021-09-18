package sturdy.effect.except

import sturdy.data.Either
import sturdy.values.exceptions.Exceptional

trait ExceptException extends Throwable

trait Except[Exc, E]:
  type ExceptJoin[A]
  val exceptional: Exceptional[Exc, E, ExceptJoin]

  @throws[ExceptException]
  def throws(ex: Exc): Nothing

  protected def tries[A](f: => A): Either[ExceptJoin, A, E]

  final def tryCatch[A](f: => A)(handle: Exc => A): ExceptJoin[A] ?=> A =
    tries(f).either(identity)(e => exceptional.handle(e)(handle))

  final def tryFinally[A](f: => A)(g: => Unit): ExceptJoin[A] ?=> A =
    tries(f).either(a => {g; a})(e => {g; exceptional.handle(e)(throws)})

  final def tryCatchFinally[A](f: => A)(handle: Exc => A)(g: => Unit): ExceptJoin[A] ?=> A =
    tries(f).either(a => {g; a})(e => try exceptional.handle(e)(handle) finally g)
