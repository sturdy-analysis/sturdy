package stateful


trait State[V] {
  def getCount: V
  def setCount(c: V)
}
trait StateImpl[V] extends State[V] {
  var current: V
  override def getCount: V = current
  override def setCount(c: V): Unit =
    current = c
}
trait StateAbs[V] extends StateImpl[V] with JoinComputation {
  val currentValJoin: Join[V]

  override def join[A](f: => A, g: => A)(implicit j: Join[A]): A = {
    val snapshot = current
    var newCurrents: List[V] = List()

    def track(fun: => A): A = {
      current = snapshot
      val a = fun
      newCurrents +:= current
      a
    }

    val a = super.join(track(f), track(g))
    current = newCurrents.reduce(currentValJoin)
    a
  }
}