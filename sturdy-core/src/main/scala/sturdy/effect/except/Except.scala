package sturdy.effect.except

import sturdy.effect.EitherCompute

trait ExceptException extends Throwable

trait Except[E]:
  type ExceptJoin[A]

  @throws[ExceptException]
  def throws(ex: E): Nothing

  def tries[A](f: => A): EitherCompute[ExceptJoin, A, E]

  final def tryCatch[A](f: => A)(handle: E => A): ExceptJoin[A] ?=> A =
    tries(f).either(identity)(handle)

  final def tryFinally[A](f: => A)(g: => Unit): ExceptJoin[A] ?=> A =
    tries(f).either(a => {g; a})(e => {g; throws(e)})

  final def tryCatchFinally[A](f: => A)(handle: E => A)(g: => Unit): ExceptJoin[A] ?=> A =
    tries(f).either(a => {g; a})(e => try handle(e) finally g)
