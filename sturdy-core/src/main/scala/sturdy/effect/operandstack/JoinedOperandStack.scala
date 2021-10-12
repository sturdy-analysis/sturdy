package sturdy.effect.operandstack

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
