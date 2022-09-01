package sturdy.values.booleans

import sturdy.data.CombineUnit
import apron.Tcons1
import sturdy.apron.Apron
import sturdy.effect.EffectStack
import sturdy.values.{Topped, Join}

given ApronBooleanBranching(using apron: Apron, effects: EffectStack): BooleanBranching[Tcons1, Unit] with
  def boolBranch(v: Tcons1, thn: => Unit, els: => Unit): Unit = 
    apron.ifThenElse(Topped.Actual(v))(thn)(els)

given ApronBooleanSelection[R](using apron: Apron, jr: Join[R]): BooleanSelection[Tcons1, R] with
  override def boolSelect(v: Tcons1, ifTrue: R, ifFalse: R): R =
    apron.ifThenElsePure(Topped.Actual(v))(ifTrue)(ifFalse)
    