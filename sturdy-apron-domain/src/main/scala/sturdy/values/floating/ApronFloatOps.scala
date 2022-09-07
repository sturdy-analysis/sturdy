package sturdy.values.floating

import sturdy.data.CombineUnit
import apron.{MpfrScalar, Tcons1, Texpr1BinNode, Texpr1CstNode, Texpr1Node, Texpr1UnNode}
import sturdy.apron.Apron
import sturdy.values.integer.{IntegerDivisionByZero, IntegerOps}

import math.Numeric.Implicits.infixNumericOps
import gmp.Mpfr
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}

val PRECISION = Texpr1Node.RTYPE_DOUBLE

given ApronFloatOps[B](using Fractional[B])
                      (using ap: Apron, effects: EffectStack, f: Failure)
                      (using order: ApronOrderingOps, eq: ApronEqOps) : FloatOps[B, Texpr1Node] with


  override def floatingLit(f: B): Texpr1Node = new Texpr1CstNode(new MpfrScalar(f.toDouble, PRECISION))

  override def randomFloat(): Texpr1Node = ap.freshConstraintVariable("randomFloat")

  override def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  override def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  override def mul(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_MUL, v1, v2)

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"$v1 / $v2")
    ap.ifThenElse(order.lt(v2, Texpr1CstNode(MpfrScalar(0, PRECISION)))) {
      ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)), Tcons1.EQ)
    } {
      ap.ifThenElse(order.lt(Texpr1CstNode(MpfrScalar(0, PRECISION)), v2)) {
        ap.assertConstrain(sub(r, Texpr1BinNode(Texpr1BinNode.OP_DIV, v1, v2)), Tcons1.EQ)
      } {
        f.fail(IntegerDivisionByZero, s"$v1 / $v2")
      }
    }
    r

  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"min($v1, $v2)")
    ap.ifThenElse(order.lt(v1, v2)) {
      ap.assertConstrain(sub(r, v1), Tcons1.EQ)
    } {
      ap.assertConstrain(sub(r, v2), Tcons1.EQ)
    }
    r

  override def max(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"max($v1, $v2)")
    ap.ifThenElse(order.gt(v1, v2)) {
      ap.assertConstrain(sub(r, v1), Tcons1.EQ)
    } {
      ap.assertConstrain(sub(r, v2), Tcons1.EQ)
    }
    r

  override def absolute(v: Texpr1Node): Texpr1Node = max(v, negated(v))

  override def negated(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  override def sqrt(v: Texpr1Node): Texpr1Node =
    var r : Texpr1Node = null
    ap.ifThenElse(order.ge(v, Texpr1CstNode(MpfrScalar(0, PRECISION)))) {
      r = new Texpr1UnNode(Texpr1UnNode.OP_SQRT, v)
    } {
      f.fail(IntegerDivisionByZero, s"sqrt($v)")
    }
    r

  override def ceil(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_CAST, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_UP, v)

  override def floor(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_CAST, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_DOWN, v)

  override def truncate(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_CAST, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_ZERO, v)

  override def nearest(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_CAST, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_NEAREST, v)

  override def copysign(v: Texpr1Node, sign: Texpr1Node): Texpr1Node =
    val r = ap.freshConstraintVariable(s"cs($v, $sign)")
    ap.ifThenElse(order.lt(v, Texpr1CstNode(MpfrScalar(0, PRECISION)))) {
      ap.ifThenElse(order.lt(sign, Texpr1CstNode(MpfrScalar(0, PRECISION)))) {
        ap.assertConstrain(sub(r,v), Tcons1.EQ)
      } {
        ap.assertConstrain(sub(r,negated(v)), Tcons1.EQ)
      }
    } {
      ap.ifThenElse(order.lt(sign, Texpr1CstNode(MpfrScalar(0, PRECISION)))) {
        ap.assertConstrain(sub(r,negated(v)), Tcons1.EQ)
      } {
        ap.assertConstrain(sub(r,v), Tcons1.EQ)
      }
    }
    r