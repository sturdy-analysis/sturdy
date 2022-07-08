package sturdy.effect.callframe

import sturdy.data.JOptionC
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.Concrete
import sturdy.values.Indent
import sturdy.values.Tree
import sturdy.values.TreeBuffer

enum CallFrameTree[Data, Var] extends Tree:
  case GetLocal(x: Int)
  case SetLocal(x: Int, v: Tree)
  case GetLocalByName(x: Var)
  case SetLocalByName(x: Var, v: Tree)
  case InNewFrame(d: Data, vars: Iterable[(Var, Tree)], effect: List[Tree])

  override def prettyPrint(using Indent): String = this match
    case GetLocal(x) => s"%$x"
    case SetLocal(x, v) => s"%$x := $v"
    case GetLocalByName(x) => s"%$x"
    case SetLocalByName(x, v) => s"%$x := $v"
    case InNewFrame(d, vars, effect) =>
      val varsStr = vars.map { case (x, v) => s"%$x := $v" }.mkString(", ")
      val effectStr = Indent.increased {
        effect.map(e => Indent.line(e.prettyPrint)).mkString("\n")
      }
      s"new frame $d ($varsStr) {\n$effectStr\n" + Indent.line("}")

class CallFrameTrees[Data, Var](initData: Data)(using buf: TreeBuffer) extends MutableCallFrame[Data, Var, Tree, NoJoin], DecidableCallFrame[Data, Var, Tree], Concrete:
  protected var _data: Data = initData
  override def data: Data = _data
  
//  val buf: TreeBuffer = new TreeBuffer

  override def getLocal(x: Int): JOptionC[Tree] = JOptionC.some(CallFrameTree.GetLocal(x))
  override def setLocal(x: Int, v: Tree): JOptionC[Unit] = JOptionC.some(buf += CallFrameTree.SetLocal(x, v))
  override def getLocalByName(x: Var): JOptionC[Tree] = JOptionC.some(CallFrameTree.GetLocalByName(x))
  override def setLocalByName(x: Var, v: Tree): JOptionC[Unit] = JOptionC.some(buf += CallFrameTree.SetLocalByName(x, v))
  override def withNew[A](d: Data, vars: Iterable[(Var, Tree)])(f: => A): A =
    val mark = buf.mark
    val a = f
    val effect = buf.cut(mark)
    buf += CallFrameTree.InNewFrame(d, vars, effect)
    a
