package sturdy.values.booleans

import apron.MpqScalar
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1UnNode
// import sturdy.apron.{Apron, ApronCons, ApronExpr, UnOp}

// given ApronBooleanOps[Addr]: BooleanOps[Addr] with
// 
//   override def boolLit(b: Boolean): ApronCons[Addr] = ApronCons[Addr].fromBool(b)
// 
//   override def and(v1: ApronCons[Addr], v2: ApronCons[Addr]): ApronCons[Addr] = ???
// 
//   override def or(v1: ApronCons[Addr], v2: ApronCons[Addr]): ApronCons[Addr] = ???
// 
//   override def not(v: ApronCons[Addr]): ApronCons[Addr] = v.negated
//   // 