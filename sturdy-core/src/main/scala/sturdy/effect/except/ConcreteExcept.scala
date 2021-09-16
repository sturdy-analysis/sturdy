package sturdy.effect.except

import sturdy.effect.EitherCompute
import sturdy.effect.NoJoin
import sturdy.effect.EitherComputeConcrete

import scala.util.Success

case class ConcreteException[E](e: E) extends ExceptException:
  override def toString: String = s"Exception ${e.toString}"


trait ConcreteExcept[E] extends Except[E]:
  override type ExceptJoin[A] = NoJoin[A]

  override def throws(ex: E): Nothing = throw ConcreteException(ex)

  override def tries[A](f: => A): EitherComputeConcrete[A, E] =
    try {
      EitherComputeConcrete.Left(f)
    } catch {
      case ConcreteException(ex) => EitherComputeConcrete.Right(ex.asInstanceOf[E])
      case ex => throw ex
    }
