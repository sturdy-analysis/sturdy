package sturdy.effect.operandstack

import sturdy.seqIsSound
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy
import sturdy.{Soundness, IsSound}
import sturdy.values.{Combine, Join}
import sturdy.values.Widening
import ConcreteOperandStack.*
import sturdy.values.MaybeChanged

/** Stacks of different execution branches are joined. */
class JoinableConcreteOperandStack[V](using Join[V]) extends ConcreteOperandStack[V]:

  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new OperandStackJoiner[A])
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
        stack = joinWith(fStack)
      else if (fRes.isSuccess)
        stack = fStack
      else if (gRes.isSuccess) {
        // nothing
      } else
        retainNone()
  }

  private def joinWith(other: List[V]): List[V] =
    val (frame, rest) = stack.splitAt(stack.size - framePointer)
    val otherFrame = other.take(stack.size - framePointer)
    val joinedFrame = frame.zipAll[V,V](otherFrame, null.asInstanceOf[V], null.asInstanceOf[V]).map {
      case (v1, null) => v1
      case (null, v2) => v2
      case (v1, v2) => Join(v1, v2).get
    }
    joinedFrame ++ rest


  def operandStackIsSound[cV](c: ConcreteOperandStack[cV])(using vSoundndess: Soundness[cV, V]): IsSound =
    val cStack = c.getState._1
    val s = seqIsSound.isSound(cStack, stack)
    if (s.isNotSound)
      IsSound.NotSound(s"Unsound operand stack c=$cStack, a=$stack")
    else
      IsSound.Sound

given CombineStackFrameState[V, W <: Widening](using Combine[V, W]): Combine[StackFrameState[V], W] with
  override def apply(ops1: StackFrameState[V], ops2: StackFrameState[V]): MaybeChanged[StackFrameState[V]] =
    var hasChanged = false
    val joinedFrame = ops1.frame.zipAll[V,V](ops2.frame, null.asInstanceOf[V], null.asInstanceOf[V]).map {
      case (v1, null) => v1
      case (null, v2) => v2
      case (v1, v2) =>
        val v = Combine(v1, v2)
        hasChanged |= v.hasChanged
        v.get
    }
    MaybeChanged(StackFrameState(joinedFrame), hasChanged)


