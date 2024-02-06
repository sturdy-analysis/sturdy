package sturdy.control

import scala.annotation.targetName

enum ControlTree[Atom, Sec, Exc]:
  case Empty()
  case Atomic(a: Atom)
  case Seq(a: ControlTree[Atom, Sec, Exc], b: ControlTree[Atom, Sec, Exc])
  case Section(section: Sec, body: ControlTree[Atom, Sec, Exc])
  case Fork(b1: ControlTree[Atom, Sec, Exc], b2: ControlTree[Atom, Sec, Exc])
  case Failed()
  
  case Try(body: ControlTree[Atom, Sec, Exc], handle: Map[Exc, ControlTree[Atom, Sec, Exc]])
  case Throw(exc: Exc)
  case Handle(exc: Exc)
  
  @targetName("plusToSeq")
  infix def +(that: ControlTree[Atom, Sec, Exc]) : ControlTree[Atom, Sec, Exc] =
    Seq(this, that)
