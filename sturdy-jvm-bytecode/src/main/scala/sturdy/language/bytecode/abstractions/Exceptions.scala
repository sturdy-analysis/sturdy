package sturdy.language.bytecode.abstractions

import sturdy.data.{CombineOption, *}
import sturdy.data.MayJoin.WithJoin
import sturdy.values.{Combine, MaybeChanged, Powerset, Widening}
import sturdy.values.exceptions.Exceptional
import sturdy.language.bytecode.generic.JvmExcept

trait Exceptions:
  case class JvmExceptAbstract[V](jumps: Powerset[Int],
                                  rets: Option[V],
                                  returns: Option[V],
                                  throwObjects: Option[V])

  given exceptionalJvm[V]: Exceptional[JvmExcept[V], JvmExceptAbstract[V], WithJoin] with
    override def exception(exc: JvmExcept[V]): JvmExceptAbstract[V] = exc match
      case JvmExcept.Jump(pc) => JvmExceptAbstract(Powerset(pc), None, None, None)
      case JvmExcept.Ret(pc) => JvmExceptAbstract(Powerset(), Some(pc), None, None)
      case JvmExcept.Return(returnValue) => JvmExceptAbstract(Powerset(), None, Some(returnValue), None)
      case JvmExcept.ThrowObject(exception) => JvmExceptAbstract(Powerset(), None, None, Some(exception))


    override def handle[A](e: JvmExceptAbstract[V])(f: JvmExcept[V] => A): WithJoin[A] ?=> A =
      val computations =
        e.jumps.set.map(pc => f(JvmExcept.Jump(pc))) ++
        e.rets.map(v => f(JvmExcept.Ret(v))) ++
        e.returns.map(v => f(JvmExcept.Return(v))) ++
        e.throwObjects.map(v => f(JvmExcept.ThrowObject(v)))

      mapJoin(computations, identity)

  given CombineJvmExceptAbstract[V, W <: Widening](using Combine[V, W]): Combine[JvmExceptAbstract[V], W] with
    override def apply(v1: JvmExceptAbstract[V], v2: JvmExceptAbstract[V]): MaybeChanged[JvmExceptAbstract[V]] =
      for
        jumps <- MaybeChanged(v1.jumps ++ v2.jumps, v1.jumps)
        rets <- CombineOption(v1.rets, v2.rets)
        returns <- CombineOption(v1.returns, v2.returns)
        throwObjects <- CombineOption(v1.throwObjects, v2.throwObjects)
      yield JvmExceptAbstract(jumps, rets, returns, throwObjects)
