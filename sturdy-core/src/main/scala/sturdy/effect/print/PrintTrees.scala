package sturdy.effect.print

import sturdy.effect.Monotone
import sturdy.effect.Stateless
import sturdy.values.Indent
import sturdy.values.Join
import sturdy.values.MaybeChanged
import sturdy.values.MaybeChanged.Unchanged
import sturdy.values.Tree
import sturdy.values.TreeBuffer
import sturdy.values.Widen

case class PrintTree[A](a: A) extends Tree:
  override def prettyPrint(using Indent): String = s"print $a"

class PrintTrees[A](using buf: TreeBuffer) extends Print[A], Stateless:
//  private val buf: TreeBuffer = new TreeBuffer

  def apply(a: A): Unit =
    buf += PrintTree(a)

//  override type State = List[Tree]
//  override def getState: List[Tree] = buf.result
//  def setState(state: List[Tree]): Unit = buf.clear() ++= state
//
//  override def join: Join[List[Tree]] = ???
//  override def widen: Widen[List[Tree]] = (t1, t2) =>
//    println(s"Widen $t1\n with $t2")
//    MaybeChanged(t2, t1)