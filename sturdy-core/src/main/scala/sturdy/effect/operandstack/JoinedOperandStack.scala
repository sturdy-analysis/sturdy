package sturdy.effect.operandstack

import sturdy.{IsSound, Soundness}
import sturdy.values.Join

/** Stacks of different execution branches are joined. */
trait JoinedOperandStack[V](using Join[V]) extends GenericOperandStack[V]:
  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = stack
    super.joinComputations(f) {
      val fStack = stack
      stack = snapshot

      try g finally {
        stack = fStack.zipAll(stack, null.asInstanceOf[V], null.asInstanceOf[V]).map {
          case (null, v) => v
          case (v, null) => v
          case (v1, v2) => Join(v1, v2).get
        }
      }
    }
    
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