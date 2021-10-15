package sturdy.effect.operandstack

import sturdy.effect.ComputationJoiner
import sturdy.effect.ComputationJoinerWithSuper
import sturdy.effect.TrySturdy
import sturdy.{Soundness, IsSound}
import sturdy.values.Join

/** Stacks of different execution branches are joined. */
trait JoinedOperandStack[V](using Join[V]) extends GenericOperandStack[V]:

  override def makeComputationJoiner[A]: ComputationJoiner[A] = new ComputationJoinerWithSuper[A](super.makeComputationJoiner) {
    val snapshot = stack
    var fStack: List[V] = null

    override def inbetween_(): Unit =
      fStack = stack
      stack = snapshot

    override def retainOnlyFirst_(fRes: TrySturdy[A]): Unit =
      if (fRes.isSuccess)
        stack = fStack
      else
        stack = snapshot

    override def retainOnlySecond_(gRes: TrySturdy[A]): Unit =
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
    // stack sizes need to be equal
    if (cStack.length != stack.length)
      IsSound.NotSound(s"${classOf[JoinedOperandStack[_]].getName}: expected stack sizes to be equal, but ${cStack.length} != ${stack.length}.")
    else
      // all entries need to be sound
      cStack.zip(stack).foreach {
        case (cv, av) =>
          val subSound = vSoundndess.isSound(cv, av)
          if (subSound.isNotSound)
            return subSound
      }
      IsSound.Sound

object JoinedOperandStack:
  type Operands[V] = List[V]