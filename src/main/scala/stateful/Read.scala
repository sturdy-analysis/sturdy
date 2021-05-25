package stateful

trait Read[V] {
  def read: V
  def provide[A](i: V)(f: => A): A
}

trait ReadImpl[V] extends Read[V] {
  var localValue: V
  override def read: V = localValue
  override def provide[A](i: V)(f: => A): A = {
    val old = localValue
    localValue = i
    val a = f
    localValue = old
    a
  }
}

trait ReadAbs[V] extends ReadImpl[V] with JoinComputation {
  // fall through to super.join because `localValue` cannot be changed by f or g non-locally
  //  override def join[A](f: => A, g: => A)(implicit j: JoinVal[A]): A =
  //    super.join(f, g)
}