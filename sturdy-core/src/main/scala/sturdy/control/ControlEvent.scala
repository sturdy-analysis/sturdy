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
  * ES ::= E*                                // event sequence
  * E ::= Start | Atomic(a:Atom) | Failed    // event
  *     | Begin(s:Section) ES End(s)
  *     | BeginTry ES Throws(exc:Exc) ES Catches(exc)? ES EndTry
  *     | Fork ES Switch ES Join
  * 
  */
enum ControlEvent[Atom, Section, Exc]:
  case Start()
  case Atomic(a: Atom)
  case Failed()

  case Begin(sec: Section)
  case End(sec: Section)

  case BeginTry()
  case Throws(exc: Exc)
  case Catches(exc: Exc)
  case EndTry()

  case Fork()
  case Switch()
  case Join()


  



