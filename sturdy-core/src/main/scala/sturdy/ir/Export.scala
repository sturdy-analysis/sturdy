package sturdy.ir

import scala.collection.mutable

object Export:

  def toGraphViz(ir: IR): String =
    val visited = mutable.Set[IR]()
    val stack = mutable.Stack[IR](ir)
    val builder = new StringBuilder
    builder ++= "strict digraph {\n"
    builder ++= s"\"$ir\" [fillcolor=lightcoral, style=filled, fontcolor=black]\n"

    ir.foreach { node =>
      if (node.isInstanceOf[IR.Feedback])
        builder ++= s"\"$node\" [fillcolor=lemonchiffon, style=filled, fontcolor=black]\n"

      for ((p, l) <- node.predecessors) {
        builder ++= s"\"$p\" -> \"$node\" [label = \"$l\"]\n"
      }
    }

    builder ++= "}\n"
    builder.toString


