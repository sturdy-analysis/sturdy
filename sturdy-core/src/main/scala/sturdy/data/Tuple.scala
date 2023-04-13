package sturdy.data

import sturdy.values.{Combine, Finite, MaybeChanged, PartialOrder, Widening}

given JoinTuple2[T1, T2, W <: Widening](using j1: Combine[T1, W], j2: Combine[T2, W]): Combine[(T1, T2), W] with
  def apply(old: (T1, T2), now: (T1, T2)): MaybeChanged[(T1, T2)] =
    val v1 = j1(old._1, now._1)
    val v2 = j2(old._2, now._2)
    MaybeChanged((v1.get, v2.get), v1.hasChanged || v2.hasChanged)

  override def lteq(x: (T1, T2), y: (T1, T2)): Boolean =
    j1.lteq(x._1, y._1) && j2.lteq(x._2, y._2)

given JoinTuple3[T1, T2, T3, W <: Widening](using j1: Combine[T1, W], j2: Combine[T2, W], j3: Combine[T3, W]): Combine[(T1, T2, T3), W] with
  def apply(old: (T1, T2, T3), now: (T1, T2, T3)): MaybeChanged[(T1, T2, T3)] =
    val v1 = j1(old._1, now._1)
    val v2 = j2(old._2, now._2)
    val v3 = j3(old._3, now._3)
    MaybeChanged((v1.get, v2.get, v3.get), v1.hasChanged || v2.hasChanged || v3.hasChanged)

  override def lteq(x: (T1, T2, T3), y: (T1, T2, T3)): Boolean =
    j1.lteq(x._1, y._1) && j2.lteq(x._2, y._2) && j3.lteq(x._3,y._3)

given JoinTuple4[T1, T2, T3, T4, W <: Widening](using j1: Combine[T1, W], j2: Combine[T2, W], j3: Combine[T3, W], j4: Combine[T4, W]): Combine[(T1, T2, T3, T4), W] with
  def apply(old: (T1, T2, T3, T4), now: (T1, T2, T3, T4)): MaybeChanged[(T1, T2, T3, T4)] =
    val v1 = j1(old._1, now._1)
    val v2 = j2(old._2, now._2)
    val v3 = j3(old._3, now._3)
    val v4 = j4(old._4, now._4)
    MaybeChanged((v1.get, v2.get, v3.get, v4.get), v1.hasChanged || v2.hasChanged || v3.hasChanged || v4.hasChanged)

  override def lteq(x: (T1, T2, T3, T4), y: (T1, T2, T3, T4)): Boolean =
    j1.lteq(x._1, y._1) && j2.lteq(x._2, y._2) && j3.lteq(x._3, y._3) && j4.lteq(x._4,y._4)

given FiniteTuple2[T1, T2](using Finite[T1], Finite[T2]): Finite[(T1, T2)] with {}
given FiniteTuple3[T1, T2, T3](using Finite[T1], Finite[T2], Finite[T3]): Finite[(T1, T2, T3)] with {}
given FiniteTuple4[T1, T2, T3, T4](using Finite[T1], Finite[T2], Finite[T3], Finite[T4]): Finite[(T1, T2, T3, T4)] with {}
