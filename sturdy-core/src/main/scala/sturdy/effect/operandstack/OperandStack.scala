package sturdy.effect.operandstack

trait OperandStack[V]:
  def push(v: V): Unit
  def pop(): V
  def peek(): V
  def ifEmpty[A](empty: => A, notEmpty: => A): A
  def withFreshOperandStack[A](f: => A): A

  final def pop2(): (V, V) =
    val v2 = pop()
    val v1 = pop()
    (v1, v2)

  final def popN(n: Int): List[V] =
    var vs: List[V] = Nil
    (1 to n).foreach(_ => vs +:= pop())
    vs.reverse
