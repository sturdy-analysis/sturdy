package sturdy.values.booleans

import apron.MpqScalar
import apron.Tcons1
import apron.Texpr1CstNode
import apron.Texpr1UnNode
import sturdy.apron.ApronCons
import sturdy.apron.{UnOp, ApronExpr, Apron}

given ApronBooleanOps: BooleanOps[ApronCons] with

  override def boolLit(b: Boolean): ApronCons = ApronCons.fromBool(b)

  override def and(v1: ApronCons, v2: ApronCons): ApronCons = ???

  override def or(v1: ApronCons, v2: ApronCons): ApronCons = ???

  override def not(v: ApronCons): ApronCons = v.negated
  