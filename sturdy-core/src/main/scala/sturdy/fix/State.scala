package sturdy.fix

import sturdy.values.*

trait State:
  type In
  type Out
  type All

  def getAllState: All
  def setAllState(st: All): Unit

  def getInState(dom: Any): In
  def setInState(dom: Any, in: In): Unit

  def getOutState(dom: Any): Out
  def setOutState(dom: Any, out: Out): Unit

  def joinIn[Body](using Join[Body])(dom: Any): Join[(Body,In)]
  def widenIn[Body](using Widen[Body])(dom: Any): Widen[(Body,In)]
  def stackWiden(dom: Any): StackWidening[In]
  
  def joinOut[Body](using Join[Body])(dom: Any): Join[(Body,Out)]
  def widenOut[Body](using Widen[Body])(dom: Any): Widen[(Body,Out)]

type StateT[I, O] = State { type In = I; type Out = O}
