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

given ApronConsBooleanBranching[Addr, Type, A: Join]
  (using apronState: ApronState[Addr, Type])
  : BooleanBranching[ApronCons[Addr, Type], A] with
  override def boolBranch(v: ApronCons[Addr, Type], thn: => A, els: => A): A =
    apronState.ifThenElse(v)(thn)(els)

given ApronBooleanBranching
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join,
    A: Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeBooleanOps: BooleanOps[Type]
  ): BooleanBranching[ApronExpr[Addr,Type], A] with
  override def boolBranch(v: ApronExpr[Addr, Type], thn: => A, els: => A): A =
    apronState.ifThenElse(ApronCons.eq(v, booleanLit(true))) {
      thn
    } {
      els
    }

given ApronBooleanSelection
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join,
    A: Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeBooleanOps: BooleanOps[Type]
  ): BooleanSelection[ApronExpr[Addr,Type], A] with
  override def boolSelect(v: ApronExpr[Addr, Type], ifTrue: A, ifFalse: A): A =
    ApronBooleanBranching.boolBranch(v, ifTrue, ifFalse)