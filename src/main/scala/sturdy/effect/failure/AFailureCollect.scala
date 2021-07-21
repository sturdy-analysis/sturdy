package sturdy.effect.failure

import sturdy.effect.JoinComputation
import sturdy.effect.JoinComputation.StarvedJoin
import sturdy.values.{Abstractly, PartialOrder}

import scala.collection.mutable.ListBuffer

case class AFailureCollectException(msgs: List[(FailureKind, String)]) extends FailureException:
  override def toString: String = msgs.map((kind, msg) => s"Failure $kind: $msg").mkString("; ")

trait AFailureCollect extends Failure with JoinComputation:
  protected val failures: ListBuffer[(FailureKind,String)] = ListBuffer()

  def getFailures: List[(FailureKind,String)] = failures.toList

  private def logFailures[A](fun: => A): A = try fun catch {
    case fail@AFailureCollectException(msgs) =>
      failures ++= msgs
      throw fail
  }

  override def fail(kind: FailureKind, msg: String): Nothing =
    throw AFailureCollectException(List(kind -> msg))

  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    try super.joinComputations(logFailures(f))(logFailures(g)) catch {
      case StarvedJoin(ex1: AFailureCollectException, ex2: AFailureCollectException) =>
        throw AFailureCollectException(ex1.msgs ++ ex2.msgs)
      case ex => throw ex
    }

given Abstractly[CFailureException, AFailureCollectException] with
  override def abstractly(c: CFailureException): AFailureCollectException = AFailureCollectException(List(c.kind -> c.msg))

given PartialOrder[AFailureCollectException] with
  override def lteq(x: AFailureCollectException, y: AFailureCollectException): Boolean =
    val xFailKinds = x.msgs.map(_._1).toSet
    val yFailKinds = y.msgs.map(_._1).toSet
    xFailKinds.subsetOf(yFailKinds)
