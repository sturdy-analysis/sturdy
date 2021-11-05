package sturdy.effect.operandstack

import sturdy.effect.Effectful
import sturdy.data.{LiftedOption, Option, SingletonOption}

trait OperandStack[V, MayJoin[_]] extends Effectful:
  def push(v: V): Unit
  def pop(): Option[MayJoin, V]
  def peek(): Option[MayJoin, V]
  def peekN(n: Int): Option[MayJoin, List[V]]

  /** Computes `f` in a new operand frame, discarding all remaining operands. */
  def withFreshOperandStack[A](f: => A): A

  /** Computes `f` in a new operand frame, but all remaining operands are moved to the surrounding frame upon exit of `f`. */
  def withFreshOperandFrame[A](f: => A): A

  def getOperandFrame: List[V]

  def clearCurrentOperandFrame(): Unit
  
  final def pop2(): Option[MayJoin, (V, V)] =
    val v2 = pop()
    val v1 = pop()
    LiftedOption(v1,v2,((x:V,y:V) => (x,y)))

  final def popN(n: Int): Option[MayJoin,List[V]] =
    var vs: Option[MayJoin,List[V]] = SingletonOption(Nil)
    for (_ <- 1 to n)
      val v = pop()
      vs = LiftedOption(v,vs, (x:V, y:List[V]) => x::y)
    vs

  final def pushN(vs: List[V]): Unit =
    var rest = vs
    while (rest != Nil) {
      push(rest.head)
      rest = rest.tail
    }
