package sturdy.values.booleans

trait IntBools[I, B]:
  def intToBool(i: I): B
  def boolToInt(b: B): I

given ConcreteIntBools: IntBools[Int, Boolean] with
  override def intToBool(i: Int): Boolean = i != 0
  override def boolToInt(b: Boolean): Int = if (b) 1 else 0

  
