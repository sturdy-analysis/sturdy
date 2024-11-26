package sturdy.values.booleans

import sturdy.ir.{IR, IROperator}

enum IRBooleanOperator extends IROperator:
  case AND
  case OR
  case NOT

given IRBooleanOps: BooleanOps[IR] with
  override def boolLit(b: Boolean): IR = IR.Const(b)
  override def and(v1: IR, v2: IR): IR = IR.Op(IRBooleanOperator.AND, v1, v2)
  override def or(v1: IR, v2: IR): IR = IR.Op(IRBooleanOperator.OR, v1, v2)
  override def not(v: IR): IR = IR.Op(IRBooleanOperator.NOT, v)
