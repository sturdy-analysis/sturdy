package sturdy.effect.operandstack

import sturdy.data.CombineEquiList
import sturdy.values.*

trait GenericOperandStack[V] extends OperandStack[V]:
  protected var stack: List[V] = Nil
  protected var framePointer: Int = 0

  def getOperandFrame: List[V] = stack.take(stack.size - framePointer)
  protected def setOperandFrame(s: List[V]): Unit =
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

