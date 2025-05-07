package sturdy.language.wasm

import sturdy.control.ControlGraph
import sturdy.language.wasm.generic.*

var newNodesTotal = 0
var newEdgesTotal = 0

def compareControlGraphs(g1: ControlGraph[InstLoc, FuncId | InstLoc], g2: ControlGraph[InstLoc, FuncId | InstLoc]): Unit = {
  println(s"${g1.name}:   ${g1.nodes.size} nodes, ${g1.edges.size} edges")
  println(s"${g2.name}:   ${g2.nodes.size} nodes, ${g2.edges.size} edges")
  compareControlGraphsDirected(g2, g1)
  compareControlGraphsDirected(g1, g2)
}

private def compareControlGraphsDirected(base: ControlGraph[InstLoc, FuncId | InstLoc], g: ControlGraph[InstLoc, FuncId | InstLoc]): Unit = {
  val extraNodes = g.nodes.removedAll(base.nodes)
  val extraEdges = g.edges.removedAll(base.edges)
  if (extraNodes.nonEmpty)
    println(s"${g.name} has extra nodes:\n${extraNodes.mkString("  ", ",\n  ", "")}")
  if (extraEdges.nonEmpty)
    println(s"${g.name} has extra edges:\n${extraEdges.mkString("  ", ",\n  ", "")}")
}
