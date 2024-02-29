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

given RelationalBooleanOps
  [
    Addr: Ordering: ClassTag,
    Type : ApronType : Join
  ]
  (using
   apronState: ApronState[Addr,Type],
   typeIntegerOps: IntegerOps[Int,Type],
   typeBooleanOps: BooleanOps[Type]
  ): BooleanOps[ApronExpr[Addr,Type]] with
  override def boolLit(b: Boolean): ApronExpr[Addr, Type] =
    booleanLit(b)

  override def and(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    binary(BinOp.Mul, v1, v2, typeBooleanOps.and(v1._type, v2._type))

  override def or(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeBooleanOps.or(v1._type, v2._type)
    binary(BinOp.Mod, binary(BinOp.Add, v1, v2, resultType), intLit(2), resultType)

  override def not(v: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeBooleanOps.not(v._type)
    binary(BinOp.Mod, binary(BinOp.Add, v, intLit(1), resultType), intLit(2), resultType)
