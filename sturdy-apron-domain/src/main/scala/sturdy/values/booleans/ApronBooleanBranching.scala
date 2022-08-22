package sturdy.values.booleans

import sturdy.data.CombineUnit
import apron.Tcons1
import sturdy.apron.Apron
import sturdy.effect.EffectStack

given ApronBooleanBranching(using apron: Apron, effects: EffectStack): BooleanBranching[Tcons1, Unit] with
  def boolBranch(v: Tcons1, thn: => Unit, els: => Unit): Unit = apron.ifThenElse(v)(thn)(els)

