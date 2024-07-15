package sturdy.values.ordering

import apron.Interval
import sturdy.data.given
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}
import sturdy.values.booleans.BooleanOps
import sturdy.values.integer.{IntegerOps, RelationalBaseIntegerOps, RelationalIntOps}

import scala.reflect.ClassTag
import ApronExpr.*
import ApronCons.*


given RelationalOrderingOps[Addr, Type](using IntegerOps[Int,Type]): OrderingOps[ApronExpr[Addr, Type], ApronCons[Addr, Type]] with
  inline override def lt(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] = ApronCons.lt(v1, v2)
  inline override def le(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] = ApronCons.le(v1, v2)

given RelationalEqOps[Addr, Type](using IntegerOps[Int,Type]): EqOps[ApronExpr[Addr, Type], ApronCons[Addr, Type]] with
  inline override def equ(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] = ApronCons.eq(v1, v2)
  inline override def neq(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] = ApronCons.neq(v1, v2)

given RelationalUnsignedOrderingOps
  [
    Addr: Ordering : ClassTag,
    Type: ApronType : Join
  ]
  (using
   apronState: ApronState[Addr, Type],
   integerOps: RelationalIntOps[Addr,Type],
   typeOrderingOps: OrderingOps[Type, Type],
   typeBooleanOps: BooleanOps[Type]
  ): UnsignedOrderingOps[ApronExpr[Addr, Type], ApronCons[Addr, Type]] with
    override def ltUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
      ApronCons.lt(integerOps.interpretSignedAsUnsigned(v1), integerOps.interpretSignedAsUnsigned(v2))

    override def leUnsigned(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronCons[Addr, Type] =
      ApronCons.le(integerOps.interpretSignedAsUnsigned(v1), integerOps.interpretSignedAsUnsigned(v2))
