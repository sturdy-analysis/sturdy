package sturdy.values.floating

import apron.Interval
import apron.Texpr1VarNode
import sturdy.data.CombineUnit
import apron.{Tcons1, Texpr1CstNode, Texpr1UnNode, Texpr1Node, Texpr1BinNode, MpfrScalar}
import sturdy.apron.{JoinTexpr1Node, Apron}
import sturdy.values.integer.{IntegerDivisionByZero, IntegerOps}

import math.Numeric.Implicits.infixNumericOps
import gmp.Mpfr
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}

import scala.language.reflectiveCalls


given ApronFloatOps[B](using Fractional[B])
                      (using ap: Apron, effects: EffectStack, f: Failure)
                      (using order: ApronOrderingOps, eq: ApronEqOps) : FloatOps[B, Texpr1Node] with


  override def floatingLit(f: B): Texpr1Node = new Texpr1CstNode(new MpfrScalar(f.toDouble, 2))

  override def randomFloat(): Texpr1Node =
    val topIv = new Interval()
    topIv.setTop()
    new Texpr1CstNode(topIv)

  override def add(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_ADD, v1, v2)

  override def sub(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_SUB, v1, v2)

  override def mul(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node = new Texpr1BinNode(Texpr1BinNode.OP_MUL, v1, v2)

  override def div(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(order.lt(x2.node, Texpr1CstNode(MpfrScalar(0, 0)))) {
        ap.assertConstrain(sub(r.node, Texpr1BinNode(Texpr1BinNode.OP_DIV, x1.node, x2.node)), Tcons1.EQ)
      } {
        ap.ifThenElse(order.lt(Texpr1CstNode(MpfrScalar(0, 0)), x2.node)) {
          ap.assertConstrain(sub(r.node, Texpr1BinNode(Texpr1BinNode.OP_DIV, x1.node, x2.node)), Tcons1.EQ)
        } {
          // TODO float division should not produce div by zero errors
          f.fail(IntegerDivisionByZero, s"$v1 / $v2")
        }
      }
      ap.getBoundNode(r)
    }

  override def min(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(order.lt(x1.node, x2.node)) {
        ap.assertConstrain(sub(r.node, x1.node), Tcons1.EQ)
      } {
        ap.assertConstrain(sub(r.node, x2.node), Tcons1.EQ)
      }
      ap.getBoundNode(r)
    }

  override def max(v1: Texpr1Node, v2: Texpr1Node): Texpr1Node =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(order.lt(x1.node, x2.node)) {
        ap.assertConstrain(sub(r.node, x2.node), Tcons1.EQ)
      } {
        ap.assertConstrain(sub(r.node, x1.node), Tcons1.EQ)
      }
      ap.getBoundNode(r)
    }

  override def absolute(v: Texpr1Node): Texpr1Node =
    ap.withTemporaryDoubleVariable { x =>
      ap.assign(x, v)
      max(x.node, negated(x.node))
    }

  override def negated(v: Texpr1Node): Texpr1Node = new Texpr1UnNode(Texpr1UnNode.OP_NEG, v)

  override def sqrt(v: Texpr1Node): Texpr1Node =
    var r: Texpr1Node = null
    ap.ifThenElse(order.ge(v, Texpr1CstNode(MpfrScalar(0, 0)))) {
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
    ap.withTemporaryIntVariables(3) { case List(x, xsign, r) =>
      ap.assign(x, v)
      ap.assign(xsign, sign)
      ap.ifThenElse(order.lt(x.node, Texpr1CstNode(MpfrScalar(0, 0)))) {
        ap.ifThenElse(order.lt(xsign.node, Texpr1CstNode(MpfrScalar(0, 0)))) {
          ap.assertConstrain(sub(r.node, x.node), Tcons1.EQ)
        } {
          ap.assertConstrain(sub(r.node, negated(x.node)), Tcons1.EQ)
        }
      } {
        ap.ifThenElse(order.lt(xsign.node, Texpr1CstNode(MpfrScalar(0, 0)))) {
          ap.assertConstrain(sub(r.node, negated(x.node)), Tcons1.EQ)
        } {
          ap.assertConstrain(sub(r.node, x.node), Tcons1.EQ)
        }
      }
      ap.getBoundNode(r)
    }