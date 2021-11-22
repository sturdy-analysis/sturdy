package sturdy.effect.operandstack

import sturdy.seqIsSound
import sturdy.effect.ComputationJoiner
import sturdy.effect.TrySturdy
import sturdy.{Soundness, IsSound}
import sturdy.values.Join

/** Stacks of different execution branches are joined. */
class JoinedDecidableOperandStack[V](using Join[V]) extends ConcreteOperandStack[V]:

  override def getComputationJoiner[A]: Option[ComputationJoiner[A]] = Some(new OperandStackJoiner[A])
  private class OperandStackJoiner[A] extends ComputationJoiner[A] {
    private val snapshot = stack
    private var fStack: List[V] = _

    override def inbetween(): Unit =
      fStack = stack
      stack = snapshot

    override def retainNone(): Unit =
      stack = snapshot

    override def retainFirst(fRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess)
        stack = fStack
      else
        stack = snapshot

    override def retainSecond(gRes: TrySturdy[A]): Unit =
      if (!gRes.isSuccess)
        stack = snapshot

    override def retainBoth(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
      if (gRes.isSuccess) {
        if (fRes.isSuccess)
          stack = joinWith(fStack)
        // else nothing
      } else if (fRes.isSuccess) {
        stack = fStack
      } else {
        stack = snapshot
      }
  }

  private def joinWith(other: List[V]): List[V] =
    if (stack.size != other.size)
      throw new IllegalStateException()
    val (frame, rest) = stack.splitAt(stack.size - framePointer)
    val otherFrame = other.take(stack.size - framePointer)
    val joinedFrame = frame.zip(otherFrame).map(Join[V](_, _).get)
    joinedFrame ++ rest


  def operandStackIsSound[cV](c: ConcreteOperandStack[cV])(using vSoundndess: Soundness[cV, V]): IsSound =
    val cStack = c.getState._1
    val s = seqIsSound.isSound(cStack, stack)
    if (s.isNotSound)
      IsSound.NotSound(s"Unsound operand stack c=$cStack, a=$stack")
    else
      IsSound.Sound

object JoinedDecidableOperandStack:
  type Operands[V] = List[V]