package sturdy.control

enum ControlTree[Atom, Sec]:
  case Empty()
  case Atomic(a: Atom)
  case Seq(x: ControlTree[Atom, Sec], xs: ControlTree[Atom, Sec])
  case Section(section: Sec, body: ControlTree[Atom, Sec])
  case Fork(b1: ControlTree[Atom, Sec], b2: ControlTree[Atom, Sec])
  case Failed()
