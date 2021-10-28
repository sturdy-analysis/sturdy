package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.SturdyException
import sturdy.values.exceptions.{ConcreteExceptional, Exceptional}

import scala.util.Success

case class ConcreteException[E](e: E) extends ExceptException:
  override def toString: String = s"Exception ${e.toString}"

trait ConcreteExcept[E] extends Except[E, E, NoJoin], ObservableExcept[E]:
  override val exceptional = ConcreteExceptional[E]

  override def throws(ex: E): Nothing =
    thrown(ex)  
    throw ConcreteException(ex)

  override protected def tries[A](f: => A): EitherC[A, E] =
    try {
      EitherC.Left(f)
    } catch {
      case ConcreteException(ex) => EitherC.Right(ex.asInstanceOf[E])
      case ex => throw ex
    }

