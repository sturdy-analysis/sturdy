package sturdy.effect

import sturdy.fix.Fixpoint

trait AnalysisState[In, Out]:
  type InState = In
  type OutState = Out

  def getInState(): InState
  def setInState(in: InState): Unit

  def getOutState(): OutState
  def setOutState(out: OutState): Unit
  
  def isOutStateStable(old: OutState, now: OutState): Boolean
