package sturdy.values.relational

trait CompareOps[V, B]:
  def isZero(v1: V): B
  def isPositive(v1: V): B
  def isNegative(v1: V): B
  def isOdd(v1: V): B
  def isEven(v1: V): B
  def lt(v1: V, v2: V): B
  def le(v1: V, v2: V): B
  def ge(v1: V, v2: V): B
  def gt(v1: V, v2: V): B

given ConcreteCompareOps: CompareOps[Double, Boolean] with
  def lt(v1: Double, v2: Double): Boolean = v1 < v2
  def le(v1: Double, v2: Double): Boolean = v1 <= v2
  def ge(v1: Double, v2: Double): Boolean = v1 >= v2
  def gt(v1: Double, v2: Double): Boolean = v1 > v2
  def isZero(v1: Double): Boolean = ???
  def isPositive(v1: Double): Boolean = ???
  def isNegative(v1: Double): Boolean = ???
  def isOdd(v1: Double): Boolean = ???
  def isEven(v1: Double): Boolean = ???

given ConcreteIntCompareOps: CompareOps[Int, Boolean] with
  def lt(v1: Int, v2: Int): Boolean = v1 < v2
  def le(v1: Int, v2: Int): Boolean = v1 <= v2
  def ge(v1: Int, v2: Int): Boolean = v1 >= v2
  def gt(v1: Int, v2: Int): Boolean = v1 > v2
  def isZero(v1: Int): Boolean = v1 == 0
  def isPositive(v1: Int): Boolean = v1 >= 0
  def isNegative(v1: Int): Boolean = v1 < 0
  def isOdd(v1: Int): Boolean = v1 % 2 == 0
  def isEven(v1: Int): Boolean = v1 % 2 == 1