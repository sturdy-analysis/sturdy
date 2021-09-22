package sturdy.effect

import sturdy.fix.Fixpoint

trait AnalysisState[In, Out, All]:
  def getInState(): In
  def setInState(in: In): Unit

  def getOutState(): Out
  def setOutState(out: Out): Unit
  
  def getAllState(): All
  def setAllState(all: All): Unit
