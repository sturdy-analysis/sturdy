package sturdy.values.ordering

import apron.Interval
import sturdy.data.given
import sturdy.apron.{*, given}
import sturdy.effect.failure.Failure
import sturdy.values.{*, given}
import sturdy.values.references.{*, given}

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
   f: Failure,
   typeOrderingOps: OrderingOps[Type,Type]
  ): OrderingOps[ApronExpr[Addr,Type], ApronExpr[Addr,Type]] with {
  override def lt(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] =
    val resultType = typeOrderingOps.lt(v1._type, v2._type)
    apronState.withTempVars(resultType, v1, v2) {
      case (result, List(x,y)) =>
        apronState.ifThenElse(intLt(x,y)) {
          apronState.assign(result, ???)
        } {
          apronState.assign(result, ???)
        }
    }
  override def le(v1: ApronExpr[Addr, Type], v2: ApronExpr[Addr, Type]): ApronExpr[Addr, Type] = ???
}

//import sturdy.apron.{*,given}
//
//given ApronOrderingOps[Addr]: OrderingOps[ApronExpr[Addr], ApronExpr[Addr]] with
//  override def lt(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//    ap.makeTempVar { temp =>
//      ap.ifThenElse {
//        ap.insertConstraint(ApronCons.lt(v1, v2))
//        ap.assign(temp, ApronExpr.Constant(1))
//      } {
//        ap.insertConstraint(ApronCons.ge(v1, v2))
//        ap.assign(temp, ApronExpr.Constant(0))
//      }
//      ApronExpr._var(temp)
//    }
//
//
//  override def le(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronCons[Addr] = ApronCons.le(v1,v2)
//  override def gt(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronCons[Addr] = ApronCons.gt(v1,v2)
//  override def ge(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronCons[Addr] = ApronCons.ge(v1,v2)
//
//given ApronEqOps[Addr]: EqOps[ApronExpr[Addr], ApronCons[Addr]] with
//   override def equ(v1 : ApronExpr[Addr], v2 : ApronExpr[Addr]) : ApronCons[Addr] = ApronCons.eq(v1, v2)
//   override def neq(v1 : ApronExpr[Addr], v2 : ApronExpr[Addr]) : ApronCons[Addr] = ApronCons.neq(v1, v2)