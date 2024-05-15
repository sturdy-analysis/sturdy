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

  def joinIn(dom: Any): Join[In]
  def widenIn(dom: Any): Widen[In]
  def stackWiden(dom: Any): StackWidening[In]
  
  def joinOut(dom: Any): Join[Out]
  def widenOut(dom: Any): Widen[Out]


