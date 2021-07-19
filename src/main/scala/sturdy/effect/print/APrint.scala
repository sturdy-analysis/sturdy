package sturdy.effect.print

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

object APrint:
  enum PrintResult[+A]:
    case Nil
    case AndThen(res: PrintResult[A], a: A)
    case OneOf(r1: PrintResult[A], r2: PrintResult[A])

import APrint.*

trait APrint[A] extends Print[A], JoinComputation:
  private var printed: PrintResult[A] = PrintResult.Nil

  def getPrinted: PrintResult[A] = printed

  override def print(a: A): Unit =
    printed = PrintResult.AndThen(printed, a)

  override def joinComputations[A](f: => A)(g: => A): Join[A] = {
    val snapshot = printed
    var printedF = PrintResult.Nil
    var printedG = PrintResult.Nil
    super.joinComputations(f) {
      val printedF = printed
      printed = snapshot
      val a = g
      if (printed != printedF)
        printed = PrintResult.OneOf(printedF, printed)
      a
    }
  }

