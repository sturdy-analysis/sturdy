package sturdy.control

enum Node[+Atom, +Section]:
  case Start()
  case Atomic(atom: Atom)
  case BlockStart(sec: Section)
  case BlockEnd(label: Section)
  case Failure()

enum EdgeType:
  case CF
  case Exceptional
  case BlockPair

case class Edge[Atom, Section](from: Node[Atom, Section], to: Node[Atom, Section], edgeType: EdgeType)


case class ControlGraph[Atom,Sec](edges: Set[Edge[Atom, Sec]]):
  lazy val toGraphViz: String =
    def mayFail(n: Node[Atom,Sec]) = edges.exists { e =>
      e.from == n && e.to == Node.Failure()
    }

    import Node.*
    edges.map {
      case Edge(_, Failure(), _) => ""
      case Edge(n1, n2, EdgeType.CF) => s"\"$n1\" -> \"$n2\""
      case Edge(n1, n2, EdgeType.Exceptional) => s"\"$n1\" -> \"$n2\"  [color=red]"
      case Edge(n1, n2, EdgeType.BlockPair) => s"\"$n1\" -> \"$n2\"  [style=dashed]"
    }.toList.sorted.fold("")((s1, s2) => s1 + (if s2 != "" then "\n" else "") + s2)
      + "\n" +
      edges.flatMap(e => List(e.from, e.to)).map {
        case s: Start[Atom, Sec] => s"\"$s\" [style=filled, fillcolor=\"#FFFFBB\"]"
        case n: Atomic[Atom, Sec] =>
          val fail = if (mayFail(n)) ", color=\"#D10F0F\"" else ""
          s"\"$n\" [style=filled, fillcolor=\"#BBFFBB\"$fail]"
        case n: BlockStart[Atom, Sec] =>
          val fail = if (mayFail(n)) ", color=\"#D10F0F\"" else ""
          s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"$fail]"
        case n: BlockEnd[Atom, Sec] => s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
        case n : Failure[Atom, Sec] => ""
      }.toList.sorted.fold("")((s1, s2) => s1 + (if s2 != "" then "\n" else "") + s2)