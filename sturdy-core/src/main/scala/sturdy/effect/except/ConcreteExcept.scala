package sturdy.effect.except

import sturdy.data.*
import sturdy.effect.Stateless
import sturdy.effect.SturdyException
import sturdy.values.exceptions.Exceptional

import scala.util.Success
import reflect.Selectable.reflectiveSelectable

case class ConcreteSturdyException[E](e: E) extends SturdyException:
  override def toString: String = s"Exception ${e.toString}"

class ConcreteExcept[E](using val exceptional: Exceptional[E, E, NoJoin]) extends Except[E, E, NoJoin], Stateless:
  override def throws(ex: E): Nothing =
    throwing(ex)  
    throw ConcreteSturdyException(ex)

  override protected def tries[A](f: => A): JEitherC[A, E] =
    try {
      JEitherC.Left(f)
    } catch {
      case ConcreteSturdyException(ex) => JEitherC.Right(ex.asInstanceOf[E])
    }

