package sturdy.values.ordering

import sturdy.ir.{IR, IROperator, IRValue}

enum IROrderingOperator extends IROperator:
  case LT
  case LE
  case LTUnsigned
  case LEUnsigned

given IROrderingOps: OrderingOps[IR, IR] with
  import IROrderingOperator.*
  override def lt(v1: IR, v2: IR): IR = IR.Op(LT, v1, v2)
  override def le(v1: IR, v2: IR): IR = IR.Op(LE, v1, v2)

given IRUnsignedOrderingOps: UnsignedOrderingOps[IR, IR] with
  import IROrderingOperator.*

  override def ltUnsigned(v1: IR, v2: IR): IR = IR.Op(LTUnsigned, v1, v2)
  override def leUnsigned(v1: IR, v2: IR): IR = IR.Op(LEUnsigned, v1, v2)

