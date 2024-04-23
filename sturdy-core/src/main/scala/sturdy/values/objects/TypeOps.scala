package sturdy.values.objects

trait TypeOps[V, TypeRep, B] {
  def instanceOf(v: V, check: TypeRep): B
}

object TypeOps:
  def instanceOf[V, TypeRep, B](v: V, check: TypeRep)(using ops: TypeOps[V, TypeRep, B]): B =
    ops.instanceOf(v, check)


trait SizeOps[V, B]{
  def is32Bit(v: V): B
}

object SizeOps:
  def is32Bit[V, B](v: V)(using ops: SizeOps[V, B]): B =
    ops.is32Bit(v)
    
