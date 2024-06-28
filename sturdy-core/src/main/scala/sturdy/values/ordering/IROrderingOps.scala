package sturdy.values.ordering

import sturdy.ir.{IR, IROperator, IRValue}

enum IROrderingOperator extends IROperator:
  case LT
  case LE

  override def eval(args: Seq[IRValue]): IRValue = this match
    case IROrderingOperator.LT => IRValue(args(0).c.asInstanceOf[Int] < args(1).c.asInstanceOf[Int])
    case IROrderingOperator.LE => IRValue(args(0).c.asInstanceOf[Int] <= args(1).c.asInstanceOf[Int])

given IROrderingOps: OrderingOps[IR, IR] with
  import IROrderingOperator.*
  override def lt(v1: IR, v2: IR): IR = IR.Op(LT, v1, v2)
  override def le(v1: IR, v2: IR): IR = IR.Op(LE, v1, v2)

