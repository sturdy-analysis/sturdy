//package sturdy.language.pcf
//
//import sturdy.values.integer.{IntegerOps, given}
//
//
////trait IntegerOps[B, V]:
////  def integerLit(i: B): V
////  def randomInteger(): V
////
////  def add(v1: V, v2: V): V
////  def sub(v1: V, v2: V): V
////  def mul(v1: V, v2: V): V
////
////  def max(v1: V, v2: V): V
////  def min(v1: V, v2: V): V
////  def absolute(v: V): V
////
////  def div(v1: V, v2: V): V
////  def divUnsigned(v1: V, v2: V): V
////  /** Maintains the sign of v1 */
////  def remainder(v1: V, v2: V): V
////  def remainderUnsigned(v1: V, v2: V): V
////  /** Yields positive remainder of v1/v2 */
////  def modulo(v1: V, v2: V): V
////  def gcd(v1: V, v2: V): V
////  final def lcm(v1: V, v2: V): V = mul(div(absolute(v1), gcd(v1, v2)), absolute(v2))
////
////  /** Binary integer operations for base type B, represented as V */
////  def bitAnd(v1: V, v2: V): V
////  def bitOr(v1: V, v2: V): V
////  def bitXor(v1: V, v2: V): V
////  def shiftLeft(v: V, shift: V): V
////  def shiftRight(v: V, shift: V): V
////  def shiftRightUnsigned(v: V, shift: V): V
////  def rotateLeft(v: V, shift: V): V
////  def rotateRight(v: V, shift: V): V
////  def countLeadingZeros(v: V): V
////  def countTrailingZeros(v: V): V
////  def nonzeroBitCount(v: V): V
////  def invertBits(v: V): V
////
////  enum Value:
////    case Int(i: VInt)
////    case Closure(closure: VClosure)
////    case TopValue
//
//object Instances:
//
//  import sturdy.language.pcf.*
//  given IntegerOps[Int, Int] with
//    def integerLit(i: Int): Int = i
//    def randomInteger(): Int = scala.util.Random.nextInt()
//
//    def add(v1: Int, v2: Int): Int = v1 + v2
//    def sub(v1: Int, v2: Int): Int = v1 - v2
//    def mul(v1: Int, v2: Int): Int = v1 * v2
//
//    def max(v1: Int, v2: Int): Int = math.max(v1, v2)
//    def min(v1: Int, v2: Int): Int = math.min(v1, v2)
//    def absolute(v: Int): Int = math.abs(v)
//
//    def div(v1: Int, v2: Int): Int = v1 / v2
//    def divUnsigned(v1: Int, v2: Int): Int = v1 / v2
//    def remainder(v1: Int, v2: Int): Int = v1 % v2
//    def remainderUnsigned(v1: Int, v2: Int): Int = v1 % v2
//    def modulo(v1: Int, v2: Int): Int = v1 % v2
//    def gcd(v1: Int, v2: Int): Int = ???
//
//    def bitAnd(v1: Int, v2: Int): Int = v1 & v2
//    def bitOr(v1: Int, v2: Int): Int = v1 | v2
//    def bitXor(v1: Int, v2: Int): Int = v1 ^ v2
//    def shiftLeft(v: Int, shift: Int): Int = v << shift
//    def shiftRight(v: Int, shift: Int): Int = v >> shift
//    def shiftRightUnsigned(v: Int, shift: Int): Int = v >>> shift
//    def rotateLeft(v: Int, shift: Int): Int = ???
//    def rotateRight(v: Int, shift: Int): Int = ???
//    def countLeadingZeros(v: Int): Int = ???
//    def countTrailingZeros(v: Int): Int = ???
//    def nonzeroBitCount(v: Int): Int = ???
//    def invertBits(v: Int): Int = ???
//  given IntegerOps[Int, Value] with
//    def integerLit(i: Int): Value = Value.Int(i)
//    def randomInteger(): Value = Value.Int(scala.util.Random.nextInt())
//
//    def add(v1: Value, v2: Value): Value = (v1, v2) match
//      case (Value.Int(i1), Value.Int(i2)) => Value.Int(i1 + i2)
//      case _ => Value.TopValue
//
//    def sub(v1: Value, v2: Value): Value = (v1, v2) match
//      case (Value.Int(i1), Value.Int(i2)) => Value.Int(i1 - i2)
//      case _ => Value.TopValue
//
//    def mul(v1: Value, v2: Value): Value = (v1, v2) match
//      case (Value.Int(i1), Value.Int(i2)) => Value.Int(i1 * i2)
//      case _ => Value.TopValue
//
//    def max(v1: Value, v2: Value): Value = (v1, v2) match
//      case (Value.Int(i1), Value.Int(i2)) => Value.Int(math.max(i1, i2))
//      case _ => Value.TopValue
//
//    def min(v1: Value, v2: Value): Value = (v1, v2) match
//      case (Value.Int(i1), Value.Int(i2)) => Value.Int(math.min(i1, i2))
//      case _ => Value.TopValue
//
//    def absolute(v: Value): Value = v match
//      case Value.Int(i) => Value.Int(math.abs(i))
//      case _ => Value.TopValue
//
//    def div(v1: Value, v2: Value): Value = ???
//    def divUnsigned(v1: Value, v2: Value): Value = ???
//    def remainder(v1: Value, v2: Value): Value = ???
//    def remainderUnsigned(v1: Value, v2: Value): Value = ???
//    def modulo(v1: Value, v2: Value): Value = ???
//    def gcd(v1: Value, v2: Value): Value = ???
//
//    def bitAnd(v1: Value, v2: Value): Value = ???
//    def bitOr(v1: Value, v2: Value): Value = ???
//    def bitXor(v1: Value, v2: Value): Value = ???
//    def shiftLeft(v: Value, shift: Value): Value = ???
//    def shiftRight(v: Value, shift: Value): Value = ???
//    def shiftRightUnsigned(v: Value, shift: Value): Value = ???
//    def rotateLeft(v: Value, shift: Value): Value = ???
//    def rotateRight(v: Value, shift: Value): Value = ???
//    def countLeadingZeros(v: Value): Value = ???
//    def countTrailingZeros(v: Value): Value = ???
//    def nonzeroBitCount(v: Value): Value = ???
//    def invertBits(v: Value): Value = ???
