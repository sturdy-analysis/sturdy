package sturdy.values.integer

import sturdy.values.{Combine, MaybeChanged, Widening}
import sturdy.values.abstraction.symbolic.*

given SymbolicIntegerOps[S, V](using sym: SymbolicValue[IntExp[S], S, V], ops: IntegerOps[Int, V]): IntegerOps[Int, IntExp[S]] with

  def integerLit(i: Int): IntExp[S] = IntExp.IntLit(i)
  def randomInteger(): IntExp[S] = IntExp.Rand()

  def add(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = IntExp.Add(v1, v2)
  def sub(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = IntExp.Sub(v1, v2)
  def mul(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = IntExp.Mul(v1, v2)

  /** Example for an operation not supported by the symbolic domain */
  def bitAnd(v1: IntExp[S], v2: IntExp[S]): IntExp[S] =
    val s1 = sym.embedTreeAndExtractValue(v1)
    val s2 = sym.embedTreeAndExtractValue(v2)
    val s = sym.embedValue(ops.bitAnd(s1, s2))
    IntExp.Symbol(s)


  def max(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def min(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def absolute(v: IntExp[S]): IntExp[S] = ???

  def div(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def divUnsigned(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  /** Maintains the sign of v1 */
  def remainder(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def remainderUnsigned(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  /** Yields positive remainder of v1/v2 */
  def modulo(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def gcd(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???

  /** Binary integer operations for base type B, represented as IntExp[S] */
  def bitOr(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def bitXor(v1: IntExp[S], v2: IntExp[S]): IntExp[S] = ???
  def shiftLeft(v: IntExp[S], shift: IntExp[S]): IntExp[S] = ???
  def shiftRight(v: IntExp[S], shift: IntExp[S]): IntExp[S] = ???
  def shiftRightUnsigned(v: IntExp[S], shift: IntExp[S]): IntExp[S] = ???
  def rotateLeft(v: IntExp[S], shift: IntExp[S]): IntExp[S] = ???
  def rotateRight(v: IntExp[S], shift: IntExp[S]): IntExp[S] = ???
  def countLeadingZeros(v: IntExp[S]): IntExp[S] = ???
  def countTrailingZeros(v: IntExp[S]): IntExp[S] = ???
  def nonzeroBitCount(v: IntExp[S]): IntExp[S] = ???
