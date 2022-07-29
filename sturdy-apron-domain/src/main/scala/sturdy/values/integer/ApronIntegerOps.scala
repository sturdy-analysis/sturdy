package sturdy.values.integer

import sturdy.data.CombineUnit
import apron.{Environment, Var, Tcons1, Texpr1CstNode, Texpr1UnNode, MpqScalar, Texpr1Node, DoubleScalar, Texpr1BinNode, Texpr0Node}
import sturdy.apron.Apron
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Top

given ApronIntegerOps[B](using Numeric[B])
                                   (using ap: Apron, effects : EffectStack, intervalOps: IntervalIntegerOps[Double], f : Failure)
      : IntegerOps[B, Texpr1Node] with

  def apronIntervalToInterval(v: Texpr1Node) : NumericInterval[Double] =
    val sup = new Array[Double](1)
    val inf = new Array[Double](1)
    callframe.getBound(v).sup.toDouble(sup, 0)
    callframe.getBound(v).inf.toDouble(inf, 0)
    NumericInterval[Double](inf(0), sup(0))

  def unaryIntervalOp(v: Texpr1Node, f: NumericInterval[Double] => NumericInterval[Double]): Texpr1Node =
    val vInterval = f(apronIntervalToInterval(v))
    val result = callframe.freshConstraintVariable(s"$f($v)")
    callframe.constrain(sub(result, new Texpr1CstNode(new MpqScalar(vInterval.low.toInt))), Tcons1.SUPEQ)
    callframe.constrain(add(neg(result), new Texpr1CstNode(new MpqScalar(vInterval.high.toInt))), Tcons1.SUPEQ)
    result

  def binaryIntervalOp(v1: Texpr1Node, v2: Texpr1Node, f: (NumericInterval[Double], NumericInterval[Double]) => NumericInterval[Double]): Texpr1Node =
    val v1Interval = apronIntervalToInterval(v1)
    val v2Interval = apronIntervalToInterval(v2)
    val resInterval = f(v1Interval, v2Interval)
    val result = callframe.freshConstraintVariable(s"$f($v1, $v2)")
    callframe.constrain(sub(result, new Texpr1CstNode(new MpqScalar(resInterval.low.toInt))), Tcons1.SUPEQ)
    callframe.constrain(add(neg(result), new Texpr1CstNode(new MpqScalar(resInterval.high.toInt))), Tcons1.SUPEQ)
    result


  override def integerLit(i: B): Texpr1Node = new Texpr1CstNode(new MpqScalar(i.toInt))

  override def randomInteger(): Texpr1Node =
    ap.freshConstraintVariable("randomZ")

  override def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  override def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  override def mul(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MUL, v1, v2)

  def neg(v: Texpr1Node): Texpr1Node =
    new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  override def max(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val result = ap.freshConstraintVariable(s"max($v1, $v2)")
    // result >= v  iff  result - v >= 0
    ap.constrain(sub(result, v1), Tcons1.SUPEQ)
    ap.constrain(sub(result, v2), Tcons1.SUPEQ)
    effects.joinComputations {
      // result == v1
      ap.constrain(sub(result, v1), Tcons1.EQ)
    } {
      // result == v2
      ap.constrain(sub(result, v2), Tcons1.EQ)
    }
    result

  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val result = ap.freshConstraintVariable(s"max($v1, $v2)")
    // result <= v  iff  result - v <= 0  iff  -result + v > 0
    ap.constrain(add(neg(result), v1), Tcons1.SUP)
    ap.constrain(add(neg(result), v2), Tcons1.SUP)
    effects.joinComputations {
      // result == v1
      ap.constrain(sub(result, v1), Tcons1.EQ)
    } {
      // result == v2
      ap.constrain(sub(result, v2), Tcons1.EQ)
    }
    result

  override def absolute(v: Texpr1Node): Texpr1Node =
    val result = callframe.freshConstraintVariable(s"absolute($v)")
    // result >= v iff result - v >= 0
    callframe.constrain(sub(result, v), Tcons1.SUPEQ)
    // result >= -v iff result + v >= 0
    callframe.constrain(add(result, v), Tcons1.SUPEQ)
    effects.joinComputations {
      // result == v iff result - v == 0
      callframe.constrain(sub(result, v), Tcons1.EQ)
    } {
      // result == -v iff result + v == 0
      callframe.constrain(add(result, v), Tcons1.EQ)
    }
    result

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    // Use join computations for call frame test
    val result = callframe.freshConstraintVariable(s"div($v1,$v2)")
    effects.joinComputations{
      callframe.constrain(v2, Tcons1.DISEQ)
      Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)
    }
    {
      callframe.constrain(v2, Tcons1.EQ)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    }
    Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)


  override def divUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainder(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainderUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def modulo(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)
  override def gcd(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???


  override def bitAnd(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v1, v2, intervalOps.bitAnd)
  override def bitOr(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v1, v2, intervalOps.bitOr)
  override def bitXor(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v1, v2, intervalOps.bitXor)
  override def shiftLeft(v: Texpr1Node, shift: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v, shift, intervalOps.shiftLeft)
  override def shiftRight(v: Texpr1Node, shift: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v, shift, intervalOps.shiftRight)
  override def shiftRightUnsigned(v: Texpr1Node, shift: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v, shift, intervalOps.shiftRightUnsigned)
  override def rotateLeft(v: Texpr1Node, shift: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v, shift, intervalOps.rotateLeft)
  override def rotateRight(v: Texpr1Node, shift: Texpr1Node): Texpr1Node =
    binaryIntervalOp(v, shift, intervalOps.rotateRight)
  override def countLeadingZeros(v: Texpr1Node): Texpr1Node =
    unaryIntervalOp(v, intervalOps.countLeadingZeros)
  override def countTrailingZeros(v: Texpr1Node): Texpr1Node =
    unaryIntervalOp(v, intervalOps.countTrailingZeros)
  override def nonzeroBitCount(v: Texpr1Node): Texpr1Node =
    unaryIntervalOp(v, intervalOps.nonzeroBitCount)
  override def invertBits(v: Texpr1Node): Texpr1Node =
    unaryIntervalOp(v, intervalOps.invertBits)
