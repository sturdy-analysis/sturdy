package sturdy.ir

import scala.collection.mutable

object Export:

  def toGraphViz(ir: IR): String =
    val visited = mutable.Set[IR]()
    val stack = mutable.Stack[IR](ir)
    val builder = new StringBuilder

    while (stack.nonEmpty) {
      val node = stack.pop()
      visited += node
      for (p <- node.predecessors) {
        builder ++= s"\"$p\" -> \"$node\"\n"
        if (!visited(p))
          stack.push(p)
      }
    }

    builder.toString


