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


trait ControlEvent[+Atom, +Section, +Exc, +Fx]

enum BasicControlEvent[Atom, Section, Exc, Fx] extends ControlEvent[Atom, Section, Exc, Fx]:
  case Atomic(a: Atom)(val label: String)
  case Failed()

  case BeginSection(sec: Section)(val label: String)
  case EndSection()

enum ExceptionControlEvent[Atom, Section, Exc, Fx] extends ControlEvent[Atom, Section, Exc, Fx]:
  case BeginTry()
  case Throw(exc: Exc)
  case Catching()
  case BeginHandle(exc: Exc)
  case EndHandle()
  case EndTry()

enum BranchingControlEvent[Atom, Section, Exc, Fx] extends ControlEvent[Atom, Section, Exc, Fx]:
  case Fork()
  case Switch()
  case Join()

enum FixpointControlEvent[Atom, Section, Exc, Fx] extends ControlEvent[Atom, Section, Exc, Fx]:
  case BeginFixpoint(fx: Fx)
  case Recurrent(fx: Fx)
  case EndFixpoint()
  case Restart()

object BasicControlEvent:
  def atomic[Section, Exc, Fx](label: String): BasicControlEvent[String, Section, Exc, Fx] =
    Atomic(label)(label)
  def beginSection[Atom, Exc, Fx](label: String): BasicControlEvent[Atom, String, Exc, Fx] =
    BeginSection(label)(label)


/*

  0. BeginTry, Atomic, EndTry
  1. BeginTry, Throw, Catch, EndTry
  2. BeginTry, Fork, Atomic, Throw, Switch, Atomic, Join, Catch, EndTry
  3. BeginTry, Fork, Atomic, Throw e1, Switch, Atomic, Throw e2, Join, Catch, EndTry

 */


