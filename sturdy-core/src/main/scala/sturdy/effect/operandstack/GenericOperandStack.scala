package sturdy.effect.operandstack

import GenericOperandStack.*
import sturdy.data.CombineEquiList
import sturdy.values.*

trait GenericOperandStack[V] extends OperandStack[V]:
  protected var stack: List[V] = Nil
  protected var framePointer: Int = 0

  def getOperandFrame: OperandState[V] = stack.take(stack.size - framePointer)
  protected def setOperandFrame(s: OperandState[V]): Unit =
    this.stack = s ++ stack.drop(stack.size - framePointer)

  def push(v: V): Unit =
    stack = v :: stack

  def pop(): V =
    val v = stack.head
    stack = stack.tail
    v

  def peek(): V =
    stack.head
    
  def ifEmpty[A](empty: => A, notEmpty: => A): A =
    if (stack.isEmpty)
      empty
    else
      notEmpty

  override def peekN(n: Int): List[V] =
    stack.take(n)

  def withFreshOperandStack[A](f: => A): A =
    val snapshot = stack
    stack = Nil
    try f finally
      stack = snapshot

  override def withFreshOperandFrame[A](f: => A): A =
    val snapshotframePointer = framePointer
    framePointer = stack.size
    try f finally
      framePointer = snapshotframePointer

  override def clearCurrentOperandFrame(): Unit =
    stack = stack.drop(stack.size - framePointer)

object GenericOperandStack:
  type OperandState[V] = List[V]
//  case class OperandState[V](vals: List[V]) extends Combinable[OperandState[V]]:
//    type CombineCtx[W] = Combine[V, W]
//    def combine[W <: Widening](that: OperandState[V]) = OperandState(Combine[List[V], W](this.vals, that.vals))
