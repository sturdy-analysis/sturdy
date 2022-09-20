package sturdy.values.floating

import apron.Interval
import apron.Texpr1VarNode
import sturdy.data.CombineUnit
import apron.{Tcons1, Texpr1CstNode, Texpr1UnNode, Texpr1Node, Texpr1BinNode, MpfrScalar}
import sturdy.apron.{JoinApronExpr, Apron}
import sturdy.values.integer.{IntegerDivisionByZero, IntegerOps}

import math.Numeric.Implicits.infixNumericOps
import gmp.Mpfr
import sturdy.apron.ApronCons
import sturdy.apron.{UnOp, ApronExpr, BinOp}
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}

import scala.language.reflectiveCalls


given ApronFloatOps[B](using Fractional[B])
                      (using ap: Apron, effects: EffectStack, f: Failure)
                      : FloatOps[B, ApronExpr] with

  import ApronOrderingOps.*

  override def floatingLit(f: B): ApronExpr = ApronExpr.Constant(new MpfrScalar(f.toDouble, 2))

  override def randomFloat(): ApronExpr = ApronExpr.top

  override def add(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Add, v1, v2)
  override def sub(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Sub, v1, v2)
  override def mul(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Mul, v1, v2)
  override def div(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Div, v1, v2)
  
  override def min(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x1.expr, x2.expr)) {
        ap.assign(r, x1.expr)
      } {
        ap.assign(r, x2.expr)
      }
      r.expr
    }

  override def max(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x1.expr, x2.expr)) {
        ap.assign(r, x2.expr)
      } {
        ap.assign(r, x1.expr)
      }
      r.expr
    }

  override def absolute(v: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariable { x =>
      ap.assign(x, v)
      max(x.expr, negated(x.expr))
    }

  override def negated(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Negate, v)

  override def sqrt(v: ApronExpr): ApronExpr =
    ap.ifThenElse(ge(v, ApronExpr.Constant(MpfrScalar(0, 0)))) {
      ApronExpr.Unary(UnOp.Sqrt, v)
    } {
      f.fail(IntegerDivisionByZero, s"sqrt($v)")
    }

  override def ceil(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_UP)
  override def floor(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_DOWN)
  override def truncate(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_ZERO)
  override def nearest(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Cast, v, Texpr1Node.RTYPE_INT, Texpr1Node.RDIR_NEAREST)

  override def copysign(v: ApronExpr, sign: ApronExpr): ApronExpr =
    ap.withTemporaryDoubleVariables(3) { case List(x, xsign, r) =>
      ap.assign(x, v)
      ap.assign(xsign, sign)
      ap.ifThenElse(lt(x.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
        ap.ifThenElse(lt(xsign.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
          ap.assign(r, x.expr)
        } {
          ap.assign(r, negated(x.expr))
        }
      } {
        ap.ifThenElse(lt(xsign.expr, ApronExpr.Constant(MpfrScalar(0, 0)))) {
          ap.assign(r, negated(x.expr))
        } {
          ap.assign(r, x.expr)
        }
      }
      r.expr
    }