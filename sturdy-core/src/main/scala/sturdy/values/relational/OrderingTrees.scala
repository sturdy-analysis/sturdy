package sturdy.values.relational

import sturdy.values.Indent
import sturdy.values.Tree

enum OrderingTree extends Tree:
  case LT(a: Tree, b: Tree)
  case LE(a: Tree, b: Tree)
  case GE(a: Tree, b: Tree)
  case GT(a: Tree, b: Tree)

  override def prettyPrint(using Indent): String = this match
    case LT(a, b) => s"${a.prettyPrint} < ${b.prettyPrint}"
    case LE(a, b) => s"${a.prettyPrint} <= ${b.prettyPrint}"
    case GE(a, b) => s"${a.prettyPrint} >= ${b.prettyPrint}"
    case GT(a, b) => s"${a.prettyPrint} > ${b.prettyPrint}"

given OrderingTrees: OrderingOps[Tree, Tree] with
  def lt(v1: Tree, v2: Tree): OrderingTree = OrderingTree.LT(v1, v2)
  def le(v1: Tree, v2: Tree): OrderingTree = OrderingTree.LE(v1, v2)
  override def ge(v1: Tree, v2: Tree): OrderingTree = OrderingTree.GE(v1, v2)
  override def gt(v1: Tree, v2: Tree): OrderingTree = OrderingTree.GT(v1, v2)
