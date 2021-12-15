package sturdy.effect

import sturdy.fix.Fixpoint

trait AnalysisState[Dom, In, Out, All]:
  def getInState(dom: Dom): In
  def setInState(in: In): Unit

  def getOutState(dom: Dom): Out
  def setOutState(out: Out): Unit

  def getAllState: All
  def setAllState(all: All): Unit
