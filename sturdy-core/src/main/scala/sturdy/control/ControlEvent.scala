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


/*

  0. BeginTry, Atomic, EndTry
  1. BeginTry, Throw, Catch, EndTry
  2. BeginTry, Fork, Atomic, Throw, Switch, Atomic, Join, Catch, EndTry
  3. BeginTry, Fork, Atomic, Throw e1, Switch, Atomic, Throw e2, Join, Catch, EndTry

 */


