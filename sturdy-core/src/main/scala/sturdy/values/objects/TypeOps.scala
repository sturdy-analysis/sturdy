package sturdy.values.objects

trait TypeOps[V, TypeRep, B]:
  def typeOf(v: V): TypeRep

  def ifInstanceOf[A](v: V, ty: TypeRep)(ifTrue: () => A)(ifFalse: () => A): A

trait SizeOps[V, B]:
  def is32Bit(v: V): B

object SizeOps:
  def is32Bit[V, B](v: V)(using ops: SizeOps[V, B]): B =
    ops.is32Bit(v)
