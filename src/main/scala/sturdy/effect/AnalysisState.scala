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

  def repeatUntilStable[A](f: () => A): A = {
    val originalInState = this.getInState()
    val originalOutState = this.getOutState()
    var iterationCount = 0

    var outState: OutState = originalOutState
    var result: A = null.asInstanceOf[A]

    while (true) {
      val oldOutState = outState
      val oldResult = result
      result = f()
      outState = this.getOutState()
      if (result == oldResult && this.isOutStateStable(oldOutState, outState))
        return result
      else {
        iterationCount += 1
        if (Fixpoint.DEBUG)
          println(s"## REPEAT (Iteration $iterationCount)")
        setInState(originalInState)
        setOutState(originalOutState)
      }
    }
    throw new IllegalStateException()
  }
