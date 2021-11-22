package sturdy.effect.operandstack

import sturdy.data.{CombineEquiList, NoJoin, JOptionC}
import sturdy.values.*
import sturdy.data.unit

trait ConcreteOperandStack[V] extends DecidableOperandStack[V]:
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

  override type State = List[V]
  override def getState: List[V] = stack
  override def setState(s: List[V]): Unit = stack = s

  override type OperandFrame = List[V]
  override def getOperandFrame: List[V] = stack.take(stack.size - framePointer)
  override def setOperandFrame(s: List[V]): Unit =
    this.stack = s ++ stack.drop(stack.size - framePointer)

