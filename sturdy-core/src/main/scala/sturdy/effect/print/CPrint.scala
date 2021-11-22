package sturdy.effect.print

import scala.collection.mutable.ListBuffer

class CPrint[A] extends Print[A]:
  private val printed: ListBuffer[A] = ListBuffer()

  def getPrinted: List[A] = printed.toList

  override def apply(a: A): Unit =
    printed += a

  override type State = List[A]
  override def getState: List[A] = printed.toList
  override def setState(s: List[A]): Unit =
    printed.clear()
    printed ++= s