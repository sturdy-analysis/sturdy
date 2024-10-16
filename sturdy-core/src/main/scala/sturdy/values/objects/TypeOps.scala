package sturdy.values.objects

import sturdy.values.Topped
import sturdy.values.integer.NumericInterval

trait TypeOps[V, TypeRep, B] {
  def instanceOf(v: V, target: TypeRep): B
}

object TypeOps:
  def instanceOf[V, TypeRep, B](v: V, target: TypeRep)(using ops: TypeOps[V, TypeRep, B]): B =
    ops.instanceOf(v, target)

class ConcreteObjectTypeOps[OID, CF, Addr, FieldName, TypeRep](f: (CF, TypeRep) => Boolean) extends TypeOps[Object[OID, CF, Addr, FieldName], TypeRep, Boolean]:
  override def instanceOf(v: Object[OID, CF, Addr, FieldName], target: TypeRep): Boolean =
    if (target == null)
      false
    else
      f(v.cls, target)

trait SizeOps[V, B]{
  def is32Bit(v: V): B
}

object SizeOps:
  def is32Bit[V, B](v: V)(using ops: SizeOps[V, B]): B =
    ops.is32Bit(v)

given ToppedSizeOps[V, B](using ops: SizeOps[V, B]): SizeOps[Topped[V], Topped[B]] with
  override def is32Bit(v: Topped[V]): Topped[B] = v match
    case Topped.Top => Topped.Top
    case Topped.Actual(v) => Topped.Actual(ops.is32Bit(v))

given IntervalSizeOps[V, B](using ops: SizeOps[V, B]): SizeOps[NumericInterval[V], Topped[B]] with
  override def is32Bit(v: NumericInterval[V]): Topped[B] = ??? //v match
//    case v: NumericInterval[Int] => Topped.Actual(ops.is32Bit(v.high))
//    case v: NumericInterval[Long] => ???
