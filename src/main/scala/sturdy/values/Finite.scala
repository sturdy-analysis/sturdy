package sturdy.values

trait Finite[+T]

given finiteIterable[V] (using Finite[V]): Finite[Iterable[V]] with {}
given finiteMap[K, V] (using Finite[K], Finite[V]): Finite[Map[K, V]] with {}

given finiteTuple2[T1, T2](using Finite[T1], Finite[T2]): Finite[(T1, T2)] with {}
given finiteTuple3[T1, T2, T3](using Finite[T1], Finite[T2], Finite[T3]): Finite[(T1, T2, T3)] with {}

