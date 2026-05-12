package sturdy.effect.location

import sturdy.data.MayJoin
import sturdy.effect.Effect
import sturdy.values.{Join, MaybeChanged, Widen}

enum Node[Inst]:
  case Start()
  case Atom(i: Inst)
  case SBlock(i: Inst)
  case EBlock(i: Inst)

  override def toString: String = this match {
    case Node.Start() => "Start"
    case Node.Atom(i) => s"$i"
    case Node.SBlock(i) => s"Start $i"
    case Node.EBlock(i) => s"Exit $i"
  }

enum InstType:
  case Block
  case Atomic
  case Ignore

class CFGLocation[Inst, J[_] <: MayJoin[_]](filter: Inst => InstType = {(_ : Inst) => InstType.Atomic}) extends Location[Inst, J]:
  type Graph = Set[(Node[Inst], Node[Inst])]
  var graph: Graph = Set.empty
  var current: Set[Node[Inst]] = Set(Node.Start())

  override def withLoc[R](a: Inst)(f: => R): R = {
    filter(a) match {
      case InstType.Block =>
        val snode = Node.SBlock(a)
        graph = graph ++ current.map(_ -> snode)
        current = Set(snode)
        val r = f
        val enode = Node.EBlock(a)
        graph = graph ++ current.map(_ -> enode)
        current = Set(enode)
        r
      case InstType.Atomic =>
        val cnode = Node.Atom(a)
        graph = graph ++ current.map(_ -> cnode)
        current = Set(cnode)
        val r = f
        //assert(current == Set(cnode))
        r
      case InstType.Ignore => f
    }
  }

  def toGraphViz: String = {
    graph.map((f, t) => s"\"$f\" -> \"$t\"").fold("digraph G {")((s1, s2) => s1 ++ "\n" ++ s2) ++ "\n"
      ++ "\n\n" ++
      graph.flatMap((s1, s2) => Set(s1, s2)).map {
        case s@Node.Start() => s"\"$s\" [style=filled, fillcolor=\"#FFBBBB\"]"
        case s@Node.Atom(i) => s"\"$s\" [style=filled, fillcolor=\"#BBFFBB\"]"
        case s@Node.SBlock(i) => s"\"$s\" [style=filled, fillcolor=\"#BBBBFF\"]"
        case s@Node.EBlock(i) => s"\"$s\" [style=filled, fillcolor=\"#BBBBFF\"]"
      }.mkString("\n")
      ++ "}"
  }

  override type State = Set[Node[Inst]]

  override def getState: State = current

  override def setState(st: State): Unit =
    current = st

  override def join: Join[State] = (v1: State, v2: State) =>
    MaybeChanged(v1 ++ v2, v1)

  override def widen: Widen[State] = (v1: State, v2: State) =>
    MaybeChanged(v1 ++ v2, v1)

