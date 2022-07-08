package sturdy.values.references

import sturdy.values.Indent
import sturdy.values.Tree

enum ReferenceTree extends Tree:
  case NullValue()
  case RefValue(addr: Tree)
  case UnmanagedRefValue(addr: Tree)
  case RefAddr(v: Tree)

  override def prettyPrint(using Indent): String = this match
    case NullValue() => "null"
    case RefValue(addr) => s"ref(${addr.prettyPrint})"
    case UnmanagedRefValue(addr) => s"unmanagedRef(${addr.prettyPrint})"
    case RefAddr(v) => s"&${v.prettyPrint}"

given ReferenceTrees: ReferenceOps[Tree, Tree] with
  def nullValue: Tree = ReferenceTree.NullValue()
  def refValue(addr: Tree): Tree = ReferenceTree.RefValue(addr)
  def unmanagedRefValue(addr: Tree): Tree = ReferenceTree.UnmanagedRefValue(addr)
  def refAddr(v: Tree): Tree = ReferenceTree.RefAddr(v)
