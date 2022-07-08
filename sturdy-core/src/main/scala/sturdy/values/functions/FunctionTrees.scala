package sturdy.values.functions

import sturdy.values.Indent
import sturdy.values.Tree
import sturdy.values.TreeBuffer
import sturdy.values.functions.FunctionTree.InvokeFunOpaque

enum FunctionTree[Fun, A] extends Tree:
  case FunValue(fun: Fun)
  case InvokeFun(fun: Tree, args: A, result: List[Tree])
  case InvokeFunOpaque(fun: Tree, args: A)

  override def prettyPrint(using Indent): String = this match
    case FunValue(fun: Fun) => fun.toString
    case InvokeFun(fun, args, result) =>
      val resultStr = Indent.increased {
        result.map(e => Indent.line(e.prettyPrint)).mkString("\n")
      }
      s"invoke(${fun.prettyPrint})($args) {\n$resultStr\n" + Indent.line("}")
    case InvokeFunOpaque(fun, args) =>
      s"invoke-opaque(${fun.prettyPrint})($args)"

given FunctionTrees[Fun, A](using buf: TreeBuffer): FunctionOps[Fun, A, Tree, Tree] with
  def funValue(fun: Fun): FunctionTree[Fun, A] = FunctionTree.FunValue(fun)
  def invokeFun(fun: Tree, a: A)(invoke: (Fun, A) => Tree): FunctionTree[Fun, A] =
    fun match
      case FunctionTree.FunValue(f: Fun) =>
        val mark = buf.mark
        val ret = invoke(f, a)
        val effect = buf.cut(mark)
        FunctionTree.InvokeFun(fun, a, effect :+ ret)
      case _ => FunctionTree.InvokeFunOpaque(fun, a)
//      case _ => throw MatchError(s"Cannot invoke non-function $fun")
