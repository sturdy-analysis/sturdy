package sturdy.fix

import sturdy.values.Finite
import sturdy.values.JoinValue

object Widening:
  def widen[T](old: T, now: T)(using w: Widening[T]): T = w.widen(old, now)

trait Widening[T]:
  def widen(old: T, now: T): T

given finiteJoinWidening[T] (using Finite[T])(using j: JoinValue[T]): Widening[T] with
  override def widen(old: T, now: T): T = j.joinValues(old, now)

case class WidenEquiList[V]()(using j: Widening[V]) extends Widening[List[V]]:
  override def widen(v1: List[V], v2: List[V]): List[V] =
    if (v1.size != v2.size)
      throw new IllegalStateException()
    v1.zip(v2).map(j.widen.tupled)

given widenMap[K, V](using f: Finite[K], w: Widening[V]): Widening[Map[K, V]] with
  override def widen(v1: Map[K, V], v2: Map[K, V]): Map[K, V] =
    var joined = v1
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = w.widen(v1V, v2V)
          joined += x -> joinedV
    joined

given widenTuple2[T1, T2](using w1: Widening[T1], w2: Widening[T2]): Widening[(T1, T2)] with
  override def widen(old: (T1, T2), now: (T1, T2)): (T1, T2) = (w1.widen(old._1, now._1), w2.widen(old._2, now._2))
given widenTuple3[T1, T2, T3](using w1: Widening[T1], w2: Widening[T2], w3: Widening[T3]): Widening[(T1, T2, T3)] with
  override def widen(old: (T1, T2, T3), now: (T1, T2, T3)): (T1, T2, T3) = (w1.widen(old._1, now._1), w2.widen(old._2, now._2), w3.widen(old._3, now._3))
given widenTuple4[T1, T2, T3, T4](using w1: Widening[T1], w2: Widening[T2], w3: Widening[T3], w4: Widening[T4]): Widening[(T1, T2, T3, T4)] with
  override def widen(old: (T1, T2, T3, T4), now: (T1, T2, T3, T4)): (T1, T2, T3, T4) = 
    (w1.widen(old._1, now._1), w2.widen(old._2, now._2), w3.widen(old._3, now._3), w4.widen(old._4, now._4))
