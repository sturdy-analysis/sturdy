package sturdy.effect.operandstack

import sturdy.effect.Effectful
import sturdy.data.Option

trait OperandStack[V, MayJoin[_]] extends Effectful:
  def push(v: V): Unit
  def pop(): Option[MayJoin, V]
  def peek(): Option[MayJoin, V]
  def peekN(n: Int): Option[MayJoin, List[V]]

  /** Computes `f` in a new operand frame, discarding all remaining operands. */
  def withFreshOperandStack[A](f: => A): A

  /** Computes `f` in a new operand frame, but all remaining operands are moved to the surrounding frame upon exit of `f`. */
  def withFreshOperandFrame[A](f: => A): A

  def getOperandFrame: List[V]

  def clearCurrentOperandFrame(): Unit
  
  final def pop2(): Option[MayJoin, (V, V)] =
    val v2 = pop()
    val v1 = pop()
    v1.flatMap(a => v2.map(b => (a, b)))

  final def popN(n: Int): Option[MayJoin,List[V]] =
    if (n <= 0)
      throw new IllegalArgumentException
    val v = pop()
    var vs: Option[MayJoin, List[V]] = v.map(_::Nil)
    for (_ <- 2 to n)
      val popped = pop()
      vs = popped.flatMap(v => vs.map(v::_))
    vs

  final def pushN(vs: List[V]): Unit =
    var rest = vs
    while (rest != Nil) {
      push(rest.head)
      rest = rest.tail
    }
