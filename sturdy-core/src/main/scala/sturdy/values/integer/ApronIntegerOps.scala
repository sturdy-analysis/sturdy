package sturdy.values.integer

import scala.collection.immutable.TreeSet
import apron.Abstract0
import apron.Box
import apron.*
import sturdy.data.MayJoin
import sturdy.values.{Combine, MaybeChanged, Unchanged, Widening}
import sturdy.values.integer.IntegerOps
import sturdy.values.ApronValue
import ApronValue.{toDouble,join,joinCopy}

import scala.compiletime.ops.int

class ApronIntegerOps(using manager: Manager) extends IntegerOps[Int, ApronValue]:
  override def integerLit(i: Int): ApronValue =
    ApronValue.interval(DoubleScalar(i),DoubleScalar(i))
  override def add(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
               expr = Texpr1BinNode(Texpr1BinNode.OP_ADD, v1.expr, v2.expr))
  override def sub(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_SUB, v1.expr, v2.expr))

  override def mul(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_MUL, v1.expr, v2.expr))

  override def div(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_DIV, v1.expr, v2.expr))
  override def modulo(v1: ApronValue, v2: ApronValue): ApronValue =
    ApronValue(domain = v1.domain.joinCopy(v2.domain),
      expr = Texpr1BinNode(Texpr1BinNode.OP_MOD, v1.expr, v2.expr))

  /** absolute(v) = sqrt(v*v) */
  override def absolute(v: ApronValue): ApronValue =
    ApronValue(domain = v.domain,
      expr = Texpr1UnNode(Texpr1UnNode.OP_SQRT, Texpr1BinNode(Texpr1BinNode.OP_MUL, v.expr, v.expr)))

  override def max(v1: ApronValue, v2: ApronValue): ApronValue =
    val i1 = v1.getBound
    val i2 = v2.getBound
    ApronValue.interval(
      scala.math.max(i1.inf().toDouble(), i2.inf().toDouble()),
      scala.math.max(i1.sup().toDouble(), i2.sup().toDouble()))

  override def min(v1: ApronValue, v2: ApronValue): ApronValue = ???

  override def randomInteger(): ApronValue = ApronValue.interval(ApronValue.negInfinity, ApronValue.posInfinity)

  override def bitAnd(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def bitOr(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def bitXor(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def invertBits(v: ApronValue): ApronValue = ???
  override def countLeadingZeros(v: ApronValue): ApronValue = ???
  override def countTrailingZeros(v: ApronValue): ApronValue = ???
  override def divUnsigned(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def gcd(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def nonzeroBitCount(v: ApronValue): ApronValue = ???
  override def remainder(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def remainderUnsigned(v1: ApronValue, v2: ApronValue): ApronValue = ???
  override def rotateLeft(v: ApronValue, shift: ApronValue): ApronValue = ???
  override def rotateRight(v: ApronValue, shift: ApronValue): ApronValue = ???
  override def shiftLeft(v: ApronValue, shift: ApronValue): ApronValue = ???
  override def shiftRight(v: ApronValue, shift: ApronValue): ApronValue = ???
  override def shiftRightUnsigned(v: ApronValue, shift: ApronValue): ApronValue = ???