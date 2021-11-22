package sturdy.effect.operandstack

import sturdy.data.{CombineEquiList, NoJoin, JOptionC}
import sturdy.values.*
import sturdy.data.unit

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
  
  def withFreshOperandStack[A](f: => A): A =
    val snapshot = stack
    val snapshotFramePointer = framePointer
    stack = Nil
    framePointer = 0
    try f finally {
      stack = snapshot
      framePointer = snapshotFramePointer
    }

  override def withFreshOperandFrame[A](f: => A): A =
    val snapshotframePointer = framePointer
    framePointer = stack.size
    try f finally
      framePointer = snapshotframePointer

  override def clearCurrentOperandFrame(): Unit =
    stack = stack.drop(stack.size - framePointer)

  override type State = (List[V], Int)
  override def getState: (List[V], Int) = (stack, framePointer)
  override def setState(s: (List[V], Int)): Unit =
    stack = s._1
    framePointer = s._2

  override type OperandFrame = List[V]
  override def getOperandFrame: List[V] =
    val f = stack.take(stack.size - framePointer)
    f
  override def setOperandFrame(s: List[V]): Unit =
    this.stack = s ++ stack.drop(stack.size - framePointer)

