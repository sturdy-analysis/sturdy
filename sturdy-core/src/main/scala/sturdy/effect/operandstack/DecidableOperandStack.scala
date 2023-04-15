package sturdy.effect.operandstack

import sturdy.data.{CombineEquiList, CombineList, JOptionC, NoJoin, unit}
import sturdy.effect.Concrete
import sturdy.values.*
import sturdy.*
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy

trait DecidableOperandStack[V] extends OperandStack[V, NoJoin]:
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

  def size: Int = stack.size
  def frameSize: Int = stack.size - framePointer
  
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

  def operandStackIsSound[cV](c: ConcreteOperandStack[cV])(using vSoundndess: Soundness[cV, V]): IsSound =
    val cStack = c.stack
    val s = seqIsSound.isSound(cStack, stack)
    if (s.isNotSound)
      IsSound.NotSound(s"Unsound operand stack c=$cStack, a=$stack")
    else
      IsSound.Sound


class ConcreteOperandStack[V] extends DecidableOperandStack[V], Concrete


/** Stacks of different execution branches are joined. */
class JoinableDecidableOperandStack[V](using Join[V], Widen[V]) extends DecidableOperandStack[V]:
  override type State = List[V]
  override def getState: List[V] =
    stack.take(stack.size - framePointer)
  override def setState(s: List[V]): Unit =
    clearCurrentOperandFrame()
    this.stack = s ++ this.stack

  override def join: Join[List[V]] = _join
  private val _join: Join[List[V]] = new CombineList
  override def widen: Widen[List[V]] = _widen
  private val _widen: Widen[List[V]] = new CombineList

  override def makeComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new OperandStackJoiner[A])
  private class OperandStackJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = stack
    private var fStack: List[V] = _

    override def inbetween(): Unit =
      fStack = stack
      stack = snapshot

    override def retainNone(): Unit =
      stack = snapshot
    // clearCurrentOperandFrame()

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess)
        stack = fStack
      else
        retainNone()

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      if (!gRes.isSuccess)
        retainNone()

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess && gRes.isSuccess)
        stack = joinStacks(stack, fStack, 0)
      else if (fRes.isSuccess)
        stack = fStack
      else if (gRes.isSuccess) {
        // nothing
      } else
        retainNone()
  }

  private def joinStacks(x: List[V], y: List[V], depth: Int): List[V] =
    if(depth > framePointer || (x eq y))
      x
    else
      (x,y) match
        case (hx::rx, hy::ry) => Join(hx,hy).get :: joinStacks(rx,ry, depth + 1)
        case (_, Nil) => x
        case (Nil, _) => y

//
//    val (frame, rest) = stack.splitAt(stack.size - framePointer)
//    val otherFrame = other.take(stack.size - framePointer)
//    val joinedFrame = frame.zipAll[V,V](otherFrame, null.asInstanceOf[V], null.asInstanceOf[V]).map {
//      case (v1, null) => v1
//      case (null, v2) => v2
//      case (v1, v2) => Join(v1, v2).get
//    }
//    joinedFrame ++ rest

