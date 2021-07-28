package sturdy.effect

trait AnalysisState[In, Out]:
  type InState = In
  type OutState = Out

  def getRelevantInState(): InState
  def getRelevantOutState(): OutState
  def getRelevantOutStateJoinedWith(previous: OutState): OutState
  def isOutStateStable(old: OutState, now: OutState): Boolean
  def setOutState(out: OutState): Unit

  def repeatUntilStable[A](f: () => A): A = {
    val originalState = this.getRelevantOutState()
    var iterationCount = 0

    var outState: OutState = originalState
    var result: A = null.asInstanceOf[A]

    while (true) {
      val oldOutState = outState
      val oldResult = result
      result = f()
      outState = this.getRelevantOutState()
      if (result == oldResult && this.isOutStateStable(oldOutState, outState))
        return result
      else {
        iterationCount += 1
        setOutState(originalState)
      }
    }
    throw new IllegalStateException()
  }
