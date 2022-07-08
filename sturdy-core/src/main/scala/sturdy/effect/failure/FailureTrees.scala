package sturdy.effect.failure

import sturdy.effect.Stateless
import sturdy.effect.SturdyFailure
import sturdy.values.Indent
import sturdy.values.Tree
import sturdy.values.TreeBuffer

case class FailureTree(kind: FailureKind, msg: String) extends Tree, SturdyFailure:
  override def prettyPrint(using Indent): String = s"fail $kind: $msg"

class FailureTrees(using buf: TreeBuffer) extends Failure, Stateless:
  override def fail(kind: FailureKind, msg: String): Nothing =
    val tree = buf += FailureTree(kind, msg)
    throw tree

  def fallible[A](f: => A): CFallible[A] =
    try {
      val res = f
      CFallible.Unfailing(res)
    } catch {
      case FailureTree(kind, msg) => CFallible.Failing(kind, msg)
      case ex => throw ex
    }