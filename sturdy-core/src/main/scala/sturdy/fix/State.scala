package sturdy.fix

import sturdy.incremental.Delta
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
  def joinOut(dom: Any): Join[Out]
  def widenOut(dom: Any): Widen[Out]

type StateT[I, O, A] = State {
   type In = I
   type Out = O
   type All = A
}

trait StateConverter[I1, I2, O1, O2, A1, A2](val from: StateT[I1, O1, A1], val to: StateT[I2, O2, A2]):
  def convertIn(in: from.In): to.In
  def convertOut(out: from.Out): to.Out
  def convertAll(all: from.All): to.All

class IdStateConverter[I, O, A](val state: StateT[I,O,A]) extends StateConverter[I,I,O,O,A,A](state, state):
  override def convertIn(in: from.In): to.In = in

  override def convertOut(out: from.Out): to.Out = out

  override def convertAll(all: from.All): to.All = all