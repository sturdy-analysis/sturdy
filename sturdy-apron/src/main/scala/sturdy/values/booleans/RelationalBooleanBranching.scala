package sturdy.values.booleans

import apron.Interval
import sturdy.data.given
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.booleans.BooleanOps
import sturdy.values.integer.IntegerOps

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*
import sturdy.effect.EffectStack

given RelationalBooleanBranching[Addr, Type, A: Join]
  (using effectStack: EffectStack, apronState: ApronState[Addr, Type])
  : BooleanBranching[ApronCons[Addr, Type], A] with
  override def boolBranch(v: ApronCons[Addr, Type], thn: => A, els: => A): A =
    apronState.ifThenElse(effectStack)(v)(thn)(els)


given RelationalBooleanSelection
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join,
    A: Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeBooleanOps: BooleanOps[Type]
  ): BooleanSelection[ApronCons[Addr,Type], A] with
  override def boolSelect(v: ApronCons[Addr, Type], ifTrue: A, ifFalse: A): A =
    apronState.ifThenElse(v)(ifTrue)(ifFalse)
