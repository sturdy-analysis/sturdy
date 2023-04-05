package sturdy.effect.print

import sturdy.effect.Concrete

import scala.collection.mutable.ListBuffer

class CPrint[A] extends Print[A], Concrete:
  private val printed: ListBuffer[A] = ListBuffer()

  def getPrinted: List[A] = printed.toList

  override def apply(a: A): Unit =
    printed += a
