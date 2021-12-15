package sturdy.effect.operandstack

import sturdy.data.{CombineEquiList, NoJoin, JOptionC}
import sturdy.values.*
import sturdy.data.unit
import ConcreteOperandStack.*

class ConcreteOperandStack[V] extends DecidableOperandStack[V]:
  protected var stack: List[V] = Nil
  protected var framePointer: Int = 0

  def push(v: V): Unit =
    stack = v :: stack

  def pop(): JOptionC[V] =
    if (stack.isEmpty)
      JOptionC.none
    else
      val v = stack.head
      stack = stack.tail
      JOptionC.some(v)
  
  def peek(): JOptionC[V] =
    if (stack.isEmpty)
      JOptionC.none
    else
      JOptionC.some(stack.head)

  override def peekN(n: Int): JOptionC[List[V]] =
    if (n > stack.size)
      JOptionC.none
    else
      JOptionC.some(stack.take(n))

  override def size: Int = stack.size
  override def frameSize: Int = stack.size - framePointer
  
  def withNewStack[A](f: => A): A =
    val snapshot = stack
    val snapshotFramePointer = framePointer
    stack = Nil
    framePointer = 0
    try f finally {
      stack = snapshot
      framePointer = snapshotFramePointer
    }

  override def withNewFrame[A](movedOps: Int)(f: => A): A =
    val snapshotframePointer = framePointer
    framePointer = stack.size - movedOps
    try f finally
      framePointer = snapshotframePointer

  override def clearCurrentOperandFrame(): Unit =
    stack = stack.drop(stack.size - framePointer)

  override type State = StackState[V]
  override def getState: StackState[V] = StackState(stack, framePointer)
  override def setState(s: StackState[V]): Unit =
    stack = s.stack
    framePointer = s.framePointer

  override type OperandFrame = StackFrameState[V]
  override def getOperandFrame: StackFrameState[V] =
    val vs = stack.take(stack.size - framePointer)
    StackFrameState(vs)
  override def setOperandFrame(s: StackFrameState[V]): Unit =
    clearCurrentOperandFrame()
    this.stack = s.frame ++ this.stack

object ConcreteOperandStack:
  case class StackState[V](stack: List[V], framePointer: Int)
  case class StackFrameState[V](frame: List[V])