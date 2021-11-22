package sturdy.effect.operandstack

import sturdy.effect.Effectful
import sturdy.effect.failure.{Failure, FailureKind}
import sturdy.data.MayJoin
import sturdy.data.JOption
import sturdy.data.NoJoin
import sturdy.data.SomeJOption

object StackUnderflow extends FailureKind

trait OperandStack[V, J[_] <: MayJoin[_]] extends OperandStack.Effectful:
  def push(v: V): Unit
  def pop(): JOption[J, V]
  def peek(): JOption[J, V]
  def peekN(n: Int): JOption[J, List[V]]

  /** Computes `f` in a new operand frame, discarding all remaining operands. */
  def withFreshOperandStack[A](f: => A): A

  /** Computes `f` in a new operand frame, but all remaining operands are moved to the surrounding frame upon exit of `f`. */
  def withFreshOperandFrame[A](f: => A): A

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
    pop().getOrElse(Failure(StackUnderflow, "pop on empty stack"))

  inline def peekOrFail()(using J[V], Failure): V =
    peek().getOrElse(Failure(StackUnderflow, "peek on empty stack"))

  inline def pop2OrFail()(using J[V], Failure): (V,V) =
    val v2 = popOrFail()
    val v1 = popOrFail()
    (v1, v2)

  inline def popNOrFail(n: Int)(using J[List[V]], Failure): List[V] =
    popN(n).getOrElse(Failure(StackUnderflow, s"popN($n) on stack with less than $n elements"))

  inline def peekNOrFail(n: Int)(using J[List[V]], Failure): List[V] =
    peekN(n).getOrElse(Failure(StackUnderflow, s"peekN($n) on stack with less than $n elements"))

trait DecidableOperandStack[V] extends OperandStack[V, NoJoin]:
  def size: Int
  
  
object OperandStack:
  trait Effectful extends sturdy.effect.Effectful:
    type OperandFrame
    def getOperandFrame: OperandFrame
    def setOperandFrame(f: OperandFrame): Unit