package sturdy.values.ordering

//import sturdy.apron.{*,given}
//
//given ApronOrderingOps[Addr]: OrderingOps[ApronExpr[Addr], ApronExpr[Addr]] with
//  override def lt(v1: ApronExpr[Addr], v2: ApronExpr[Addr]): ApronExpr[Addr] =
//    ap.makeTempVar { temp =>
//      ap.ifThenElse {
//        ap.insertConstraint(ApronCons.lt(v1, v2))
//        ap.assign(temp, ApronExpr.Constant(1))
//        ApronExpr._var(temp)
//      } {
//        ap.insertConstraint(ApronCons.ge(v1, v2))
//        ap.assign(temp, ApronExpr.Constant(0))
//        ApronExpr._var(temp)
//      }
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