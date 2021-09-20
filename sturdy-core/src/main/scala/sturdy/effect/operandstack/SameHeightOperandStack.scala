package sturdy.effect.operandstack

import sturdy.values.JoinValue

/** Branches must yield operand stacks of compatible heights. */
trait SameHeightOperandStack[V](using JoinValue[V]) extends GenericOperandStack[V]:
  override def joinComputations[A](f: => A)(g: => A): Joined[A] =
    val snapshot = stack
    super.joinComputations(f) {
      val fStack = stack
      stack = snapshot

      try g finally {
        if (fStack.size != stack.size)
          throw new IllegalStateException(s"Stacks have different heights at join point: $fStack and $stack")
        stack = fStack.zip(stack).map(JoinValue.join)
      }
    }

