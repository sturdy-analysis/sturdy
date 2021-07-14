package stateful

import stateful.SignEnum.{Zero, Sign, Top, Neg, Pos}

trait Val[V] {
  type TValJoin[A]
  def int(i: Int): V
  def add(v1: V, v2: V): V
  def ifNeg[A](v: V, thn: => A, els: => A)(implicit j: TValJoin[A]): A
}

trait ValInt extends Val[Int] {
  type TValJoin[A] = Unit
  override def int(i: Int): Int = i
  override def add(v1: Int, v2: Int): Int = v1 + v2
  override def ifNeg[A](v: Int, thn: => A, els: => A)(implicit j: Unit): A =
    if (v < 0) thn else els
}

object SignEnum extends Enumeration {
  type Sign = Value
  val Neg, Zero, Pos, Top: Sign = Value
  implicit object Join extends Join[Sign] {
    override def apply(a1: Sign, a2: Sign): Sign = {
      if (a1 == a2) a1
      else Top
    }
  }
}
trait ValSign extends Val[Sign] with JoinComputation {
  override type TValJoin[A] = Join[A]
  override def int(i: Int): Sign = if (i < 0) Neg else if (i == 0) Zero else Pos
  override def add(v1: Sign, v2: Sign): Sign = (v1, v2) match {
    case (Zero, _) => v2
    case (_, Zero) => v1
    case (Neg, Neg) => Neg
    case (Pos, Pos) => Pos
    case _ => Top
  }
  override def ifNeg[A](v: Sign, thn: => A, els: => A)(implicit j: TValJoin[A]): A = v match {
    case Neg => thn
    case Zero => els
    case Pos => els
    case Top => join(thn, els)
  }
}
