package sturdy.values.ordering

import sturdy.ir.{IR, IRBinaryOperator, IROperator, IRValue}


trait IROrderingOperator extends IROperator
object IROrderingOperator:
  object LT extends IROrderingOperator, IRBinaryOperator("<")
  object LE extends IROrderingOperator, IRBinaryOperator("<=")
  object LTUnsigned extends IROrderingOperator, IRBinaryOperator("< (unsigned)")
  object LEUnsigned extends IROrderingOperator, IRBinaryOperator("<= (unsigned)")

given IROrderingOps: OrderingOps[IR, IR] with
  import IROrderingOperator.*
  override def lt(v1: IR, v2: IR): IR = IR.Op(LT, v1, v2)
  override def le(v1: IR, v2: IR): IR = IR.Op(LE, v1, v2)

given IRUnsignedOrderingOps: UnsignedOrderingOps[IR, IR] with
  import IROrderingOperator.*

  override def ltUnsigned(v1: IR, v2: IR): IR = IR.Op(LTUnsigned, v1, v2)
  override def leUnsigned(v1: IR, v2: IR): IR = IR.Op(LEUnsigned, v1, v2)

