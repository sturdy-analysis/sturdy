package sturdy.data

import sturdy.values.{Combine, Finite, Join, MaybeChanged, Widen, Widening}

import scala.collection.{MapOps, immutable}
import scala.collection.immutable.IntMap

given FiniteMap[K, V](using Finite[K], Finite[V]): Finite[Map[K, V]] with {}


trait AbstractMap[K,V,M[K,V]]:
  val m: Map[K,V]
  def make[A,B](m: Map[A,B]): M[A,B]
  def apply(k: K): V = m(k)
  def isDefinedAt(k: K): Boolean = m.isDefinedAt(k)
  def get(k: K): Option[V] = m.get(k)
  def getOrElse(k: K, default: => V): V = m.getOrElse(k, default)
  def +(kv: (K,V)): M[K,V] = make(m + kv)
  def -(k: K): M[K,V] = make(m - k)
  def withFilter(f: ((K,V)) => Boolean): MapOps.WithFilter[K, V, immutable.Iterable, Map] = m.withFilter(f)
  def foreachEntry[U](f: (K,V) => U) = m.foreachEntry(f)
  def size: Int = m.size
  def values: Iterable[V] = m.values
  def map[K2, V2](f: ((K, V)) => (K2, V2)): M[K2, V2] = make(m.map(f))
  def foreach(f: ((K,V)) => Unit): Unit = m.foreach(f)
  override def hashCode(): Int = m.hashCode()
  override def equals(obj: Any): Boolean = obj match
    case that: AbstractMap[K, V, M] => this.getClass.getName == that.getClass.getName && m == that.m
    case _ => false

class MayMap[K,V](val m: Map[K, V]) extends AbstractMap[K,V,MayMap]:
  override def make[A, B](m: Map[A, B]): MayMap[A, B] = new MayMap(m)
  override def toString: String = s"MayMap(${m.mkString(", ")})"
class MustMap[K,V](val m: Map[K, V]) extends AbstractMap[K,V,MustMap]:
  override def make[A, B](m: Map[A, B]): MustMap[A, B] = new MustMap(m)
  override def toString: String = s"MustMap(${m.mkString(", ")})"

object MayMap:
  def apply[K,V](): MayMap[K,V] = new MayMap(Map())
  def apply[K,V](m: Map[K,V]): MayMap[K, V] = new MayMap(m)
object MustMap:
  def apply[K,V](): MustMap[K,V] = new MustMap(Map())
  def apply[K,V](m: Map[K,V]): MustMap[K, V] = new MustMap(m)

given JoinMayMap[K, V] (using j: Join[V]): Join[MayMap[K, V]] with
  override def apply(vs1: MayMap[K, V], vs2: MayMap[K, V]): MaybeChanged[MayMap[K, V]] =
    var joined = vs1.m
    var changed = false
    for ((x, v2) <- vs2.m)
      joined.get(x) match
        case None =>
          joined += x -> v2
          changed = true
        case Some(v1) =>
          val joinedV = j(v1, v2)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(MayMap(joined), changed)

given JoinMustMap[K, V] (using j: Join[V]): Join[MustMap[K, V]] with
  override def apply(vs1: MustMap[K, V], vs2: MustMap[K, V]): MaybeChanged[MustMap[K, V]] =
    var joined = Map[K,V]()
    val keys = vs1.m.keySet.intersect(vs2.m.keySet)
    var changed = keys.size != vs1.m.size
    for (k <- keys) {
      val joinedV = j(vs1.m(k), vs2.m(k))
      joined += k -> joinedV.get
      changed |= joinedV.hasChanged
    }
    if (changed)
      println(s"Changed join $vs1 ~> ${MustMap(joined)}")
    MaybeChanged(MustMap(joined), changed)

given JoinMayIntMap[V, W <: Widening] (using j: Combine[V, W]): Join[IntMap[V]] with
  override def apply(vs1: IntMap[V], vs2: IntMap[V]): MaybeChanged[IntMap[V]] =
    var joined = vs1
    var changed = false
    for ((x, v2) <- vs2)
      joined.get(x) match
        case None =>
          joined += x -> v2
          changed = true
        case Some(v1) =>
          val joinedV = j(v1, v2)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(joined, changed)

given WidenFiniteKeyMayMap[K, V] (using j: Widen[V], fk: Finite[K]): Widen[MayMap[K, V]] with
  override def apply(v1: MayMap[K, V], v2: MayMap[K, V]): MaybeChanged[MayMap[K, V]] =
    var joined = v1.m
    var changed = false
    for ((x, v2V) <- v2.m)
      joined.get(x) match
        case None =>
          joined += x -> v2V
          changed = true
        case Some(v1V) =>
          val joinedV = j(v1V, v2V)
          joined += x -> joinedV.get
          changed |= joinedV.hasChanged
    MaybeChanged(MayMap(joined), changed)

given WidenMustMap[K, V] (using j: Widen[V]): Widen[MustMap[K, V]] with
  override def apply(vs1: MustMap[K, V], vs2: MustMap[K, V]): MaybeChanged[MustMap[K, V]] =
    var joined = Map[K, V]()
    val keys = vs1.m.keySet.intersect(vs2.m.keySet)
    var changed = keys.size != vs1.m.size
    for (k <- keys) {
      val joinedV = j(vs1.m(k), vs2.m(k))
      joined += k -> joinedV.get
      changed |= joinedV.hasChanged
    }
    if (changed)
      println(s"Changed join $vs1 ~> ${MustMap(joined)}")
    MaybeChanged(MustMap(joined), changed)

inline def combineMayMaps[K, V](m1: MayMap[K, V], m2: MayMap[K, V], inline combine: (V, V) => V): MayMap[K, V] =
  val (small, large) = if (m1.m.size >= m2.m.size) (m1.m, m2.m) else (m2.m, m1.m)
  var result = large
  for ((k, v1) <- small)
    val v = large.get(k) match
      case None => v1
      case Some(v2) => combine(v1, v2)
    result += k -> v
  MayMap(result)