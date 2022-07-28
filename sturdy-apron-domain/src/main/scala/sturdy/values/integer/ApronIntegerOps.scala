package sturdy.values.integer

import sturdy.data.CombineUnit
import apron.{DoubleScalar, Environment, Tcons1, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.Top

given ApronIntegerOps[B, Data, Var](using Numeric[B], IntegerOps[B, B], StrictIntegerOps[B, B, NoJoin], Top[NumericInterval[B]], Failure)
                                   (using callframe: ApronCallFrame[Data, Var], effects : EffectStack) : IntegerOps[Int, Texpr1Node] with

  private val intervalOps: IntervalIntegerOps[B] = StandardIntervalIntegerOps

  def unaryIntervalOp(v: Texpr1Node, f: NumericInterval[B] => NumericInterval[B]): Texpr1Node =
  // - convert both to NumericInterval
  // - call interval operation
  // - make into constraints about a new result apron-variable
    ???
  
  def binaryIntervalOp(v1: Texpr1Node, v2: Texpr1Node, f: (NumericInterval[B], NumericInterval[B]) => NumericInterval[B]): Texpr1Node =
  // - convert both to NumericInterval
  // - call interval operation
  // - make into constraints about a new result apron-variable
    ???


  override def integerLit(i: B): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))
  override def integerLit(i: Int): Texpr1Node = new Texpr1CstNode(new DoubleScalar(i.toDouble))
  
  override def randomInteger(): Texpr1Node =
    callframe.freshConstraintVariable("random")

  override def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  override def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  override def mul(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MUL, v1, v2)

  def neg(v: Texpr1Node): Texpr1Node =
    new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  override def max(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val result = callframe.freshConstraintVariable(s"max($v1, $v2)")
    // result >= v  iff  result - v >= 0
    callframe.constrain(sub(result, v1), Tcons1.SUPEQ)
    callframe.constrain(sub(result, v2), Tcons1.SUPEQ)
    effects.joinComputations {
      // result == v1
      callframe.constrain(sub(result, v1), Tcons1.EQ)
    } {
      // result == v2
      callframe.constrain(sub(result, v2), Tcons1.EQ)
    }
    result

  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val result = callframe.freshConstraintVariable(s"max($v1, $v2)")
    // result <= v  iff  result - v <= 0  iff  -result + v > 0
    callframe.constrain(add(neg(result), v1), Tcons1.SUP)
    callframe.constrain(add(neg(result), v2), Tcons1.SUP)
    effects.joinComputations {
      // result == v1
      callframe.constrain(sub(result, v1), Tcons1.EQ)
    } {
      // result == v2
      callframe.constrain(sub(result, v2), Tcons1.EQ)
    }
    result

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    // Use join computations for call frame test
    new Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)

  override def divUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainder(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def remainderUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def modulo(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    new Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)
  override def gcd(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def absolute(v: Texpr1Node): Texpr1Node = ???

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
