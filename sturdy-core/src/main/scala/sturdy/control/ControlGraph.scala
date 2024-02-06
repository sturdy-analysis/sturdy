package sturdy.control

enum Node[Atom, Section]:
  case Atomic(atom: Atom)
  case BlockStart(sec: Section)
  case BlockEnd(label: Section)
  case Failure()

enum EdgeType:
  case CF
  case BlockPair

case class Edge[Atom, Section](from: Node[Atom, Section], to: Node[Atom, Section], edgeType: EdgeType)


object ControlGraph :
  def toGraphViz[Atom, Sec](edges: List[Edge[Atom, Sec]]): String =
    import Node.*
    edges.map {
      case Edge(_, Failure(), _) => ""
      case Edge(n1, n2, EdgeType.CF) => s"\"$n1\" -> \"$n2\""
      case Edge(n1, n2, EdgeType.BlockPair) => s"\"$n1\" -> \"$n2\"  [style=dashed]"
    }.reduce((s1, s2) => s1 + (if s2 != "" then "\n" else "") + s2)
      + "\n\n" +
      edges.flatMap(e => List(e.from, e.to)).toSet.map {
        case n: Atomic[Atom, Sec] => if(edges.exists { e => e.from == n && e.to == Failure() })
          s"\"$n\" [style=filled, fillcolor=\"#FFBBBB\"]"
        else
          s"\"$n\" [style=filled, fillcolor=\"#BBFFBB\"]"
        case n: BlockStart[Atom, Sec] => s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
        case n: BlockEnd[Atom, Sec] => s"\"$n\" [shape=rect, style=filled, fillcolor=\"#BBBBFF\"]"
        case n : Failure[Atom, Sec] => ""
      }.reduce((s1, s2) => s1 + (if s2 != "" then "\n" else "") + s2) + "\n\n--------------\n\n"