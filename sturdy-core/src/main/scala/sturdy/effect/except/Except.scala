package sturdy.effect.except

import sturdy.data.Either
import sturdy.effect.SturdyException
import sturdy.values.exceptions.Exceptional


private[except] trait ExceptSturdyException extends SturdyException:
  override def isBottom: Boolean = false

trait LanguageException
//  val cause: Option[LanguageException]
//  lazy val rootCause: LanguageException = cause match
//    case None => this
//    case Some(ex) => ex.rootCause

trait Except[Exc <: LanguageException, E, MayJoin[_]] extends ObservableExcept[Exc]:
  val exceptional: Exceptional[Exc, E, MayJoin]

  @throws[ExceptSturdyException]
  def throws(ex: Exc): Nothing

  protected def tries[A](f: => A): Either[MayJoin, A, E]

  final def tryCatch[A](f: => A)(handle: Exc => A): MayJoin[A] ?=> A =
    tryStart()
    try tries(f).either(identity){ e =>
      catchStart()
      try exceptional.handle(e)(exc => handling(exc, handle))
      finally catchEnd()
    } finally {
      tryEnd()
    }

  final def tryFinally[A](f: => A)(g: => Unit): MayJoin[A] ?=> A =
    tryStart()
    try tries(f).either(a => {g; a}){ e =>
      catchStart()
      try exceptional.handle(e) { exc =>
        handling(exc)
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
