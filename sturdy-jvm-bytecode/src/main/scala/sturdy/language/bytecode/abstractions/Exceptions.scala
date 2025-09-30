package sturdy.language.bytecode.abstractions

import org.opalj.br.ClassType
import sturdy.data.{CombineOption, *}
import sturdy.data.MayJoin.WithJoin
import sturdy.values.{Combine, Join, MaybeChanged, Powerset, Widening}
import sturdy.values.exceptions.Exceptional
import sturdy.language.bytecode.generic.JvmExcept

trait Exceptions:
  case class JvmExceptAbstract[V](jumps: Powerset[Int],
                                  rets: Option[V],
                                  returns: Option[V],
                                  throws: Powerset[ClassType],
                                  throwObjects: Option[V])

  given exceptionalJvm[V]: Exceptional[JvmExcept[V], JvmExceptAbstract[V], WithJoin] with
    override def exception(exc: JvmExcept[V]): JvmExceptAbstract[V] = exc match
      case JvmExcept.Jump(pc) => JvmExceptAbstract(Powerset(pc), None, None, Powerset(), None)
      case JvmExcept.Ret(pc) => JvmExceptAbstract(Powerset(), Some(pc), None, Powerset(), None)
      case JvmExcept.Return(returnValue) => JvmExceptAbstract(Powerset(), None, Some(returnValue), Powerset(), None)
      case JvmExcept.Throw(exception) => JvmExceptAbstract(Powerset(), None, None, Powerset(exception), None)
      case JvmExcept.ThrowObject(exception) => JvmExceptAbstract(Powerset(), None, None, Powerset(), Some(exception))


    override def handle[A](e: JvmExceptAbstract[V])(f: JvmExcept[V] => A): WithJoin[A] ?=> A =
      val computations =
        e.jumps.set.map(pc => f(JvmExcept.Jump(pc))) ++
        e.rets.map(v => f(JvmExcept.Ret(v))) ++
        e.returns.map(v => f(JvmExcept.Return(v))) ++
        e.throws.set.map(exception => f(JvmExcept.Throw(exception))) ++
        e.throwObjects.map(v => f(JvmExcept.ThrowObject(v)))

      mapJoin(computations, identity)

  given CombineJvmExceptAbstract[V, W <: Widening](using Combine[V, W]): Combine[JvmExceptAbstract[V], W] with
    override def apply(v1: JvmExceptAbstract[V], v2: JvmExceptAbstract[V]): MaybeChanged[JvmExceptAbstract[V]] =
      val jret = CombineOption(v1.rets, v2.rets)
      val jthrow = CombineOption(v1.throwObjects, v2.throwObjects)
      val jreturn = CombineOption(v1.returns, v2.returns)
      val exc = JvmExceptAbstract(
        v1.jumps ++ v2.jumps,
        jret.get,
        jreturn.get,
        v1.throws ++ v2.throws,
        jthrow.get
      )
      MaybeChanged(exc, jret.hasChanged || jreturn.hasChanged || jthrow.hasChanged || exc.jumps.size != v1.jumps.size || exc.throws.size != v1.throws.size)
