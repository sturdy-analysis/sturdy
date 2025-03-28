package sturdy.values.floating

import sturdy.ir.{IR, IROperator}

enum IRFloatOperator extends IROperator:
  case ADD
  case SUB
  case MUL
  case DIV

given IRFloatOps[B]: FloatOps[B, IR] with
  import IRFloatOperator.*

  override def floatingLit(f: B): IR = IR.Const(f)
  override def randomFloat(): IR = IR.Unknown()
  override def add(v1: IR, v2: IR): IR = IR.Op(ADD, v1, v2)
  override def sub(v1: IR, v2: IR): IR = IR.Op(SUB, v1, v2)
  override def mul(v1: IR, v2: IR): IR = IR.Op(MUL, v1, v2)
  override def div(v1: IR, v2: IR): IR = IR.Op(DIV, v1, v2)

  override def min(v1: IR, v2: IR): IR = ???

  override def max(v1: IR, v2: IR): IR = ???

  override def absolute(v: IR): IR = ???

  override def negated(v: IR): IR = ???

  override def sqrt(v: IR): IR = ???

  override def ceil(v: IR): IR = ???

  override def floor(v: IR): IR = ???

  override def truncate(v: IR): IR = ???

  override def nearest(v: IR): IR = ???

  override def copysign(v: IR, sign: IR): IR = ???

