package sturdy.values.ordering

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


given ApronOrderingOps
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeOrderingOps: OrderingOps[Type,Type],
   typeBooleanOps: BooleanOps[Type]
  ): OrderingOps[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] with
  override def lt(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronState.comparison(intLt(_,_), v1, v2, typeOrderingOps.lt(v1._type, v2._type))
  override def le(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronState.comparison(intLe(_,_), v1, v2, typeOrderingOps.le(v1._type, v2._type))

given ApronEqOps
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeEqOps: EqOps[Type,Type],
   typeBooleanOps: BooleanOps[Type]
  ): EqOps[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] with
  override def equ(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronState.comparison(intEq(_,_), v1, v2, typeEqOps.equ(v1._type, v2._type))

  override def neq(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    ApronState.comparison(intNeq(_,_), v1, v2, typeEqOps.neq(v1._type, v2._type))