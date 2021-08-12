package sturdy.effect.operandstack

import scala.collection.immutable.VectorBuilder

trait OperandStack[V]:
  def push(v: V): Unit
  def pop(): V
  def peek(): V
  def ifEmpty[A](empty: => A, notEmpty: => A): A
  def withFreshOperandStack[A](f: => A): A
  def restoreAfter[A](f: => A): A

  final def pop2(): (V, V) =
    val v2 = pop()
    val v1 = pop()
    (v1, v2)

  final def popN(n: Int): List[V] =
    var vs: List[V] = Nil
    for (_ <- 1 to n)
      vs = pop()::vs
    vs

  final def pushN(vs: List[V]): Unit =
    var rest = vs
    while (rest != Nil) {
      push(rest.head)
      rest = rest.tail
    }
