package sturdy.language.bytecode.abstractions

import org.opalj.br.ObjectType
import sturdy.data.*
import sturdy.data.MayJoin.WithJoin
import sturdy.values.{Combine, Join, MaybeChanged, Powerset, Widening}
import sturdy.values.exceptions.Exceptional
import sturdy.language.bytecode.generic.JvmExcept

trait Exceptions:
  case class JvmExceptAbstract[V](jumps: Powerset[Int],
                                  returns: Option[V],
                                  throws: Powerset[ObjectType],
                                  throwObjects: Option[V])

  given exceptionalJvm[V]: Exceptional[JvmExcept[V], JvmExceptAbstract[V], WithJoin] with
    override def exception(exc: JvmExcept[V]): JvmExceptAbstract[V] = exc match
      case JvmExcept.Jump(pc) => JvmExceptAbstract(Powerset(pc), None, Powerset(), None)
      case JvmExcept.Ret(pc) => JvmExceptAbstract(Powerset(), Some(pc), Powerset(), None)
      case JvmExcept.Throw(exception) => JvmExceptAbstract(Powerset(), None, Powerset(exception), None)
      case JvmExcept.ThrowObject(exception) => JvmExceptAbstract(Powerset(), None, Powerset(), Some(exception))

    override def handle[A](e: JvmExceptAbstract[V])(f: JvmExcept[V] => A): WithJoin[A] ?=> A =
      val computations: Iterable[() => A] =
        e.jumps.set.map(pc => () => f(JvmExcept.Jump(pc))) ++
        e.returns.map(v => () => f(JvmExcept.Ret(v))) ++
        e.throws.set.map(exception => () => f(JvmExcept.Throw(exception))) ++
        e.throwObjects.map(v => () => f(JvmExcept.ThrowObject(v)))
      
      mapJoin(computations, comp => comp())

  given CombineJvmExceptAbstract[V, W <: Widening](using Combine[V, W]): Combine[JvmExceptAbstract[V], W] with
    override def apply(v1: JvmExceptAbstract[V], v2: JvmExceptAbstract[V]): MaybeChanged[JvmExceptAbstract[V]] = {
      val jret = CombineOption()(v1.returns, v2.returns)
      val jthrow = CombineOption()(v1.throwObjects, v2.throwObjects)
      val exc = JvmExceptAbstract(
        v1.jumps ++ v2.jumps,
        jret.get,
        v1.throws ++ v2.throws,
        jthrow.get
      )
      MaybeChanged(exc, jret.hasChanged || jthrow.hasChanged || exc.jumps.size != v1.jumps.size || exc.throws.size != v1.throws.size)
    }