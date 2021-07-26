package sturdy.fix

import scala.collection.mutable

case class RecurrentCall(ix: Int) extends Exception

final class Stack[Dom, State]:
  private var stackHeight = 0
  private var stackMap: Map[(Dom, State), Int] = Map()
  private var stack: List[(Dom, State)] = List()

  private val recurrentCalls: mutable.Set[Int] = mutable.BitSet()

  def push(v: (Dom, State)): Unit =
    stackMap.get(v) match
      case None =>
        stack = v :: stack
        stackMap += (v -> stackHeight)
        stackHeight += 1
      case Some(ix) =>
        recurrentCalls += ix
        throw RecurrentCall(ix)

  def pop(): Unit =
    val v = stack.head
    stack = stack.tail
    stackMap -= v
    stackHeight -= 1

  def height: Int = stackHeight

  def activeRecurrentCalls: Set[Int] = recurrentCalls.toSet
  def clearRecurrentCalls(): Unit = recurrentCalls.clear()


