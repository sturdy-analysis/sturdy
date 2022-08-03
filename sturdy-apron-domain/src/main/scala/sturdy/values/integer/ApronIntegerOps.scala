package sturdy.values.integer

import sturdy.data.CombineUnit
import apron.{DoubleScalar, Environment, MpqScalar, Tcons1, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import sturdy.apron.Apron
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Top, Topped}
import sturdy.values.ordering.{EqOps, OrderingOps}
import sturdy.values.given

given ApronIntegerOps[B](using Numeric[B])
                                   (using ap: Apron, effects : EffectStack, intervalOps: IntervalIntegerOps[Int], f : Failure)
      : IntegerOps[B, Texpr1Node] with

  def apronIntervalToInterval(v: Texpr1Node) : NumericInterval[Int] =
    val supArray = new Array[Double](1)
    val infArray = new Array[Double](1)
    ap.getBound(v).sup.toDouble(supArray, 0)
    ap.getBound(v).inf.toDouble(infArray, 0)
    val sup = supArray(0).toInt
    val inf = infArray(0).toInt
    NumericInterval[Int](inf, sup)

  def unaryIntervalOp(v: Texpr1Node, f: NumericInterval[Int] => NumericInterval[Int]): Texpr1Node =
    val vInterval = f(apronIntervalToInterval(v))
    val result = ap.freshConstraintVariable(s"$f($v)")
    ap.constrain(sub(result, new Texpr1CstNode(new MpqScalar(vInterval.low.toInt))), Tcons1.SUPEQ)
    ap.constrain(add(neg(result), new Texpr1CstNode(new MpqScalar(vInterval.high.toInt))), Tcons1.SUPEQ)
    result

  def binaryIntervalOp(v1: Texpr1Node, v2: Texpr1Node, f: (NumericInterval[Int], NumericInterval[Int]) => NumericInterval[Int]): Texpr1Node =
    val v1Interval = apronIntervalToInterval(v1)
    val v2Interval = apronIntervalToInterval(v2)
    val resInterval = f(v1Interval, v2Interval)
    val result = ap.freshConstraintVariable(s"$f($v1, $v2)")
    ap.constrain(sub(result, new Texpr1CstNode(new MpqScalar(resInterval.low.toInt))), Tcons1.SUPEQ)
    ap.constrain(add(neg(result), new Texpr1CstNode(new MpqScalar(resInterval.high.toInt))), Tcons1.SUPEQ)
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
    ap.constrain(add(neg(result), v1), Tcons1.SUPEQ)
    ap.constrain(add(neg(result), v2), Tcons1.SUPEQ)
    effects.joinComputations {
      // result == v1
      ap.constrain(sub(result, v1), Tcons1.EQ)
    } {
      // result == v2
      ap.constrain(sub(result, v2), Tcons1.EQ)
    }
    result

  override def absolute(v: Texpr1Node): Texpr1Node =
    val result = ap.freshConstraintVariable(s"absolute($v)")
    // result >= v iff result - v >= 0
    ap.constrain(sub(result, v), Tcons1.SUPEQ)
    // result >= -v iff result + v >= 0
    ap.constrain(add(result, v), Tcons1.SUPEQ)
    effects.joinComputations {
      // result == v iff result - v == 0
      ap.constrain(sub(result, v), Tcons1.EQ)
    } {
      // result == -v iff result + v == 0
      ap.constrain(add(result, v), Tcons1.EQ)
    }
    result

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    // Use join computations for call frame test
    ap.ifThenElse(ap.makeConstraint(v2, Tcons1.DISEQ)) {
      Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)
    } {
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    }

  override def divUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def remainder(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    effects.joinWithFailure {
      ap.constrain(v2, Tcons1.DISEQ)

      val result = ap.freshConstraintVariable(s"remainder($v1,$v2)")

      effects.joinComputations {
        ap.constrain(v1, Tcons1.SUPEQ)
        ap.constrain(Texpr1BinNode(Texpr1BinNode.OP_SUB, result, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)), Tcons1.EQ)
      } {
        ap.constrain(neg(v1), Tcons1.SUP)
        ap.constrain(Texpr1BinNode(Texpr1BinNode.OP_SUB, result, Texpr1BinNode(Texpr1BinNode.OP_SUB, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2), v1)), Tcons1.EQ)
      }
      result
    } {
      ap.constrain(v2, Tcons1.EQ)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    }

  override def remainderUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???
  override def modulo(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    effects.joinWithFailure {
      ap.constrain(v2, Tcons1.DISEQ)
      Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)
    } {
      ap.constrain(v2, Tcons1.EQ)
      f.fail(IntegerDivisionByZero, s"$v1 / $v2")
    }
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

given ApronEqOps[B](using ap : Apron, effects : EffectStack, intOps : ApronIntegerOps[B]) : EqOps[Texpr1Node, Topped[Boolean]] with
  override def equ(v1 : Texpr1Node, v2 : Texpr1Node) : Topped[Boolean] =
    ap.ifThenElse(ap.makeConstraint(intOps.sub(v1, v2), Tcons1.EQ)) {Topped.Actual(true)} {Topped.Actual(false)}
  override def neq(v1 : Texpr1Node, v2 : Texpr1Node) : Topped[Boolean] = equ(v1,v2).map(!_)

given ApronOrderingOps[B](using ap : Apron, effects : EffectStack, intOps : ApronIntegerOps[B]) : OrderingOps[Texpr1Node, Topped[Boolean]] with
  override def lt(v1: Texpr1Node, v2: Texpr1Node): Topped[Boolean] =
    // v1 < v2 iff -v1 + v2 > 0
    ap.ifThenElse(ap.makeConstraint(intOps.add(intOps.neg(v1), v2), Tcons1.SUP)) {
        Topped.Actual(true)
      } {
        Topped.Actual(false)
      }
  override def le(v1: Texpr1Node, v2: Texpr1Node): Topped[Boolean] =
  // v1 < v2 iff -v1 + v2 > 0
    ap.ifThenElse(ap.makeConstraint(intOps.add(intOps.neg(v1), v2), Tcons1.SUPEQ)) {
      Topped.Actual(true)
    } {
      Topped.Actual(false)
    }
