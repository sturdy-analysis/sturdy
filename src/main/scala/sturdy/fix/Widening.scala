package sturdy.fix

import sturdy.values.Finite
import sturdy.values.JoinValue

trait Widening[T]:
  def widen(old: T, now: T): T

given finiteJoinWidening[T] (using Finite[T])(using j: JoinValue[T]): Widening[T] with
  override def widen(old: T, now: T): T = j.joinValues(old, now)

given widenMap[K, V] (using f: Finite[K], w: Widening[V]): Widening[Map[K, V]] with
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
