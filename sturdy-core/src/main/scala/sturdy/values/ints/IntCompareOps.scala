package sturdy.values.ints

/** Extra compare ops for integers */
trait IntCompareOps[V, B]:
  def ltUnsigned(v1: V, v2: V): B = ???
  def leUnsigned(v1: V, v2: V): B = ???
  def geUnsigned(v1: V, v2: V): B = ???
  def gtUnsigned(v1: V, v2: V): B = ???

given ConcreteIntCompareOps: IntCompareOps[Int, Boolean] with
  override def ltUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) < 0
  override def leUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) <= 0
  override def geUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) >= 0
  override def gtUnsigned(v1: Int, v2: Int): Boolean = Integer.compareUnsigned(v1, v2) > 0
