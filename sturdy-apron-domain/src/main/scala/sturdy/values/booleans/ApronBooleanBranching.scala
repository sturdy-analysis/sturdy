package sturdy.values.booleans

import sturdy.data.CombineUnit
import apron.Tcons1
import sturdy.apron.Apron
import sturdy.apron.ApronCons
import sturdy.effect.EffectStack
import sturdy.values.{Join, Topped}

given ApronBooleanBranching(using apron: Apron, effects: EffectStack): BooleanBranching[ApronCons, Unit] with
  def boolBranch(v: ApronCons, thn: => Unit, els: => Unit): Unit =
    apron.ifThenElse(v)(thn)(els)

given ApronBooleanSelection[R](using apron: Apron, jr: Join[R]): BooleanSelection[ApronCons, R] with
  override def boolSelect(v: ApronCons, ifTrue: R, ifFalse: R): R =
    apron.ifThenElsePure(v, widen = false)(ifTrue)(ifFalse)
    