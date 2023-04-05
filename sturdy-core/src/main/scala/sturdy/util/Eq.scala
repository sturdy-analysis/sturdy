package sturdy.util

import scala.deriving.*
import scala.quoted.*

/**
 * Trait which defines a structural equality operator.
 * We need this in addition to the .equals method, because we override
 * the .equals method on syntax with a more performant version based on labels,
 * but still need to do structural comparisons other times.
 */
trait Eq[T] {
  def eqv(x: T, y: T): Boolean
}

object Eq {
  inline def eqv[T: Eq](x: T, y: T) = summon[Eq[T]].eqv(x,y)

  import scala.compiletime.{erasedValue, summonFrom}
  import compiletime.*
  import scala.deriving.*

  inline def tryEqv[TT](x: TT, y: TT): Boolean = summonFrom {
    case eq: Eq[TT] => eq.eqv(x, y)
  }

  inline def eqvElems[Elems <: Tuple](n: Int)(x: Product, y: Product): Boolean =
    inline erasedValue[Elems] match {
      case _: (elem *: elems1) =>
        tryEqv[elem](x.productElement(n).asInstanceOf[elem], y.productElement(n).asInstanceOf[elem]) &&
          eqvElems[elems1](n + 1)(x, y)
      case _: EmptyTuple =>
        true
    }

  transparent inline def eqvCases[Alts](n: Int)(x: Any, y: Any, ord: Int): Boolean =
    inline erasedValue[Alts] match {
      case _: (alt *: alts1) =>
        if (ord == n)
          summonFrom {
            case m: Mirror.ProductOf[`alt`] =>
              eqvElems[m.MirroredElemTypes](0)(x.asInstanceOf[Product], y.asInstanceOf[Product])
          }
        else eqvCases[alts1](n + 1)(x, y, ord)
      case _: EmptyTuple =>
        false
    }

  transparent inline def derived[T](implicit ev: Mirror.Of[T]): Eq[T] = new Eq[T] {
    def eqv(x: T, y: T): Boolean =
      inline ev match {
        case m: Mirror.SumOf[T] =>
          val ord = m.ordinal(x)
          ord == m.ordinal(y) && eqvCases[m.MirroredElemTypes](0)(x, y, ord)
        case m: Mirror.ProductOf[T] =>
          eqvElems[m.MirroredElemTypes](0)(x.asInstanceOf[Product], y.asInstanceOf[Product])
      }
  }

  given IntEq: Eq[Int] with
    def eqv(x: Int, y: Int) = x == y

  given StringEq: Eq[String] with
    override def eqv(x: String, y: String): Boolean = x.equals(y)

  given SeqEq[A: Eq]: Eq[Seq[A]] with
    override def eqv(xs: Seq[A], ys: Seq[A]): Boolean =
      val xIter = xs.iterator
      val yIter = ys.iterator
      while(xIter.hasNext && yIter.hasNext)
        val x = xIter.next()
        val y = yIter.next()
        if(x =!= y)
          return false
      !xIter.hasNext && !yIter.hasNext

  given PairEq[A: Eq, B: Eq]: Eq[(A,B)] = Eq.derived
  given OptionEq[A: Eq]: Eq[Option[A]] = Eq.derived

  extension[A: Eq](a: A)
    inline def ===(b: A): Boolean = summon[Eq[A]].eqv(a,b)
    inline def =!=(b: A): Boolean = !(a === b)
}
