package sturdy.values.booleans

import sturdy.data.{joinComputations, MakeJoined}
import sturdy.effect.EffectStack
import sturdy.ir.IR
import sturdy.values.Join

given IRBooleanBranching[R](using EffectStack, Join[R]): BooleanBranching[IR,R] with
  override def boolBranch(v: IR, thn: => R, els: => R): R =
    joinComputations(thn)(els)
    