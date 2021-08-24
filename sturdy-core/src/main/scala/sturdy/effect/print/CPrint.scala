package sturdy.effect.print

import scala.collection.mutable.ListBuffer

trait CPrint[A] extends Print[A]:
  private var printed: ListBuffer[A] = ListBuffer()

  def getPrinted: List[A] = printed.toList

  override def print(a: A): Unit =
    printed += a
