package sturdy.effect.print

import sturdy.effect.JoinComputation
import sturdy.values.JoinValue

import scala.collection.mutable.ListBuffer

object APrint:
  enum PrintResult[A]:
    case Definite(as: Vector[A])
    case OneOf(rs: Vector[PrintResult[A]])
    case DefiniteSuffix(oneOf: PrintResult.OneOf[A], as: Vector[A])

    def join(that: PrintResult[A]): PrintResult[A] = (this, that) match
      case (PrintResult.OneOf(rs1), PrintResult.OneOf(rs2)) => PrintResult.OneOf(rs1 ++ rs2)
      case (PrintResult.OneOf(rs1), _) => PrintResult.OneOf(rs1 :+ that)
      case (_, PrintResult.OneOf(rs2)) => PrintResult.OneOf(this +: rs2)
      case (r1: PrintResult.Definite[A], r2: PrintResult.Definite[A]) if r1.as == r2.as => r1
      case _ => PrintResult.OneOf(Vector(this, that))


    def :+(a: A): PrintResult[A] = this match
      case PrintResult.Definite(as) => PrintResult.Definite(as :+ a)
      case PrintResult.DefiniteSuffix(oneOf, rs) => PrintResult.DefiniteSuffix(oneOf, rs :+ a)
      case oneOf@PrintResult.OneOf(_) => PrintResult.DefiniteSuffix(oneOf, Vector(a))

    def size: Int = this match
      case PrintResult.Definite(as) => as.size
      case PrintResult.OneOf(rs) => rs.map(_.size).sum
      case PrintResult.DefiniteSuffix(oneOf, as) => oneOf.size + as.size

import APrint.*

trait APrint[A] extends Print[A], JoinComputation:
  private var printed: PrintResult[A] = PrintResult.Definite(Vector.empty)

  def getPrinted: PrintResult[A] = printed

  override def print(a: A): Unit =
    printed = printed :+ a

  override def joinComputations[A](f: => A)(g: => A): Join[A] = {
    val snapshot = printed
    super.joinComputations(f) {
      val printedF = printed
      printed = snapshot
      val a = g
      printed = printedF.join(printed)
      a
    }
  }

