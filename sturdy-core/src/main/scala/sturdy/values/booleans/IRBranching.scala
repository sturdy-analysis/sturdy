package sturdy.values.booleans

import sturdy.data.joinComputations
import sturdy.ir.IR
import sturdy.data.MakeJoined
import sturdy.effect.EffectStack
import sturdy.values.Join

class IRBranching[R](using Join[R], EffectStack) extends BooleanBranching[IR, R]:
  private var _currentCond: Option[IR] = None
  private var _inElse = false
  
  def currentCond: Option[IR] = _currentCond
  def inElse: Boolean = _inElse

  override def boolBranch(cond: IR, thn: => R, els: => R): R =
    val condBefore = _currentCond
    val inElseBefore = _inElse
    try {
      _currentCond = Some(cond)
      _inElse = false
      joinComputations {
        thn
      } {
        _inElse = true
        els
      }
    }
    finally {
      _inElse = inElseBefore
      _currentCond = condBefore
    }
