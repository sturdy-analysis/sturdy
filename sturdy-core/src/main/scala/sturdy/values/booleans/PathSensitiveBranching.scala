package sturdy.values.booleans

import sturdy.data.joinComputations
import sturdy.ir.IR
import sturdy.data.MakeJoined
import sturdy.effect.EffectStack
import sturdy.values.Join

class PathSensitiveBranching[Cond, R](negate: Cond => Cond)(using j: Join[R], eff: EffectStack) extends BooleanBranching[Cond, R]:
  private var _currentCond: Option[Cond] = None
  private var _inElse = false
  
  def currentCond: Option[Cond] = _currentCond
  def inElse: Boolean = _inElse

  override def boolBranch(cond: Cond, thn: => R, els: => R): R =
    val condBefore = _currentCond
    val inElseBefore = _inElse
    try {
      _currentCond = Some(cond)
      _inElse = false
      joinComputations {
        try thn
        finally eff.assert(cond)
      } {
        _inElse = true
        try els
        finally eff.assert(negate(cond))
      }
    } finally {
      _inElse = inElseBefore
      _currentCond = condBefore
    }
