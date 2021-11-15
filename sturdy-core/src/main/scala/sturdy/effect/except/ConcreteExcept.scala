package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.SturdyException
import sturdy.values.exceptions.Exceptional

import scala.util.Success
import reflect.Selectable.reflectiveSelectable

case class ConcreteSturdyException[E](e: E) extends SturdyException:
  override def toString: String = s"Exception ${e.toString}"

trait ConcreteExcept[E](using val exceptional: Exceptional[E, E, NoJoin]) extends Except[E, E, NoJoin]:
  override def throws(ex: E): Nothing =
    throwing(ex)  
    throw ConcreteSturdyException(ex)

  override protected def tries[A](f: => A): EitherC[A, E] =
    try {
      EitherC.Left(f)
    } catch {
      case ConcreteSturdyException(ex) => EitherC.Right(ex.asInstanceOf[E])
    }

