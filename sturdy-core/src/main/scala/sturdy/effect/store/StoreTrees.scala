package sturdy.effect.store

import sturdy.data.JOptionC
import sturdy.data.MayJoin
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.Stateless
import sturdy.values.Indent
import sturdy.values.Tree
import sturdy.values.TreeBuffer

enum StoreTree extends Tree:
  case Read(x: Tree)
  case Write(x: Tree, v: Tree)
  case Free(x: Tree)

  override def prettyPrint(using Indent): String = this match
    case Read(x) => s"read ${x.prettyPrint}"
    case Write(x, v) => s"write ${x.prettyPrint} := ${v.prettyPrint}"
    case Free(x) => s"free ${x.prettyPrint}"

class StoreTrees(using buf: TreeBuffer) extends Store[Tree, Tree, NoJoin], Stateless:
  override def read(x: Tree): JOptionC[Tree] = JOptionC.some(StoreTree.Read(x))
  override def write(x: Tree, v: Tree): Unit = buf += StoreTree.Write(x, v)
  override def free(x: Tree): Unit = buf += StoreTree.Free(x)