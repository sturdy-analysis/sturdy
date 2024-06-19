package sturdy.language.wasm

import sturdy.control.{ControlGraph, EdgeType, Node}
import sturdy.fix.cfg.ControlFlowGraph
import sturdy.language.wasm.abstractions.CfgNode
import sturdy.language.wasm.generic.*
import swam.syntax.{Block, If, Loop}

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
