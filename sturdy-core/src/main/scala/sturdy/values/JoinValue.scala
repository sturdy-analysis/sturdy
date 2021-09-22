package sturdy.values

trait JoinValue[V]:
  def joinValues(v1: V, v2: V): V

trait Joinable[V]:
  def join(that: V): V
given JoinableJoin[V <: Joinable[V]]: JoinValue[V] with
  override def joinValues(v1: V, v2: V): V = v1.join(v2)

object JoinValue:
  def join[V](v1: V, v2: V)(using j: JoinValue[V]) = (v1, v2) match {
    case (r1: AnyRef, r2: AnyRef) if r1 eq r2 => r1
    case _ => j.joinValues(v1, v2)
  }

given unit: Unit = ()
given joinUnit: JoinValue[Unit] with
  override def joinValues(v1: Unit, v2: Unit): Unit = v1

case class JoinEquiList[V]()(using j: JoinValue[V]) extends JoinValue[List[V]]:
  override def joinValues(v1: List[V], v2: List[V]): List[V] =
    if (v1.size != v2.size)
      throw new IllegalStateException()
    v1.zip(v2).map(j.joinValues.tupled)

given joinMap[K, V] (using j: JoinValue[V]): JoinValue[Map[K, V]] with
  override def joinValues(v1: Map[K, V], v2: Map[K, V]): Map[K, V] =
    var joined = v1
    for ((x, v2V) <- v2)
      joined.get(x) match
        case None => joined += x -> v2V
        case Some(v1V) =>
          val joinedV = j.joinValues(v1V, v2V)
          joined += x -> joinedV
    joined

given joinTuple2[T1, T2](using j1: JoinValue[T1], j2: JoinValue[T2]): JoinValue[(T1, T2)] with
  override def joinValues(old: (T1, T2), now: (T1, T2)): (T1, T2) = (j1.joinValues(old._1, now._1), j2.joinValues(old._2, now._2))

given joinTuple3[T1, T2, T3](using j1: JoinValue[T1], j2: JoinValue[T2], j3: JoinValue[T3]): JoinValue[(T1, T2, T3)] with
  override def joinValues(old: (T1, T2, T3), now: (T1, T2, T3)): (T1, T2, T3) = (j1.joinValues(old._1, now._1), j2.joinValues(old._2, now._2), j3.joinValues(old._3, now._3))

given joinTuple4[T1, T2, T3, T4](using j1: JoinValue[T1], j2: JoinValue[T2], j3: JoinValue[T3], j4: JoinValue[T4]): JoinValue[(T1, T2, T3, T4)] with
  override def joinValues(old: (T1, T2, T3, T4), now: (T1, T2, T3, T4)): (T1, T2, T3, T4) =
    (j1.joinValues(old._1, now._1), j2.joinValues(old._2, now._2), j3.joinValues(old._3, now._3), j4.joinValues(old._4, now._4))
