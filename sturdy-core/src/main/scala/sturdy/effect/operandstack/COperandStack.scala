package sturdy.effect.operandstack

trait COperandStack[V] extends OperandStack[V]:
  private var stack: List[V] = Nil

  def push(v: V): Unit =
    stack = v :: stack

  def pop(): V =
    val v = stack.head
    stack = stack.tail
    v

  def peek(): V =
    stack.head
    
  def size(): Int =
    stack.size

  def ifEmpty[A](empty: => A, notEmpty: => A): A =
    if (stack.isEmpty)
      empty
    else
      notEmpty

  def withFreshOperandStack[A](f: => A): A =
    val snapshot = stack
    stack = Nil
    try f finally
      stack = snapshot

  def restoreAfter[A](f: => A): A =
    val snapshot = stack
    try f finally
      stack = snapshot