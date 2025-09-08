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
  inline override def boolBranch(v: ApronCons[Addr, Type], thn: => A, els: => A): A =
    apronState.ifThenElse(effectStack)(v)(thn)(els)

given RelationalBooleanBranchingBool[Addr, Type, A: Join]
  (using effectStack: EffectStack, apronState: ApronState[Addr, Type])
  : BooleanBranching[ApronBool[Addr, Type], A] with
  inline override def boolBranch(v: ApronBool[Addr, Type], thn: => A, els: => A): A =
    apronState.ifThenElse(effectStack)(v)(thn)(els)

given RelationalBooleanSelection[Addr, Type, A: Join]
  (using apronState: ApronState[Addr,Type]): BooleanSelection[ApronCons[Addr,Type], A] with
  inline override def boolSelect(v: ApronCons[Addr, Type], ifTrue: A, ifFalse: A): A =
    apronState.ifThenElse(v)(ifTrue)(ifFalse)

given RelationalBooleanSelectionBool[Addr, Type, A: Join]
  (using apronState: ApronState[Addr,Type]): BooleanSelection[ApronBool[Addr,Type], A] with
  inline override def boolSelect(v: ApronBool[Addr, Type], ifTrue: A, ifFalse: A): A =
    apronState.ifThenElse(v)(ifTrue)(ifFalse)

given RelationalBreakIf[Addr, Type](using apronState: ApronState[Addr,Type], effectStack: EffectStack): BreakIf[ApronCons[Addr,Type]] with
  override def breakIf(cond: ApronCons[Addr, Type])(break: => Unit): Unit =
    apronState.ifThenElse(effectStack)(cond) {
      break
    } {
    }
    apronState.addConstraints(cond.negated)

  override def assertCondition(cond: ApronCons[Addr, Type], state: effectStack.State): Unit =
    effectStack.setStateNonMonotonically(state)
    apronState.addConstraints(cond)

given RelationalBreakIfBool[Addr, Type](using apronState: ApronState[Addr,Type], effectStack: EffectStack): BreakIf[ApronBool[Addr,Type]] with
  override def breakIf(cond: ApronBool[Addr, Type])(break:  => Unit): Unit =
    apronState.ifThenElse(effectStack)(cond) {
      break
    } {

    }
    apronState.addCondition(cond.negated)

  override def assertCondition(cond: ApronBool[Addr, Type], state: effectStack.State): Unit =
    effectStack.setStateNonMonotonically(state)
    apronState.addCondition(cond)