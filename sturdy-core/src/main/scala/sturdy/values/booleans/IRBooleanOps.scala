package sturdy.values.booleans

import sturdy.ir.{IR, IRBinaryOperator, IROperator}
import sturdy.values.Join

trait IRBooleanOperator extends IROperator
object IRBooleanOperator:
  case object AND extends IRBooleanOperator, IRBinaryOperator("&&")
  case object OR extends IRBooleanOperator, IRBinaryOperator("||")
  case object NOT extends IRBooleanOperator

given IRBooleanOps: BooleanOps[IR] with
  override def boolLit(b: Boolean): IR = IR.Const(b)
  override def and(v1: IR, v2: IR): IR = IR.Op(IRBooleanOperator.AND, v1, v2)
  override def or(v1: IR, v2: IR): IR = IR.Op(IRBooleanOperator.OR, v1, v2)
  override def not(v: IR): IR = IR.Op(IRBooleanOperator.NOT, v)

given IRBooleanSelection[R](using Join[R]): BooleanSelection[IR, R] with
  override def boolSelect(v: IR, ifTrue: R, ifFalse: R): R = Join(ifTrue, ifFalse).get