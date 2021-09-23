package sturdy.effect.operandstack

import sturdy.effect.Effectful

trait OperandStack[V] extends Effectful:
  def push(v: V): Unit
  def pop(): V
  def peek(): V
  def ifEmpty[A](empty: => A, notEmpty: => A): A
  def peekN(n: Int): List[V]

  /** Computes `f` in a new operand frame, discarding all remaining operands. */
  def withFreshOperandStack[A](f: => A): A

  /** Computes `f` in a new operand frame, but all remaining operands are moved to the surrounding frame upon exit of `f`. */
  def withFreshOperandFrame[A](f: => A): A

  def clearCurrentOperandFrame(): Unit
  
  final def pop2(): (V, V) =
    val v2 = pop()
    val v1 = pop()
    (v1, v2)

  final def popN(n: Int): List[V] =
    var vs: List[V] = Nil
    for (_ <- 1 to n)
      vs = pop()::vs
    vs

  final def pushN(vs: List[V]): Unit =
    var rest = vs
    while (rest != Nil) {
      push(rest.head)
      rest = rest.tail
    }
