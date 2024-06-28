package sturdy.values.booleans

import sturdy.ir.IR

given IRBooleanOps: BooleanOps[IR] with
  override def boolLit(b: Boolean): IR = IR.Const(b)
  override def and(v1: IR, v2: IR): IR = ???

  override def not(v: IR): IR = ???

  override def or(v1: IR, v2: IR): IR = ???
