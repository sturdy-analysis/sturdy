package sturdy.values.ordering

import sturdy.ir.{IR, IROperator, embed}
import sturdy.values.Topped

enum IREqualityOperator extends IROperator:
  case EQ
  case NEQ

given IREqualityOps: EqOps[IR, IR] with
  import IREqualityOperator.*
  override def equ(v1: IR, v2: IR): IR = IR.Op(EQ, v1, v2)
  override def neq(v1: IR, v2: IR): IR = IR.Op(NEQ, v1, v2)

given LiftedIREqualityOps[T](using ops: EqOps[T, Topped[Boolean]]): EqOps[T, IR] with
  override def equ(v1: T, v2: T): IR = embed(ops.equ(v1, v2))
  override def neq(v1: T, v2: T): IR = embed(ops.neq(v1, v2))
