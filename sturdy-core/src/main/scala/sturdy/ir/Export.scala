package sturdy.ir

import scala.collection.mutable

object Export:

  def toGraphViz(ir: IR): String =
    val visited = mutable.Set[IR]()
    val stack = mutable.Stack[IR](ir)
    val builder = new StringBuilder
    builder ++= "strict digraph {\n"
    builder ++= s"\"$ir\" [fillcolor=red, style=filled, fontcolor=black]\n"

    while (stack.nonEmpty) {
      val node = stack.pop()
      visited += node
      for ((p,l) <- node.predecessors) {
        builder ++= s"\"$p\" -> \"$node\" [label = \"$l\"]\n"
        if (!visited(p))
          stack.push(p)
      }
    }

    builder ++= "}\n"
    builder.toString


