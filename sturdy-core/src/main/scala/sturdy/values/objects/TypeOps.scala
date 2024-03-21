package sturdy.values.objects

trait TypeOps[V, TypeRep, B] {
  def instanceOf(v: V, check: TypeRep): B
}

object TypeOps:
  def instanceOf[V, TypeRep, B](v: V, check: TypeRep)(using ops: TypeOps[V, TypeRep, B]): B =
    ops.instanceOf(v, check)

