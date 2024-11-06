package sturdy.control

enum Node[+Atom, +Section]:
  case Start()
  case Atomic(atom: Atom)(val label: String)
  case BlockStart(sec: Section)(val label: String)
  case BlockEnd(sec: Section)(val label: String)
  case Failure()

  override def toString: String = this match
    case Node.Start() => "Start"
    case n@Node.Atomic(atom) => s"${n.label} @$atom"
    case n@Node.BlockStart(sec) => s"${n.label} @$sec"
    case n@Node.BlockEnd(sec) => s"End ${n.label} @$sec"
    case Node.Failure() => "Failure"

object Node:
  def atomic[Section](atom: String): Node[String, Section] = Atomic(atom)(atom)
  def blockStart[Atom](sec: String): Node[Atom, String] = BlockStart(sec)(sec)
  def blockEnd[Atom](sec: String): Node[Atom, String] = BlockEnd(sec)(sec)


enum EdgeType:
  case CF
  case Exceptional
  case BlockPair

case class Edge[Atom, Section](from: Node[Atom, Section], to: Node[Atom, Section], edgeType: EdgeType)

trait EdgeProvider[Atom, Sec]:
  def edges: Iterable[Edge[Atom, Sec]]

case class ControlGraph[Atom,Sec](edgeProvider: EdgeProvider[Atom, Sec]):
  var name: String = s"ControlGraph_${Integer.toHexString(this.hashCode())}"
  def withName(name: String): this.type = { this.name = name; this }

  lazy val edges: Set[Edge[Atom, Sec]] = edgeProvider.edges.toSet
  lazy val nodes: Set[Node[Atom, Sec]] = edgeProvider.edges.flatMap(e => Set(e.from, e.to)).toSet
  
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