package sturdy.values.ordering

import sturdy.ir.{IR, IROperator}

enum IROrderingOperator extends IROperator:
  case LT
  case LE

given IROrderingOps: OrderingOps[IR, IR] with
  import IROrderingOperator.*
  override def lt(v1: IR, v2: IR): IR = IR.Op(LT, v1, v2)
  override def le(v1: IR, v2: IR): IR = IR.Op(LE, v1, v2)
  
