package sturdy.values.integer

import sturdy.data.CombineUnit
import apron.{DoubleScalar, Environment, MpqScalar, Tcons1, Texpr0Node, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode, Var}
import sturdy.apron.{Apron, given}
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Top, Topped}
import sturdy.values.ordering.{ApronOrderingOps, ApronEqOps}


given ApronIntegerOps[B](using Numeric[B])
                        (using ap: Apron, effects : EffectStack, intervalOps: IntervalIntegerOps[Int], f : Failure)
                        (using order : ApronOrderingOps, eq : ApronEqOps)
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
    ap.assertConstrain(sub(result, new Texpr1CstNode(new MpqScalar(vInterval.low.toInt))), Tcons1.SUPEQ)
    ap.assertConstrain(add(neg(result), new Texpr1CstNode(new MpqScalar(vInterval.high.toInt))), Tcons1.SUPEQ)
    result

  def binaryIntervalOp(v1: Texpr1Node, v2: Texpr1Node, f: (NumericInterval[Int], NumericInterval[Int]) => NumericInterval[Int]): Texpr1Node =
    val v1Interval = apronIntervalToInterval(v1)
    val v2Interval = apronIntervalToInterval(v2)
    val resInterval = f(v1Interval, v2Interval)
    val result = ap.freshConstraintVariable(s"$f($v1, $v2)")
    ap.assertConstrain(sub(result, new Texpr1CstNode(new MpqScalar(resInterval.low))), Tcons1.SUPEQ)
    ap.assertConstrain(add(neg(result), new Texpr1CstNode(new MpqScalar(resInterval.high))), Tcons1.SUPEQ)
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
    val r = ap.freshConstraintVariable(s"max($v1,$v2)")
    ap.ifThenElse(order.lt(v1,v2)) {
      ap.assertConstrain(sub(r, v2), Tcons1.EQ)
    } {
      ap.assertConstrain(sub(r, v1), Tcons1.EQ)
    }
    r

  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"min($v1,$v2)")
    ap.ifThenElse(order.lt(v1, v2)) {
      ap.assertConstrain(sub(r, v1), Tcons1.EQ)
    } {
      ap.assertConstrain(sub(r, v2), Tcons1.EQ)
    }
    r

  override def absolute(v: Texpr1Node): Texpr1Node =
    max(v, neg(v))

  def safediv (v1:  Texpr1Node, v2: Texpr1Node): Texpr1BinNode =
    Texpr1BinNode(Texpr1BinNode.OP_DIV, Texpr1BinNode(Texpr1BinNode.OP_SUB,v1, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)),  v2)
  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"$v1 / $v2")
    ap.ifThenElse(order.lt(v2, Texpr1CstNode(MpqScalar(0)))) {
      ap.assertConstrain(sub(r, safediv(v1, v2)), Tcons1.EQ)
    } {
      ap.ifThenElse(order.lt(Texpr1CstNode(MpqScalar(0)), v2)) {
        ap.assertConstrain(sub(r, safediv(v1, v2)), Tcons1.EQ)
      } {
        f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      }
    }
    r

  override def divUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def remainder(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"$v1 remainder $v2")
    ap.ifThenElse(order.lt(v2, Texpr1CstNode(MpqScalar(0)))) {
      ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)), Tcons1.EQ)
    } {
      ap.ifThenElse(order.lt(Texpr1CstNode(MpqScalar(0)), v2)) {
        ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)), Tcons1.EQ)
      } {
        f.fail(IntegerDivisionByZero, s"$v1 remainder $v2")
      }
    }
    r

  override def remainderUnsigned(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = ???

  override def modulo(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"$v1 % $v2")
    ap.assertConstrain(r, Tcons1.SUPEQ)
    // cumbersome
    ap.ifThenElse(order.lt(v2, Texpr1CstNode(MpqScalar(0)))) {
      ap.ifThenElse(order.lt(v1, Texpr1CstNode(MpqScalar(0)))) {
        ap.assertConstrain(sub(r, sub(Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2), v2)), Tcons1.EQ)
      } {
        ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)), Tcons1.EQ)
      }
    } {
      ap.ifThenElse(order.lt(Texpr1CstNode(MpqScalar(0)), v2)) {
        ap.ifThenElse(order.lt(v1, Texpr1CstNode(MpqScalar(0)))) {
          ap.assertConstrain(sub(r, add(Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2), v2)), Tcons1.EQ)
        } {
          ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_MOD, v1, v2)), Tcons1.EQ)
        }
      } {
        f.fail(IntegerDivisionByZero, s"$v1 % $v2")
      }
    }
    r

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