package sturdy.effect.except

import sturdy.data.*
import sturdy.values.exceptions.{ConcreteExceptional, Exceptional}

import scala.util.Success

case class ConcreteException[E](e: E) extends ExceptException:
  override def toString: String = s"Exception ${e.toString}"

trait ConcreteExcept[E] extends Except[E, E]:
  override type ExceptJoin[A] = NoJoin[A]
  override val exceptional = ConcreteExceptional[E]

  override def throws(ex: E): Nothing = throw ConcreteException(ex)

  override protected def tries[A](f: => A): EitherC[A, E] =
    try {
      EitherC.Left(f)
    } catch {
      case ConcreteException(ex) => EitherC.Right(ex.asInstanceOf[E])
      case ex => throw ex
    }
