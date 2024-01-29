package sturdy.control

/** 
  * Represents a program's control flow through a sequence of 
  * control events. Control events can be triggered by an (abstract)
  * interpreter, and control events can be observed to construct
  * a control-flow graph.
  * 
  * Invariants: A valid sequence of control events must adhere to the
  * following data-dependent grammar.
  * 
  * ES ::= Start E*                  // complete event sequence
  * E ::= Atomic(a:Atom) | Failed    // event
  *     | Begin(s:Section) E* End(s)
  *     | BeginTry E* Throws(exc:Exc)? E* Catches(exc)? E* EndTry
  *     | Fork E* Switch E* Join
  *     
  */
enum ControlEvent[Atom, Section, Exc]:
  case Start()
  case Atomic(a: Atom)
  case Failed()

  case Begin(sec: Section)
  case End(sec: Section)

  case BeginTry()
  case Throw()
  case Catch()
  case EndTry()

  case Fork()
  case Switch()
  case Join()

object ControlEvent:
  def toString(es: List[ControlEvent[_,_,_]], _indent: String, sep: String): String =
    val buf = new StringBuffer()
    var indent = _indent
    var rest = es
    while (rest.nonEmpty) {
      val e = rest.head
      e match
        case Begin(_) =>
          buf.append(indent).append(e).append(sep)
          indent += "  "
        case End(_) =>
          indent = indent.drop(2)
          buf.append(indent).append(e).append(sep)
        case Fork() =>
          buf.append(indent).append(e).append(sep)
          indent += "  "
        case Switch() =>
          buf.append(indent.drop(2)).append(e).append(sep)
        case Join() =>
          indent = indent.drop(2)
          buf.append(indent).append(e).append(sep)
        case _ =>
          buf.append(indent).append(e).append(sep)

      rest = rest.tail
    }
    buf.toString


/*

  0. BeginTry, Atomic, EndTry
  1. BeginTry, Throw, Catch, EndTry
  2. BeginTry, Fork, Atomic, Throw, Switch, Atomic, Join, Catch, EndTry
  3. BeginTry, Fork, Atomic, Throw e1, Switch, Atomic, Throw e2, Join, Catch, EndTry

 */


