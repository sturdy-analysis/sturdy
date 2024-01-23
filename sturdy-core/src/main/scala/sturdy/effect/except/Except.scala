package sturdy.effect.except

import sturdy.data.JEither
import sturdy.data.MayJoin
import sturdy.effect.Effect
import sturdy.effect.SturdyException
import sturdy.values.exceptions.Exceptional

/** Effect [[Except]] causes and handles exceptions */
trait Except[Exc, E, J[_] <: MayJoin[_]] extends Effect, ObservableExcept[Exc]:
  val exceptional: Exceptional[Exc, E, J]

  @throws[SturdyException]
  def throws(ex: Exc): Nothing

  protected def tries[A](f: => A): JEither[J, A, E]

  final def tryCatch[A](f: => A)(handle: Exc => A): J[A] ?=> A =
    tryStart()
    try tries(f).either(identity){ e =>
      catchStart()
      try exceptional.handle(e)(exc => handling(exc, handle))
      finally catchEnd()
    } finally {
      tryEnd()
    }

  final def tryFinally[A](f: => A)(g: => Unit): J[A] ?=> A =
    tryStart()
    try tries(f).either(a => {g; a}){ e =>
      catchStart()
      try exceptional.handle(e) { exc =>
//        handling(exc)
        g
        throws(exc)
      } finally {
        catchEnd()
      }
    } finally {
      tryEnd()
    }

//  final def tryCatchFinally[A](f: => A)(handle: Exc => A)(g: => Unit): MayJoin[A] ?=> A =
//    val tried = tries(f)
//    tried.either(a => {g; a})(e => try exceptional.handle(e)(handle) finally g)
