package sturdy.effect.failure

import sturdy.effect.JoinComputation
import sturdy.effect.JoinComputation.StarvedJoin

import scala.collection.mutable.ListBuffer

case class AFailureCollectException(msgs: Seq[String]) extends FailureException

trait AFailureCollect extends Failure with JoinComputation:
  protected val failures: ListBuffer[String] = ListBuffer()

  def getFailures: Seq[String] = failures.toSeq

  private def logFailures[A](fun: => A): A = try fun catch {
    case fail@AFailureCollectException(msgs) =>
      failures ++= msgs
      throw fail
  }

  override def fail(msg: String): Nothing =
    throw AFailureCollectException(Seq(msg))

  override def joinComputations[A](f: => A)(g: => A): Join[A] =
    try super.joinComputations(logFailures(f))(logFailures(g)) catch {
      case StarvedJoin(ex1: AFailureCollectException, ex2: AFailureCollectException) =>
        throw AFailureCollectException(ex1.msgs ++ ex2.msgs)
      case ex => throw ex
    }
