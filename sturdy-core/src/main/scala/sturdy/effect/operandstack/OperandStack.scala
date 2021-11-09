package sturdy.effect.operandstack

import sturdy.effect.Effectful
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.data.Option
import sturdy.data.NoJoin
import sturdy.data.SomeOption

object StackUnderflow extends FailureKind

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
    if (n < 0)
      throw new IllegalArgumentException
    if (n == 0)
      return SomeOption(Nil)
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

  inline def popOrFail()(using MayJoin[V], Failure): V =
    pop().getOrElse(Failure(StackUnderflow, "pop on empty stack"))

  inline def peekOrFail()(using MayJoin[V], Failure): V =
    peek().getOrElse(Failure(StackUnderflow, "peek on empty stack"))

  inline def pop2OrFail()(using MayJoin[V], Failure): (V,V) =
    val v2 = popOrFail()
    val v1 = popOrFail()
    (v1, v2)

  inline def popNOrFail(n: Int)(using MayJoin[List[V]], Failure): List[V] =
    popN(n).getOrElse(Failure(StackUnderflow, s"popN($n) on stack with less than $n elements"))

  inline def peekNOrFail(n: Int)(using MayJoin[List[V]], Failure): List[V] =
    peekN(n).getOrElse(Failure(StackUnderflow, s"peekN($n) on stack with less than $n elements"))

trait DecidableOperandStack[V] extends OperandStack[V, NoJoin]:
  def size: Int