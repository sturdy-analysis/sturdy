package sturdy.values.relational

import sturdy.values.Indent
import sturdy.values.Tree

enum EqTree extends Tree:
  case Equ(v1: Tree, v2: Tree)
  case Neq(v1: Tree, v2: Tree)

  override def prettyPrint(using Indent): String = this match
    case Equ(v1, v2) => s"${v1.prettyPrint} == ${v2.prettyPrint}"
    case Neq(v1, v2) => s"${v1.prettyPrint} != ${v2.prettyPrint}"
  

given EqTrees: EqOps[Tree, Tree] with
  def equ(v1: Tree, v2: Tree): EqTree = EqTree.Equ(v1, v2)
  def neq(v1: Tree, v2: Tree): EqTree = EqTree.Neq(v1, v2)
