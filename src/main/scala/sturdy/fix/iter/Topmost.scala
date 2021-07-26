package sturdy.fix.iter

import sturdy.fix.{Combinator, RecurrentCall, Stack}

import scala.collection.mutable

def topmost[Dom, Codom, State](stack: Stack[Dom, State], currentState: () => State): Topmost[Dom, Codom, State] = new Topmost(stack, currentState)
final class Topmost[Dom, Codom, State](stack: Stack[Dom, State], currentState: () => State) extends Combinator[Dom, Codom] {

  override def apply(f: Dom => Codom): Dom => Codom = dom =>
    if (stack.height > 0) {
      step(f, dom, currentState())
    } else {
      // this is the topmost call
      var iterationCount: Int = 0
      var state: State = currentState()
      var result: Codom = null.asInstanceOf[Codom]
      while
        val oldState = state
        val oldResult = result
        result = step(f, dom, oldState)
        state = currentState()
        stack.activeRecurrentCalls.nonEmpty && (result != oldResult || state != oldState)
      do
        iterationCount += 1
        stack.clearRecurrentCalls()

      result
    }

  private def step(f: Dom => Codom, dom: Dom, s: State): Codom =
    stack.push((dom, s))
    try {
      return f(dom)
    } catch {
      case _: RecurrentCall if stack.height == 1 => // absorb the exception but do nothing here
    } finally {
      stack.pop()
    }
    null.asInstanceOf[Codom]
}
