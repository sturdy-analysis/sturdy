package sturdy.effect.operandstack

import sturdy.effect.Effect
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.data.MayJoin
import sturdy.data.JOption
import sturdy.data.NoJoin
import sturdy.data.SomeJOption

object StackUnderflow extends FailureKind

trait OperandStack[V, J[_] <: MayJoin[_]] extends Effect:
  def push(v: V): Unit
  def pop(): JOption[J, V]
  def peek(): JOption[J, V]
  def peekN(n: Int): JOption[J, List[V]]

  /** Computes `f` in a new operand frame, discarding all remaining operands. */
  def withNewStack[A](f: => A): A

  /** Computes `f` in a new operand frame, but `movedOps` operands are moved to the new frame and all operands remaining after `f` are moved back to the surrounding frame. */
  def withNewFrame[A](movedOps: Int)(f: => A): A

  def clearCurrentOperandFrame(): Unit
  
  final def pop2(): JOption[J, (V, V)] =
    val v2 = pop()
    val v1 = pop()
    v1.flatMap(a => v2.map(b => (a, b)))

  final def popN(n: Int): JOption[J, List[V]] =
    if (n < 0)
      throw new IllegalArgumentException
    if (n == 0)
      return SomeJOption(Nil)
    val v = pop()
    var vs: JOption[J, List[V]] = v.map(_::Nil)
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

  inline def popOrFail()(using J[V], Failure): V =
    pop().getOrFail(Failure(StackUnderflow, "pop on empty stack"))

  inline def peekOrFail()(using J[V], Failure): V =
    peek().getOrFail(Failure(StackUnderflow, "peek on empty stack"))

  inline def pop2OrFail()(using J[V], Failure): (V,V) =
    val v2 = popOrFail()
    val v1 = popOrFail()
    (v1, v2)

  inline def popNOrFail(n: Int)(using J[List[V]], Failure): List[V] =
    popN(n).getOrFail(Failure(StackUnderflow, s"popN($n) on stack with less than $n elements"))

  inline def peekNOrFail(n: Int)(using J[List[V]], Failure): List[V] =
    peekN(n).getOrFail(Failure(StackUnderflow, s"peekN($n) on stack with less than $n elements"))

  inline def popOrAbort()(using J[V]): V =
    pop().getOrFail(throw new IllegalStateException("pop on empty stack"))

  inline def peekOrAbort()(using J[V]): V =
    peek().getOrFail(throw new IllegalStateException("peek on empty stack"))

  inline def pop2OrAbort()(using J[V]): (V,V) =
    val v2 = popOrAbort()
    val v1 = popOrAbort()
    (v1, v2)

  inline def popNOrAbort(n: Int)(using J[List[V]]): List[V] =
    popN(n).getOrFail(throw new IllegalStateException(s"popN($n) on stack with less than $n elements"))

  inline def peekNOrAbort(n: Int)(using J[List[V]]): List[V] =
    peekN(n).getOrFail(throw new IllegalStateException(s"peekN($n) on stack with less than $n elements"))
