package sturdy.effect.except

import sturdy.data.JEither
import sturdy.data.MayJoin
import sturdy.effect.{Effect, SturdyException, TrySturdyFinally}
import sturdy.values.exceptions.Exceptional

/** Effect [[Except]] causes and handles exceptions */
trait Except[Exc, E, J[_] <: MayJoin[_]] extends Effect, ObservableExcept[Exc]:
  val exceptional: Exceptional[Exc, E, J]

  @throws[SturdyException]
  def throws(ex: Exc): Nothing

  protected def tries[A](f: => A): JEither[J, A, E]

  final def tryCatch[A](f: => A)(handle: Exc => A): J[A] ?=> A =
    tryStart()
    TrySturdyFinally { tries(TrySturdyFinally {f} {catchStart()}).either(identity) { e =>
      exceptional.handle(e) { exc =>
        handlingStart(exc)
        TrySturdyFinally {handle(exc)} {handlingEnd()}
      }
    }} {
      catchEnd()
      tryEnd()
    }

  final def tryFinally[A](f: => A)(g: => Unit): J[A] ?=> A =
    val a = tryCatch(f)(exc => {g; throws(exc)})
    g
    a

//  final def tryCatchFinally[A](f: => A)(handle: Exc => A)(g: => Unit): MayJoin[A] ?=> A =
//    val tried = tries(f)
//    tried.either(a => {g; a})(e => try exceptional.handle(e)(handle) finally g)
