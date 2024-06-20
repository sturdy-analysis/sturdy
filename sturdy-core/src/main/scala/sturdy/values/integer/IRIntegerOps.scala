package sturdy.values.integer

import sturdy.ir.{IR, IROperator}

enum IntegerIROperator extends IROperator:
  case ADD
  case SUB
  case MUL
  case DIV

given IRIntegerOps[B]: IntegerOps[B, IR] with
  import IntegerIROperator.*

  override def integerLit(i: B): IR = IR.Const(i)
  override def randomInteger(): IR = ???
  override def add(v1: IR, v2: IR): IR = IR.Op(ADD, v1, v2)
  override def sub(v1: IR, v2: IR): IR = IR.Op(SUB, v1, v2)
  override def mul(v1: IR, v2: IR): IR = IR.Op(MUL, v1, v2)
  override def max(v1: IR, v2: IR): IR = ???
  override def min(v1: IR, v2: IR): IR = ???
  override def absolute(v: IR): IR = ???
  override def div(v1: IR, v2: IR): IR = IR.Op(DIV, v1, v2)
  override def divUnsigned(v1: IR, v2: IR): IR = ???
  override def remainder(v1: IR, v2: IR): IR = ???
  override def remainderUnsigned(v1: IR, v2: IR): IR = ???
  override def modulo(v1: IR, v2: IR): IR = ???
  override def gcd(v1: IR, v2: IR): IR = ???
  override def bitAnd(v1: IR, v2: IR): IR = ???
  override def bitOr(v1: IR, v2: IR): IR = ???
  override def bitXor(v1: IR, v2: IR): IR = ???
  override def shiftLeft(v: IR, shift: IR): IR = ???
  override def shiftRight(v: IR, shift: IR): IR = ???
  override def shiftRightUnsigned(v: IR, shift: IR): IR = ???
  override def rotateLeft(v: IR, shift: IR): IR = ???
  override def rotateRight(v: IR, shift: IR): IR = ???
  override def countLeadingZeros(v: IR): IR = ???
  override def countTrailingZeros(v: IR): IR = ???
  override def nonzeroBitCount(v: IR): IR = ???
  override def invertBits(v: IR): IR = ???
