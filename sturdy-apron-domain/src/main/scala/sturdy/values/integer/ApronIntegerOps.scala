package sturdy.values.integer

import apron.Interval
import sturdy.data.CombineUnit
import apron.{Environment, Var, Tcons1, Texpr1CstNode, Texpr1UnNode, MpqScalar, Texpr1Node, DoubleScalar, Texpr1BinNode, Texpr0Node}
import gmp.Mpz
import sturdy.apron.ApronCons
import sturdy.apron.{JoinApronExpr, UnOp, BinOp, ApronExpr, Apron}
import sturdy.data.MayJoin.NoJoin
import sturdy.effect.callframe.ApronCallFrame

import java.util
import math.Numeric.Implicits.infixNumericOps
import sturdy.effect.EffectStack
import sturdy.effect.failure.Failure
import sturdy.values.{Top, Topped}
import sturdy.values.ordering.{ApronEqOps, ApronOrderingOps}
import sturdy.values.utils.{ConvertInterval, given}

import scala.language.reflectiveCalls


given ApronIntegerOps[B](using Numeric[B])
                        (using convertInterval: ConvertInterval[B])
                        (using ap: Apron, effects: EffectStack, intervalOps: IntervalIntegerOps[B], f: Failure)
      : IntegerOps[B, ApronExpr] with

  import ApronOrderingOps.*
  
  def apronIntervalToInterval(v: ApronExpr) : NumericInterval[B] =
    val supArray = new Array[Double](1)
    val infArray = new Array[Double](1)
    ap.getBound(v).sup.toDouble(supArray, 0)
    ap.getBound(v).inf.toDouble(infArray, 0)
    val sup = convertInterval.convertTo(supArray(0))
    val inf = convertInterval.convertTo(infArray(0))
    NumericInterval[B](inf, sup)

  def unaryIntervalOp(v: ApronExpr, f: NumericInterval[B] => NumericInterval[B]): ApronExpr =
    val vInterval = f(apronIntervalToInterval(v))
    ApronExpr.Constant(new Interval(convertInterval.convertFrom(vInterval.low), convertInterval.convertFrom(vInterval.high)))

  def binaryIntervalOp(v1: ApronExpr, v2: ApronExpr, f: (NumericInterval[B], NumericInterval[B]) => NumericInterval[B]): ApronExpr =
    val v1Interval = apronIntervalToInterval(v1)
    val v2Interval = apronIntervalToInterval(v2)
    val resInterval = f(v1Interval, v2Interval)
    ApronExpr.Constant(new Interval(convertInterval.convertFrom(resInterval.low), convertInterval.convertFrom(resInterval.high)))

  override def integerLit(i: B): ApronExpr = ApronExpr.Constant(new MpqScalar(convertInterval.convertFrom(i)))

  override def randomInteger(): ApronExpr = ApronExpr.top

  override def add(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Add, v1, v2)
  override def sub(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Sub, v1, v2)
  override def mul(v1: ApronExpr, v2: ApronExpr): ApronExpr = ApronExpr.Binary(BinOp.Mul, v1, v2)
  def neg(v: ApronExpr): ApronExpr = ApronExpr.Unary(UnOp.Negate, v)

  override def max(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x1.expr, x2.expr)) {
        ap.assertConstrain(ApronCons.eq(r.expr, x2.expr))
      } {
        ap.assertConstrain(ApronCons.eq(r.expr, x1.expr))
      }
      r.expr
    }

  override def min(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x1.expr, x2.expr)) {
        ap.assertConstrain(ApronCons.eq(r.expr, x1.expr))
      } {
        ap.assertConstrain(ApronCons.eq(r.expr, x2.expr))
      }
      r.expr
    }

  override def absolute(v: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariable { x =>
      ap.assign(x, v)
      max(x.expr, neg(x.expr))
    }

  def safediv(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ApronExpr.Binary(BinOp.Div,
      ApronExpr.Binary(BinOp.Sub,
        v1,
        ApronExpr.Binary(BinOp.Mod,
          v1,
          v2)),
      v2)

  override def div(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        ap.assertConstrain(ApronCons.eq(r.expr, safediv(x1.expr, x2.expr)))
      } {
        ap.ifThenElse(lt(ApronExpr.Constant(MpqScalar(0)), x2.expr)) {
          ap.assertConstrain(ApronCons.eq(r.expr, safediv(x1.expr, x2.expr)))
        } {
          f.fail(IntegerDivisionByZero, s"$v1 / $v2")
        }
      }
      r.expr
    }

  override def divUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def remainder(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.ifThenElse(lt(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        ap.assertConstrain(ApronCons.eq(r.expr, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr)))
      } {
        ap.ifThenElse(lt(ApronExpr.Constant(MpqScalar(0)), x2.expr)) {
          ap.assertConstrain(ApronCons.eq(r.expr, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr)))
        } {
          f.fail(IntegerDivisionByZero, s"$v1 remainder $v2")
        }
      }
      r.expr
    }

  override def remainderUnsigned(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def modulo(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    ap.withTemporaryIntVariables(3) { case List(x1, x2, r) =>
      ap.assign(x1, v1)
      ap.assign(x2, v2)
      ap.assertConstrain(ApronCons.ge(r.expr, ApronExpr.num(0)))
      // cumbersome
      ap.ifThenElse(lt(x2.expr, ApronExpr.Constant(MpqScalar(0)))) {
        ap.ifThenElse(lt(x1.expr, ApronExpr.Constant(MpqScalar(0)))) {
          ap.assertConstrain(ApronCons.eq(r.expr, sub(ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr)))
        } {
          ap.assertConstrain(ApronCons.eq(r.expr, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr)))
        }
      } {
        ap.ifThenElse(lt(ApronExpr.Constant(MpqScalar(0)), x2.expr)) {
          ap.ifThenElse(lt(x1.expr, ApronExpr.Constant(MpqScalar(0)))) {
            ap.assertConstrain(ApronCons.eq(r.expr, add(ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr), x2.expr)))
          } {
            ap.assertConstrain(ApronCons.eq(r.expr, ApronExpr.Binary(BinOp.Mod, x1.expr, x2.expr)))
          }
        } {
          f.fail(IntegerDivisionByZero, s"$v1 % $v2")
        }
      }
      r.expr
    }

  override def gcd(v1: ApronExpr, v2: ApronExpr): ApronExpr = ???

  override def bitAnd(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitAnd)
  override def bitOr(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitOr)
  override def bitXor(v1: ApronExpr, v2: ApronExpr): ApronExpr =
    binaryIntervalOp(v1, v2, intervalOps.bitXor)
  override def shiftLeft(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftLeft)
  override def shiftRight(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftRight)
  override def shiftRightUnsigned(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.shiftRightUnsigned)
  override def rotateLeft(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.rotateLeft)
  override def rotateRight(v: ApronExpr, shift: ApronExpr): ApronExpr =
    binaryIntervalOp(v, shift, intervalOps.rotateRight)
  override def countLeadingZeros(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.countLeadingZeros)
  override def countTrailingZeros(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.countTrailingZeros)
  override def nonzeroBitCount(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.nonzeroBitCount)
  override def invertBits(v: ApronExpr): ApronExpr =
    unaryIntervalOp(v, intervalOps.invertBits)