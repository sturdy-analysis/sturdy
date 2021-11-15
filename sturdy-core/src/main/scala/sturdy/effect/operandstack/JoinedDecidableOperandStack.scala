package sturdy.effect.operandstack

import sturdy.seqIsSound
import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.TrySturdy
import sturdy.{Soundness, IsSound}
import sturdy.values.Join

/** Stacks of different execution branches are joined. */
trait JoinedDecidableOperandStack[V](using Join[V]) extends ConcreteOperandStack[V]:

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new OperandStackJoiner[A]
  class OperandStackJoiner[A] extends ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    private val snapshot = stack
    private var fStack: List[V] = _

    override def inbetween_(): Unit =
      fStack = stack
      stack = snapshot

    override def retainNone_(): Unit =
      stack = snapshot

    override def retainFirst_(fRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess)
        stack = fStack
      else
        stack = snapshot

    override def retainSecond_(gRes: TrySturdy[A]): Unit =
      if (!gRes.isSuccess)
        stack = snapshot

    override def retainBoth_(fRes: TrySturdy[A], gRes: TrySturdy[A]): Unit =
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
    val cStack = c.getStack
    seqIsSound.isSound(cStack, stack)

object JoinedDecidableOperandStack:
  type Operands[V] = List[V]