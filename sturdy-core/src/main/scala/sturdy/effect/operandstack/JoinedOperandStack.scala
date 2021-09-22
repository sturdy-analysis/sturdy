package sturdy.effect.operandstack

import sturdy.fix.Widening
import sturdy.values.JoinValue

/** Stacks of different execution branches are joined. */
trait JoinedOperandStack[V <: AnyRef](using JoinValue[V]) extends GenericOperandStack[V]:
  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = stack
    super.joinComputations(f) {
      val fStack = stack
      stack = snapshot

      try g finally {
        stack = fStack.zipAll(stack, null.asInstanceOf[V], null.asInstanceOf[V]).map {
          case (null, v) => v
          case (v, null) => v
          case (v1, v2) => JoinValue.join(v1, v2)
        }
      }
    }

object JoinedOperandStack:
  type State[V] = GenericOperandStack.State[V]
  given Widen[V](using Widening[V]): Widening[State[V]] = GenericOperandStack.Widen
