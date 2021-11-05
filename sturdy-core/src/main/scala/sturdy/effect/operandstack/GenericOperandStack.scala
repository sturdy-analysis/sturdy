package sturdy.effect.operandstack

import sturdy.data.{CombineEquiList, NoJoin, OptionC}
import sturdy.values.*
import sturdy.data.unit

trait GenericOperandStack[V] extends OperandStack[V, NoJoin]:
  protected var stack: List[V] = Nil
  protected var framePointer: Int = 0

  def getOperandFrame: List[V] = stack.take(stack.size - framePointer)
  protected def setOperandFrame(s: List[V]): Unit =
    this.stack = s ++ stack.drop(stack.size - framePointer)

  def push(v: V): Unit =
    stack = v :: stack

  def pop(): OptionC[V] =
    if (stack.isEmpty)
      OptionC.none
    else
      val v = stack.head
      stack = stack.tail
      OptionC.some(v)

  def safePop(): V =
    pop().getOrElse(throw IllegalStateException("pop on empty stack"))
    
  def safePeek(): V =
    peek().getOrElse(throw IllegalStateException("peek on empty stack"))
    
  def safePop2(): (V,V) =
    pop2().getOrElse((throw IllegalStateException("pop2 on emtpy stack")))
    
  def safePeekN(n: Int): List[V] =
    peekN(n).getOrElse(throw IllegalStateException("peekN on empty stack"))

  def safePopN(n: Int): List[V] =
    popN(n).getOrElse(throw IllegalStateException("popN on empty stack"))
  
  def peek(): OptionC[V] =
    if (stack.isEmpty)
      OptionC.none
    else
      OptionC.some(stack.head)

  override def peekN(n: Int): OptionC[List[V]] =
    if (n > stack.size)
      OptionC.none
    else
      OptionC.some(stack.take(n))

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

