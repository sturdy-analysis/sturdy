package sturdy.language.wasm

import sturdy.control.{ControlGraph, Edge, EdgeType, Node}
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.generic.*
import swam.syntax.{Block, If, Loop}

var newNodesTotal = 0
var newEdgesTotal = 0

def testCfgDifference(oldCfg: ControlFlowGraph[CfgNode, _], newCfg: ControlGraph[InstLoc, FuncId | InstLoc]): Unit = {
  type OldNode = CfgNode
  val oldNodes: Set[OldNode] = oldCfg.getNodes.map(_.node).toSet
  val oldEdges: Set[(OldNode, OldNode)] = oldCfg.getEdgesFlat.map(e => e._1.node -> e._2.node).toSet

  type NewNode = Node[InstLoc, FuncId | InstLoc]
  val newNodes: Set[NewNode] = newCfg.nodes
  val newEdges: Set[(NewNode, NewNode)] = newCfg.edges.flatMap(e => if (e.edgeType == EdgeType.BlockPair) Set() else Set(e.from -> e.to))
  val (newEdgesToFailure, newEdgesToSuccess) = newEdges.partition(_._2.isInstanceOf[Node.Failure[_, _]])

  def convert(n: OldNode): NewNode = n match
    case CfgNode.Start => Node.Start()
    case CfgNode.Instruction(inst, loc) => Node.Atomic(loc)(inst.toString)
    case CfgNode.Labled(inst, loc) =>
      val label = inst match
        case Block(_, _) => "Block"
        case Loop(_, _) => "Loop"
        case If(_, _, _) => "If"
      Node.BlockStart(loc)(label)
    case CfgNode.LabledEnd(startNode) =>
      val label = startNode.inst match
        case Block(_, _) => "Block"
        case Loop(_, _) => "Loop"
        case If(_, _, _) => "If"
      Node.BlockEnd(startNode.loc)(label)
    case CfgNode.Call(inst, loc) => Node.BlockStart(loc)(inst.toString)
    case CfgNode.CallReturn(startNode) => Node.BlockEnd(startNode.loc)(startNode.inst.toString)
    case CfgNode.Enter(funId) => Node.BlockStart(funId)("enter")
    case CfgNode.Exit(funId) => Node.BlockEnd(funId)("enter")

  println(s"Old graph:   ${oldNodes.size} nodes, ${oldEdges.size} edges")
  println(s"New graph:   ${newNodes.size} nodes, ${newEdgesToSuccess.size} edges, ${newEdgesToFailure.size} failure edges")
  newNodesTotal += newNodes.size
  newEdgesTotal += newEdges.size

  val oldNodesC = oldNodes.map(convert)
  val oldEdgesC = oldEdges.map(kv => convert(kv._1) -> convert(kv._2))

  assert(oldNodes.size == oldNodesC.size)
  assert(oldEdges.size == oldEdgesC.size)

  val diffNodes1 = newNodes.removedAll(oldNodesC) - Node.Failure()
  val diffNodes2 = oldNodesC.removedAll(newNodes)

  if (diffNodes1.nonEmpty)
    println(s"Old graph, missing nodes: $diffNodes1")
  if (diffNodes2.nonEmpty)
    println(s"Old graph, extra nodes: $diffNodes2")


  val diffEdges1 = newEdgesToSuccess.removedAll(oldEdgesC).removedAll(newEdgesToFailure)

  val extraBlockEdges = oldEdgesC.filter(e => e._1.isInstanceOf[Node.BlockStart[_,_]] && e._2.isInstanceOf[Node.BlockEnd[_,_]])
  val diffEdges2 = oldEdgesC.removedAll(newEdgesToSuccess).removedAll(extraBlockEdges)

  if (diffEdges1.nonEmpty)
    println(s"Old graph, ${diffEdges1.size} missing edges: ${diffEdges1.mkString(", ")}")
  if (extraBlockEdges.nonEmpty)
    println(s"Old graph, ${extraBlockEdges.size} extra block edges: ${extraBlockEdges.mkString(", ")}")
  if (diffEdges2.nonEmpty)
    println(s"Old graph, ${diffEdges2.size} extra edges: ${diffEdges2.mkString(", ")}")

  assert(diffEdges1.isEmpty, s"Old graph, ${diffEdges1.size} missing edges: ${diffEdges1.mkString(", ")} \n" + oldCfg.toGraphViz + "\n\n\n" + newCfg.toGraphViz)
  assert(diffEdges2.isEmpty, s"Old graph, ${diffEdges2.size} extra edges: ${diffEdges2.mkString(", ")} \n" + oldCfg.toGraphViz + "\n\n\n" + newCfg.toGraphViz)
}


def compareControlGraphs(g1: ControlGraph[InstLoc, FuncId | InstLoc], g2: ControlGraph[InstLoc, FuncId | InstLoc]): Unit = {
  type N = Node[InstLoc, FuncId | InstLoc]
  type E = Edge[InstLoc, FuncId | InstLoc]

  println(s"${g1.name}:   ${g1.nodes.size} nodes, ${g1.edges.size} edges")
  println(s"${g2.name}:   ${g2.nodes.size} nodes, ${g2.edges.size} edges")
  compareControlGraphsDirected(g2, g1)
  compareControlGraphsDirected(g1, g2)
}

private def compareControlGraphsDirected(base: ControlGraph[InstLoc, FuncId | InstLoc], g: ControlGraph[InstLoc, FuncId | InstLoc]): Unit = {
  val extraNodes = g.nodes.removedAll(base.nodes)
  val extraEdges = g.edges.removedAll(base.edges)
  val n1 = base.nodes.toList.sortBy(_.toString)
  val n2 = g.nodes.toList.sortBy(_.toString)
  if (extraNodes.nonEmpty)
    println(s"${g.name} has extra nodes:\n${extraNodes.mkString("  ", ",\n  ", "")}")
  if (extraEdges.nonEmpty)
    println(s"${g.name} has extra edges:\n${extraEdges.mkString("  ", ",\n  ", "")}")
}
